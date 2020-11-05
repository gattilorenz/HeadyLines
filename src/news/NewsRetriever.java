package news;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import org.unbescape.html.HtmlEscape;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import main.HeadyLines;
import main.Stopwords;
import main.DBOperations;

public class NewsRetriever {
	QueryNYT nytQuerier = new QueryNYT();
	QueryRSS rssQuerier = new QueryRSS();

	
	Stopwords stopwords = new Stopwords();
	//StanfordParser stnParser ;
	StanfordCoreNLP pipeline;
	//Annotation annotation;
	WordnetChecker wnChecker;
	String nytApi = "8dc3e5f16738427aa6d263f6970f442e";
	HashMap<String, Float> lemmaProbs = new HashMap<String,Float>(); 
	HashMap<String, String> countriesDerivation; 
	class RelationInfoClass{
		int requiredPositionOfTw;
		String posOfNewWord;
		RelationInfoClass(int position, String pos){
			requiredPositionOfTw=position;
			posOfNewWord=pos;
		}
	}


	public NewsRetriever() throws Exception {
		wnChecker  = new WordnetChecker();
		//stnParser = new StanfordParser();
		Properties props = new Properties();

		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment");//, ner, parse");
		props.setProperty("ner.useSUTime", "false");
		props.setProperty("ner.applyNumericClassifiers", "false");
		props.setProperty("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
		pipeline = new StanfordCoreNLP(props);

		//SharedResources.fillInStopWords();
		//SharedResources.stopWords.addAll(
//				Arrays.asList("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday",
//						"january" , "february" , "march" , "april", "may",
//						"june", "july", "august", "september", "october",
//						"november", "december")
//				);
		fillInLemmaProbs();

		
		this.countriesDerivation   = new HashMap<String,String>()
		{
			private static final long serialVersionUID = -5381845296094892030L;
		{
			put("eu", "Europe#n");
			
			//from country to adj
			put("europe", "European#na");
			put("eu", "European#na");
			put("usa", "American#na");
			put("u.s.a.", "American#na");
			put("america", "American#na");
			put("italy", "Italian#na");
			put("germany", "German#na");
			put("france", "French#na");
			put("spain", "Spanish#na");
			put("uk", "British;English#na");
			put("u.k.", "British;English#na");
		    put("england", "British;English#na");
		    put("britain", "British;English#na");
		    put("russia", "Russian#na");
		    put("china", "Chinese#na");
		    put("austria", "Austrian#na");
		    put("portugal", "Portuguese#na");
		    
		    //from adj to country
		    put("american", "USA;America;U.S.A.#n");
			put("european", "Europe;EU#n");
			put("italian", "Italy#n");
			put("german", "Germany#n");
			put("french", "France#n");
			put("spanish", "Spain#n");
			put("british", "Britain;England;UK;U.K.#n");
			put("english", "Britain;England;UK;U.K.#n");
		    put("russian", "Russia#n");
		    put("chinese", "China#n");
		    put("austrian", "Austria#n");
		    put("portuguese", "Portugal#n");
		}};


	}

	NER ner = new NER();



	//the first parameter is the name of the relation and the second parameter is which parameter the target word is 

	public void fillInLemmaProbs() throws IOException{
		// Create Buffered/PrintWriter Objects
		BufferedReader lemmaProbsInputStream = new BufferedReader(new FileReader(StaticDefinitions.NYTlemmaProbsPath));
		String inLine = lemmaProbsInputStream.readLine(); //read first line: total doc count
		while((inLine = lemmaProbsInputStream.readLine())!=null){
			inLine = inLine.trim();
			if(inLine.length()>0){
				String lemma = inLine.split("Lemma: ")[1].split("existing doc count: ")[0].trim();
				float prob = Float.parseFloat(inLine.split("prob: ")[1].trim());
				lemmaProbs.put(lemma.toLowerCase(), prob);
			}
		}
		lemmaProbsInputStream.close();
		//System.out.println(lemmaProbs.get("year"));
		System.out.println("lemma probabilities loaded!");
	}

//	public String getDate(){
//		String dateStr = "";
//		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
//		dateStr = dateFormat.format(LocalDate.now());
//		System.out.println("Date:" + dateStr);
//		return dateStr; 
//	}

