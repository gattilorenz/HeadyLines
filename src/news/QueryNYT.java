package news;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import org.json.JSONArray;   // JSON library from http://www.json.org/java/
import org.json.JSONException;
import org.json.JSONObject;

public class QueryNYT {

	public String makeQuery(String queryString, String nytApi) {
		try
		{
			// Convert spaces to +, etc. to make a valid URL
			String encodedQuery = URLEncoder.encode(queryString, "UTF-8");

			URL url = new URL("http://api.nytimes.com/svc/search/v2/articlesearch.json?q="+encodedQuery+"&api-key="+ nytApi);
			URLConnection connection = url.openConnection();

			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}

			String response = builder.toString();
			return response;
		}
		catch (Exception e) {
			System.err.println("Something went wrong...");
			e.printStackTrace();
		}
		return null;
	}

	private boolean isValid(String headline) {
		if (headline.trim().endsWith("...")) return false;
		if (headline.trim().endsWith("?")) return false;
		if (headline.toUpperCase().startsWith("PAID NOTICE")) return false;
		return true;
	}
	
	

	public String queryByDate(LocalDate date, String nytApi, int pageIndex) {
		String response = "";
		try
		{
			//DateFormat df = new SimpleDateFormat("yyyyMMdd");
			DateTimeFormatter df = DateTimeFormatter.BASIC_ISO_DATE; //yyyyMMdd
			String dateStartString =  date.format(df);
			String dateEndString = date.plusDays(1).format(df);
   		    //Convert spaces to +, etc. to make a valid URL
			dateStartString = URLEncoder.encode(dateStartString, "UTF-8");
			dateEndString   = URLEncoder.encode(dateEndString, "UTF-8");

			String urlString = "http://api.nytimes.com/svc/search/v2/articlesearch.json?facet_field=day_of_week&begin_date="+dateStartString+"&end_date="+dateEndString+"&page=" + pageIndex+"&sort=newest&api-key=" + nytApi ;
			//System.out.println("urlString: " + urlString);
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();

			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}

			response = builder.toString();
		}
		catch (Exception e) {
			System.err.println("Something went wrong...");
			e.printStackTrace();
		}
		return response;
	}

	//	news.add(new NewsObject(message.getTitle(), message.getDescription(), "BBC", new Date()));

	//returns the number of results returned by the query, returns -1 if there's a problem
	int getResultCount(String queryString, String nytApi){
		//System.out.println("Querying for "+queryString+"...");
		String jsonResponse = makeQuery(queryString, nytApi);
		if(jsonResponse != null){
			try{
				JSONObject json = new JSONObject(jsonResponse);
				return json.getJSONObject("response").getJSONObject("meta").getInt("hits");
			}
			catch(JSONException jEx){
				jEx.printStackTrace();
			}
		}
		return -1;
	}

	public int determineExtraQueryCount(int maxQueryCount, int resultCount){
		int mode = resultCount % 10;
		int numOfExtraQueries =  resultCount / 10;
		if (mode == 0){ 
			numOfExtraQueries--;
		}
		return Math.min(numOfExtraQueries, maxQueryCount-1);
	}

	public void writeDailyNews(LocalDate date, int maxQueryCount, String nytApi, BufferedWriter bufWriter){
		int pageIndex = 0;
		String jsonResponse = queryByDate(date, nytApi, pageIndex);
		int resultCount = 0;
		JSONObject json = null;
		if(jsonResponse != null){
			try{
				json = new JSONObject(jsonResponse);
				resultCount =  json.getJSONObject("response").getJSONObject("meta").getInt("hits");
				//System.out.println("resultCount: "+resultCount);
			}
			catch(JSONException jEx){
				jEx.printStackTrace();
			}
		}
		if (resultCount > 0){
			//write the contents of the first page
			writeNewsOnOnePage(json, bufWriter);

			//check how many more times you need to do the query
			int numOfExtraQueries = determineExtraQueryCount(maxQueryCount, resultCount);
			//System.out.println("numOfExtraQueries: "+numOfExtraQueries);

			for (int qInd = 0 ; qInd < numOfExtraQueries ; qInd++){
				pageIndex++;
				//System.out.println("qInd: "+ qInd);
				jsonResponse = queryByDate(date, nytApi, pageIndex);
				if(jsonResponse != null){
					try{
						json = new JSONObject(jsonResponse);
						writeNewsOnOnePage(json, bufWriter);
					}
					catch(JSONException jsEx){
						jsEx.printStackTrace();
					}
				}
			}
		}
	}

	public ArrayList<NewsObject> getNewsByDate(LocalDate date, int maxQueryCount, String nytApi){
		ArrayList<NewsObject> news = new ArrayList<NewsObject>();
		int pageIndex = 0;
		
		String jsonResponse = queryByDate(date, nytApi, pageIndex);
		int resultCount = 0;
		JSONObject json = null;
		if(jsonResponse != null){
			try{
				json = new JSONObject(jsonResponse);
				resultCount =  json.getJSONObject("response").getJSONObject("meta").getInt("hits");
				//System.out.println("resultCount: "+resultCount);
			}
			catch(JSONException jEx){
				jEx.printStackTrace();
			}
		}
		if (resultCount > 0){
			//write the contents of the first page
			news.addAll(arrayFromOnePage(json, date));

			//check how many more times you need to do the query
			int numOfExtraQueries = determineExtraQueryCount(maxQueryCount, resultCount);
			//System.out.println("numOfExtraQueries: "+numOfExtraQueries);

			for (int qInd = 0 ; qInd < numOfExtraQueries ; qInd++){
				try {
					Thread.sleep(1000); //sleep for NYT API limits
				} catch (InterruptedException e) {
				}				
				pageIndex++;
				//System.out.println("qInd: "+ qInd);
				jsonResponse = queryByDate(date, nytApi, pageIndex);
				if(jsonResponse != null){
					try{
						json = new JSONObject(jsonResponse);
						news.addAll(arrayFromOnePage(json, date));
					}
					catch(JSONException jsEx){
						jsEx.printStackTrace();
					}
				}
			}
		}
		return news;
	}

	//parses the json object for one page and writes the content in the file
	public void writeNewsOnOnePage(JSONObject json, BufferedWriter bufWriter) {
		if(json != null){
			try{
				JSONArray docsArr = json.getJSONObject("response").getJSONArray("docs");
				for (int docInd = 0 ; docInd < docsArr.length() ; docInd++){
					JSONObject doc = docsArr.getJSONObject(docInd);
					String snippet = "", headline = "";
					if(doc.has("snippet")){
						snippet = doc.getString("snippet");
					}
					if(doc.has("headline")){
						headline = doc.getJSONObject("headline").getString("main");
					}
					if(snippet.length() > 0 && headline.length() > 0){
						bufWriter.write("TITLE: "+headline+"\tDESCRIPTION: "+snippet+"\tSOURCE: NYT\n");
					}
					else{
						//System.out.println("Missing snippet or headline!!!");
					}
				}
			}
			catch(JSONException | IOException ex){
				ex.printStackTrace();
			}
		}
	}
	
	public ArrayList<NewsObject> arrayFromOnePage(JSONObject json, LocalDate date) {
		ArrayList<NewsObject> news = new ArrayList<NewsObject>();
		if(json != null){
			try{
				JSONArray docsArr = json.getJSONObject("response").getJSONArray("docs");
				for (int docInd = 0 ; docInd < docsArr.length() ; docInd++){
					JSONObject doc = docsArr.getJSONObject(docInd);
					String snippet = "", headline = "";
					if(doc.has("snippet")){
						snippet = doc.getString("snippet");
					}
					if(doc.has("headline")){
						headline = doc.getJSONObject("headline").getString("main");
						if (!isValid(headline))
							headline = "";
					}
					if(snippet.length() > 0 && headline.length() > 0 && !snippet.endsWith(":")){
						//bufWriter.write("TITLE: "+headline+"\tDESCRIPTION: "+snippet+"\tSOURCE: NYT\n");
						news.add(new NewsObject(headline, snippet, "NYT", date));
					}
					else{
						//System.out.println("Missing snippet or headline!!!");
					}
				}
			}
			catch(JSONException ex){
				ex.printStackTrace();
			}
		}
		return news;
	}


}