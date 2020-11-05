package news;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

public class QueryRSS {
	private boolean isValid(String headline) {
		if (headline.startsWith("VIDEO:")) return false;
		if (headline.startsWith("AUDIO:")) return false;
		if (headline.startsWith("Your pictures:")) return false;
		if (headline.contains("week in")) return false;
		if (headline.contains(": The week")) return false;
		if (headline.trim().endsWith("...")) return false;
		if (headline.trim().endsWith("?")) return false;
		if (headline.contains("pictures") || headline.contains("photos")) return false; //WHY SO MANY???
		return true;
	}

	public void query(BufferedWriter bufWriter) throws IOException {
		RSSFeedParser parser = new RSSFeedParser("http://feeds.bbci.co.uk/news/rss.xml");

		Feed feed = parser.readFeed();
		System.out.println(feed);
		for (FeedMessage message : feed.getMessages()) {
			if (isValid(message.getTitle())) {
				//System.out.println("MESSAGE******: "+message);
				bufWriter.write("TITLE: "+message.getTitle()+"\tDESCRIPTION: "+message.getDescription()+"\tSOURCE: BBC\n");
			}
		}
	}

	public ArrayList<NewsObject> query() throws IOException {
		RSSFeedParser parser = new RSSFeedParser("http://feeds.bbci.co.uk/news/rss.xml");
		Feed feed = parser.readFeed();
		ArrayList<NewsObject> news = new ArrayList<NewsObject>();
		for (FeedMessage message : feed.getMessages()) {
			if (isValid(message.getTitle()) && isValid(message.getDescription()))
				news.add(new NewsObject(message.getTitle(), message.getDescription(), "BBC", LocalDate.now()));
		}
		return news;
	}

} 