	public void extractLemmasWithHighProbs(NewsObject newsInfo, Annotation newsAnnotation, int numOfLemmasToConsider) throws Exception {
		HashMap<String, Float> lemmaProbsOfSnippet = new HashMap<String, Float>();
		ValueComparator lemma_comparator =  new ValueComparator(lemmaProbsOfSnippet);
		TreeMap<String, Float> sortedLemmaProbsOfSnippet = new TreeMap<String,Float>(lemma_comparator);

		if (newsAnnotation == null)
			newsAnnotation = new Annotation(newsInfo.description);
		pipeline.annotate(newsAnnotation);
		if (newsInfo.sentiment == null)
			newsInfo.sentiment = getMainSentiment(newsAnnotation);
		List<CoreMap> sentences = newsAnnotation.get(SentencesAnnotation.class);
		for(CoreMap tokenizedSentence: sentences) {
			//System.out.println("tokenizedSentence: "+ tokenizedSentence.toString());

			//ArrayList<TaggedWord> taggedWords = stnParser.POStag(parse);
			List<CoreLabel> tokens = tokenizedSentence.get(TokensAnnotation.class);
			for (CoreLabel tw : tokens){
				String lemma = wnChecker.lemmatize(tw.word(), tw.tag());
				//System.out.println("WORD: "+ tw.word()+" length : "+ tw.word().length() + " TAG: "+ tw.tag()+" LEMMA: "+lemma );

				if(lemma!=null){
					//skip stop words
					if(stopwords.is(lemma)){
						continue;
					}
					
					lemma = lemma.toLowerCase();
					String oneLetterPos =  WordUtils.convertToOneLetterPos(tw.tag());
					
					//add countries always
					if (countriesDerivation.containsKey(lemma)) {
						//add the current word!
						float prob = -1;
						if (lemmaProbs.containsKey(lemma))
							prob = lemmaProbs.get(lemma);						
						lemmaProbsOfSnippet.put(lemma+"#"+oneLetterPos, prob);
						
						String derived = countriesDerivation.get(lemma);
						String[] array = derived.split("#");
						if (array.length < 2)
							continue;
						String[] lemmasArray = array[0].split(";"); 
						char[] all_PoS = array[1].toCharArray();
						for (int i = 0; i < lemmasArray.length; i++) {
							for (int j = 0; j < all_PoS.length; j++) {
								//String derivedLP = lemmasArray[i] + "#" + all_PoS[j];
								//TODO: add this in the derivations
								prob = -1;
								if (lemmaProbs.containsKey(lemmasArray[i].toLowerCase()))
									prob = lemmaProbs.get(lemmasArray[i].toLowerCase());
								Ingredient countryDerivedIngredient = new Ingredient(prob, lemmasArray[i], String.valueOf(all_PoS[j]), "derivation#"+lemma, lemma);
								newsInfo.ingredients.add(countryDerivedIngredient);
							}
						}
						continue;
					}
					
					
					//skip proper nouns
					if (tw.tag().equals("NNP"))
						continue;
					
					if(!lemmaProbs.containsKey(lemma) || lemmaProbs.get(lemma)>HeadyLines.minIngredientProb){
						continue;
					}
					lemmaProbsOfSnippet.put(lemma+"#"+oneLetterPos, lemmaProbs.get(lemma));


				}
			}
		}
		sortedLemmaProbsOfSnippet.putAll(lemmaProbsOfSnippet);
		//System.out.println("SORTED LEMMAS*********************");

		int counter = 0;
		for(Map.Entry<String, Float> entry:sortedLemmaProbsOfSnippet.entrySet()){
			String [] splittedKey = entry.getKey().split("#");
			String lemma = splittedKey[0];
			String oneLetterPos = splittedKey[1];
			float prob = entry.getValue();
			//System.out.println("lemma: "+ lemma +" oneLetPos: "+oneLetterPos +" prob: "+prob);
			if(counter == numOfLemmasToConsider){
				break;
			}

			newsInfo.ingredients.add(new Ingredient(prob, lemma, oneLetterPos, StaticDefinitions.SENT_LEMMA));
			counter++;
		}
	}

