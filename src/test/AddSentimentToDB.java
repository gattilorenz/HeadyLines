package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.unbescape.html.HtmlEscape;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class AddSentimentToDB {


	private static Connection conn ;
	public Connection getConnection(){
		return conn;
	}
	public static void connect_Db(String dbPath){
		if(conn!=null){
			disconnect_Db();
		}
		try{
			Class.forName("com.mysql.jdbc.Driver");
			try {
				conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+dbPath, "root", "");
			}
			catch(Exception e) {
				//probably mysql is not running... let's start it
				System.out.println("Error connecting to the database.\nTrying to start MySQL server...");
				Runtime.getRuntime().exec("/usr/local/bin/mysql.server start");
				Thread.sleep(5000);
				conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+dbPath, "root", "");
				System.out.println("Connection succesful.");
			}
		}
		catch(Exception e) { // ClassNotFoundException, SQLException
			System.out.println("Error. Connection NOT succesful.");
			e.printStackTrace();
		}
	}


	public static void disconnect_Db(){
		try{		
			conn.close();
			conn= null;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	

	public static void main(String[] args) {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");//, ner, parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);		
		System.err.println("CoreNLP Loaded!");
		connect_Db("news");
		String tableName = "bookmarkednews";
		try {
			PreparedStatement ps2 = conn.prepareStatement("update "+tableName+" set sentiment=? where ID=?;");
			PreparedStatement ps = conn.prepareStatement("select ID,description,sentiment from "+tableName+";");
			ResultSet rs = ps.executeQuery();
			int longest;
			int mainSentiment;
			int ID;
			while (rs.next()) {
				Integer sentiment =  rs.getInt("sentiment"); //returns 0 even if null
				if (rs.wasNull()) 
					sentiment = null;
				if (sentiment != null)
					continue;				
				ID = rs.getInt("ID");
				String description = rs.getString("description");
				
				description = HtmlEscape.unescapeHtml(description).trim().replaceAll("…", "...");
				description = description.replaceAll("`", "'");
				description = description.replaceAll("’", "'");
				description = description.replaceAll("“", "\"");	
				description = description.replaceAll("”", "\"");

				Annotation annotation = new Annotation(description);
				pipeline.annotate(annotation);
				longest = 0;
				mainSentiment = 0;
				for (CoreMap sent : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
					Tree tree = sent.get(SentimentAnnotatedTree.class);
					int tmpSentiment = RNNCoreAnnotations.getPredictedClass(tree);
					String partText = sent.toString();
					if (partText.length() > longest) {
						mainSentiment = tmpSentiment;
						longest = partText.length();
					}
				}
				
				ps2.setInt(2, ID);
				ps2.setInt(1, mainSentiment);
				ps2.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("updated everything");
	}		




}