	//adds lemmas of content words
	public Vector<Ingredient> extractContentWordsFromSent(String sent, StanfordParser stnParser, WordnetChecker wn) throws IOException {
		Vector<Ingredient> sentIngs = new Vector<Ingredient>();

		Annotation annotation = new Annotation(sent);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for(CoreMap tokenizedSentence: sentences) {
			System.out.println("tokenizedSentence: "+ tokenizedSentence.toString());

			List<CoreLabel> tokens = tokenizedSentence.get(TokensAnnotation.class);
			for (CoreLabel tw : tokens){
				String lemma = wn.lemmatize(tw.word(), tw.tag());

				//	System.out.println("WORD: "+ tw.word()+" length : "+ tw.word().length() + " TAG: "+ tw.tag()+" LEMMA: "+lemma );

				if(lemma!=null){
					if(stopwords.is(lemma)){
						//System.out.println("stop word!!!!!!");
						continue;
					}

					lemma = lemma.toLowerCase();

					String oneLetterPos =  WordUtils.convertToOneLetterPos(tw.tag());

					sentIngs.add(new Ingredient(lemma, oneLetterPos, StaticDefinitions.SENT_LEMMA));
					/*if(!lemma.equals(tw.word())){ //add the token as an ingredient only if it is not equal to lemma
						sentIngs.add(new Ingredient(tw.word(), oneLetterPos, StaticDefinitions.SENT_TOKEN, phonetics.getPronunciation(tw.word())));
					}*/
					//String newName, String newPos, String newRelation, String newPronunciation
				}
			}
		}
		return sentIngs;
	}


	public void processNews(int numOfLemmasToConsider, String freshOrSample, int maxQueryCount, String newsFile, String ingredientFile) throws Exception{
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(ingredientFile));

		//fetch the daily news if the argument is set to fresh
		if(freshOrSample.equals("fresh")){
			BufferedWriter newsWriter = new BufferedWriter(new FileWriter(newsFile));
			rssQuerier.query(newsWriter);
			nytQuerier.writeDailyNews(LocalDate.now(), maxQueryCount, nytApi, newsWriter);
			newsWriter.close();
		}

		BufferedReader bufReader = new BufferedReader(new FileReader(newsFile));

		String inLine = "";
		while((inLine = bufReader.readLine()) != null){
			//System.out.println("INPUT***********");
			if(inLine.length() > 0){
				//System.out.println("inLine: "+inLine);
				String [] splittedInLine = inLine.split("\t");
				String title = splittedInLine[0].split("TITLE: ")[1].trim();
				String desc = splittedInLine[1].split("DESCRIPTION: ")[1].trim();
				bufWriter.write(inLine + "\n");
				NewsObject newsInfo = new NewsObject(title,desc,"unknown",LocalDate.now());

				extractLemmasWithHighProbs(newsInfo, null, numOfLemmasToConsider);

				newsInfo.addSynonymsAndDerivations( lemmaProbs, wnChecker);
				//newsInfo.addHypernyms( wnChecker, phonetics);
				//newsInfo.addDerivations(lemmaProbs, wnChecker, phonetics);


				//extract the named entities and add the freebase information for each
				Vector<String> namedEntities = ner.extract(desc);
				for(String namedEntity:namedEntities){
					//System.out.println("namedEntity: "+namedEntity);
					Ingredient newIng = new Ingredient(namedEntity, "n", StaticDefinitions.NAMED_ENTITIY);
					newsInfo.ingredients.add(newIng);
					/*Vector<Ingredient> fbIngs = queryFB.getFBings(lemmaProbs, namedEntity, wnChecker);
					for(Ingredient fbIng:fbIngs){
					//	if(!newsInfo.ingredients.contains(fbIng)){
							newsInfo.ingredients.add(fbIng);
					//	}
					}
					 */
				}

				System.out.println("**************"); 
				newsInfo.printIngredients(bufWriter);
			}
		}
		bufWriter.close();
		bufReader.close();
	}


	public static void main(String[] args) throws Exception{
		NewsRetriever retr = new NewsRetriever();
		String freshOrSample = args[0]; //fresh for web retrieval, sample for stored sample json file
		int numOfLemmasToConsider = Integer.parseInt(args[1]); //the lemmas are sorted based on their probs, only a portion is considered
		//maximize the score, or it may drop lemmas :)
		String ingredientFile = args[2];
		String newsFile = args[3]; //"data/sample.json"
		int numOfQueries = Integer.parseInt(args[4]);
		retr.processNews(numOfLemmasToConsider, freshOrSample, numOfQueries, newsFile, ingredientFile);
	}

	//      1st check is in DB: if we have the date, we get the news from there
	//	    if not, we check if it is today's date, and
	//			if so, we download from NYT and BBC
	//			otherwise just NYT
	//		then save the results in the DB
	public ArrayList<NewsObject> retrieveNews(LocalDate newsDate) {
		ArrayList<NewsObject> returnedNews = new ArrayList<NewsObject>();
		DBOperations dbOps_loc = new DBOperations();
		dbOps_loc.connect_Db("news");
		try {
			ArrayList<NewsObject> newsOfDay = dbOps_loc.getNews(newsDate);
			if (!newsOfDay.isEmpty()) {
				returnedNews = cleanNewsArray(newsOfDay);
			} else {
				LocalDate now = LocalDate.now();
				boolean isToday =  newsDate.getYear() == now.getYear() && newsDate.getDayOfYear() == now.getDayOfYear(); 
				int maxQueryCount = 3;
				if (isToday) {
					newsOfDay.addAll(rssQuerier.query());
					newsOfDay.addAll(nytQuerier.getNewsByDate(newsDate, maxQueryCount, nytApi));
				}
				else {
					newsOfDay.addAll(nytQuerier.getNewsByDate(newsDate, maxQueryCount, nytApi));
				}

				returnedNews = cleanNewsArray(newsOfDay);
				dbOps_loc.putNews(returnedNews);
			}

		} catch (Exception pe) {
			pe.printStackTrace();
		}


		Collections.shuffle(returnedNews);
		return returnedNews;
	}

	public ArrayList<NewsObject> retrieveBookmarkedNews() {
		ArrayList<NewsObject> returnedNews = new ArrayList<NewsObject>(0);
		DBOperations dbOps_loc = new DBOperations();
		dbOps_loc.connect_Db("news");
		try {
			ArrayList<NewsObject> newsOfDay = dbOps_loc.getBookmarkedNews();
			if (newsOfDay.isEmpty())
				return returnedNews;
			returnedNews = cleanNewsArray(newsOfDay);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Collections.shuffle(returnedNews);
		return returnedNews;
	}

	private ArrayList<NewsObject> cleanNewsArray(ArrayList<NewsObject> newsOfDay) {
		ArrayList<NewsObject> returnedNews = new ArrayList<NewsObject>(newsOfDay.size());
		System.out.println("Cleaning news and adding sentiment score if not present");
		for (NewsObject todayNews : newsOfDay) {
			if (todayNews.headline.toUpperCase().startsWith("PAID NOTICE")) //skip obituaries on the NYT
				continue;
			todayNews.description = HtmlEscape.unescapeHtml(todayNews.description).trim().replaceAll("…", "...");
			
			if (todayNews.description.endsWith("..."))
				continue;
			if (todayNews.headline.contains("BBC"))
				continue;
			
			todayNews.description = todayNews.description.replaceAll("`", "'");
			todayNews.description = todayNews.description.replaceAll("’", "'");
			todayNews.description = todayNews.description.replaceAll("“", "\"");
			todayNews.description = todayNews.description.replaceAll("”", "\"");
			if (todayNews.sentiment == null) {
				//todayNews.setIngredients(fillIngredients(todayNews)); //redundant
				Annotation annotation = new Annotation(todayNews.description);
				pipeline.annotate(annotation); 
				todayNews.annotation = annotation;				
				todayNews.sentiment = getMainSentiment(annotation);		
			}
			returnedNews.add(todayNews);
		}
		System.out.println("Done");
		return returnedNews;
	}

	private int getMainSentiment(Annotation annotation) {
		int longest = 0;
		int mainSentiment = 0;
		for (CoreMap sent : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
			Tree tree = sent.get(SentimentAnnotatedTree.class);
			int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
			String partText = sent.toString();
			if (partText.length() > longest) {
				mainSentiment = sentiment;
				longest = partText.length();
			}
		}
		return mainSentiment;
	}


	public ArrayList<String>  findConceptsInHeadline(NewsObject news) {
		ArrayList<String> returnArray = new ArrayList<String>();
		try {
			if (news.annotation == null)
				news.annotation = new Annotation(news.description);
			pipeline.annotate(news.annotation);
			if (news.sentiment == null)
				news.sentiment = getMainSentiment(news.annotation);
			List<CoreMap> sentences = news.annotation.get(SentencesAnnotation.class);
			for(CoreMap tokenizedSentence: sentences) {
				System.out.println("tokenizedSentence: "+ tokenizedSentence.toString());


				List<CoreLabel> tokens = tokenizedSentence.get(TokensAnnotation.class);
				for (CoreLabel tw : tokens){
					String lemma = wnChecker.lemmatize(tw.word(), tw.tag());

					//NER is not used now
					int ContentType = 0; // 0 = stopword; 1 = content word; 2 = NER; 3 = unrecognized content word

					//System.out.println("WORD: "+ tw.word()+" length : "+ tw.word().length() + " TAG: "+ tw.tag()+" LEMMA: "+lemma );

					if(lemma!=null){
						//skip stop words BUT NOT proper nouns
						//if(SharedResources.stopWords.contains(lemma) || SharedResources.stopWords.contains(tw.word().toLowerCase()) ){//|| tw.tag().equals("NNP")){
						//	isContent = false;
						//}

						lemma = lemma.toLowerCase();

						if(!lemmaProbs.containsKey(lemma)){
							ContentType = 3;
						}
						else if (lemmaProbs.get(lemma) <= 0.05) {
							ContentType = 1;
						}


						if(stopwords.is("lemma")) {
							ContentType = 0;
						}

						String entity_type = tw.get(NamedEntityTagAnnotation.class);
						if (!entity_type.equals("O"))  {
							ContentType = 2;
						}

						String LemmaPos = tw.lemma()+"#"+WordUtils.convertToOneLetterPos(tw.tag());
						switch (ContentType) {
						case 1:	 returnArray.add(tw.word()+ "#" + LemmaPos + "#CONTENT"); break;
						case 2:	 returnArray.add(tw.word()+ "#" + tw.word() + "#" + "n" + "#" + entity_type+"#ENTITY"); break; //named entities apparently do not have a lemmapos?
						case 3:  returnArray.add(tw.word()+ "#" + LemmaPos + "#UNKNOWNCONTENT"); break;
						default: returnArray.add(tw.word()+ "#" + LemmaPos + "#STOP");
						}


					}
					else { //if lemma is null we add it as a stopword, unless it's a NE
						String entity_type = tw.get(NamedEntityTagAnnotation.class);
						if (!entity_type.equals("O"))  {
							ContentType = 2;
						}
						if (ContentType== 2)
							returnArray.add(tw.word()+"#" + entity_type+"#ENTITY");
						else returnArray.add(tw.word()+"#STOP");
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnArray;
	}

	public HashMap<String,String> getIngredients(NewsObject news) {
		news.description = HtmlEscape.unescapeHtml(news.description);
		HashMap<String,String> results = new HashMap<String,String>();
		try {
			news.ingredients.clear();
			extractLemmasWithHighProbs(news, null, 1000);
			news.addSynonymsAndDerivations( lemmaProbs, wnChecker);
		} catch (Exception e) {
			e.printStackTrace();
		}


		//extract the named entities and add the freebase information for each
		Vector<String> namedEntities = ner.extract(news.description);
		for(String namedEntity:namedEntities){
			Ingredient newIng = new Ingredient(namedEntity, "n", StaticDefinitions.NAMED_ENTITIY);
			news.ingredients.add(newIng);
		}
		for (Ingredient ing: news.ingredients){
			//System.out.println(ing.name+" "+ing.pos+" "+ing.relation+" prob: "+ing.lemmaProb);
			if (!results.containsKey(ing.name+"#"+ing.pos) && (ing.relation != "sent_lemma") && (ing.relation != "named_entity")) {
				String relation = "";

				if (ing.relation != "named_entity") {
					String PoS = "";
					if (ing.relation.contains("#a")) PoS = "the adjective ";
					if (ing.relation.contains("#n")) PoS = "the noun ";
					if (ing.relation.contains("#v")) PoS = "the verb ";
					if (ing.relation.contains("#r")) PoS = "the adverb ";
					ing.relation = ing.relation.replaceAll("#[nvar]$", "");

					if (ing.relation.startsWith("derivation#"))
						relation = "derived from " + PoS;
					if (ing.relation.startsWith("synonym#"))
						relation = "synonym of " + PoS;

					ing.relation = ing.relation.replaceFirst("derivation#", "");
					ing.relation = ing.relation.replaceFirst("synonym#", "");
					relation = relation + "</i>" + ing.relation + "<i>";
				}
				else {
					relation = "named entity"; 
				}


				results.put(ing.name+"#"+ing.pos, relation);
			}
			else {
				//remove sent-lemma because they are obvious
				if (ing.relation == "sent_lemma" || ing.relation == "named_entity") {
					results.remove(ing.name+"#"+ing.pos);
				}
			}
		}
		return results;
	}

	public ArrayList<String> getDatesInDB() {
		DBOperations dbOps_loc = new DBOperations();
		dbOps_loc.connect_Db("news");
		return dbOps_loc.getDates();
	}

	public void bookmarkNews(NewsObject article) {
		DBOperations dbOps_loc = new DBOperations();
		dbOps_loc.connect_Db("news");
		dbOps_loc.bookmark(article);
	}



}
 