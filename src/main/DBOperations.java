package main;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.time.format.DateTimeFormatter;

import news.NewsObject;


public class DBOperations {
	private Connection conn ;
	public Connection getConnection(){
		return conn;
	}
	
	public void connect_Db(String dbPath){
		if(conn!=null){
			disconnect_Db();
		}
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+dbPath+"?autoReconnect=true&useSSL=false", "headylines", "");
		}
		catch(Exception e) {
			//probably mysql is not running... let's start it
			System.out.println("Error connecting to the database.\nTrying to start MySQL server...");
			try {
				Runtime.getRuntime().exec("/usr/local/bin/mysql.server start");
				Thread.sleep(5000);
				conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+dbPath+"?autoReconnect=true&useSSL=false", "headylines", "");
				System.out.println("Connection succesful.");
			}
			catch(Exception e2) { // ClassNotFoundException, SQLException
				System.out.println("Error. Connection NOT succesful.");
				e2.printStackTrace();
			}
		}
	}

	public ArrayList<String> getMostUsedModifier(String head, String PTBPoS ) throws Exception{
		String headRel = "";
		String modifPTBPoS = "";
		if (PTBPoS==null || head==null)
			throw new Exception("PTBPoS or head are null");
		else if (PTBPoS.length()==1)
			throw new Exception("I need a PoS with a PennTreeBank format (VB/VBD/VBG/VBN/VBP/VBZ)");
		else if (PTBPoS.toLowerCase().startsWith("nn")) {
			headRel = "amod";
			modifPTBPoS = "JJ";
		}
		else if (PTBPoS.toLowerCase().startsWith("vb")){
			headRel = "advmod";
			modifPTBPoS = "RB";
		}
		else throw new Exception("I can only modify noun or verbs! You specified a PTBPoS = "+PTBPoS);

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select mod_lemma,counts from head_mod_counts where head_rel=? and head_POS=? and head_lemma=? and mod_POS=? order by counts desc;");			
			ps.setString(1, headRel);
			ps.setString(2, PTBPoS);
			ps.setString(3, head);
			ps.setString(4, modifPTBPoS);
			rs = ps.executeQuery();
			ArrayList<String> resultsToReturn = new ArrayList<String>();
			while (rs.next()) {
				String lemma = rs.getString("mod_lemma");
				resultsToReturn.add(lemma);
			}
			return resultsToReturn;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Close regardless of what happens...
			try {
				rs.close();
				ps.close();
				rs = null;
				ps = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;	

	}

	public ArrayList<String>  getModifierForTwoHeads(String head1, String head2, String headPTBPoS ) throws Exception{

		String headRel = "";
		String modifPTBPoS = "";
		if (headPTBPoS==null || head2==null || head1==null)
			throw new Exception("headWnPoS, head1 or head2 are null");
		else if (headPTBPoS.length()==1)
			throw new Exception("I need a PoS with a PennTreeBank format (VB/VBD/VBG/VBN/VBP/VBZ)");
		else if (headPTBPoS.toLowerCase().startsWith("nn")) {
			headRel = "amod";
			modifPTBPoS = "JJ";
		}
		else if (headPTBPoS.toLowerCase().startsWith("vb")){
			headRel = "advmod";
			modifPTBPoS = "RB";
		}
		else throw new Exception("I can only modify noun or verbs! You specified a headPTBPoS = "+headPTBPoS);

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select table1.mod_lemma from head_mod_counts as table1 join head_mod_counts as table2 on  table1.mod_lemma=table2.mod_lemma where table1.head_rel=? and table1.head_pos=? and table1.mod_pos=? and table1.head_lemma=? and table2.head_rel=? and table2.head_pos=? and table2.mod_pos=? and table2.head_lemma=?;");
			ps.setString(1, headRel); //advmod or amod
			ps.setString(2, headPTBPoS); //VB* or NN*
			ps.setString(3, modifPTBPoS); //JJ or RB
			ps.setString(4, head1); //first head
			ps.setString(5, headRel); //advmod or amod
			ps.setString(6, headPTBPoS); //VB* or NN*
			ps.setString(7, modifPTBPoS); //JJ or RB
			ps.setString(8, head2); //second head
			rs = ps.executeQuery();

			ArrayList<String> resultsToReturn = new ArrayList<String>();
			while (rs.next()) {
				String lemma = rs.getString("mod_lemma");
				resultsToReturn.add(lemma);
			}

			return resultsToReturn;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Close regardless of what happens...
			try {
				rs.close();
				ps.close();
				rs = null;
				ps = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;			
	}


	public double getProb(String reln, String dep, String dep_pos, String gov, String gov_pos ) throws SQLException{
		double count = 0;
		double denominator = 0;
		double prob = 0;

		PreparedStatement ps = conn.prepareStatement("select counts from head_mod_counts where head_rel=? and head_POS=? and head_lemma=? and mod_POS=? and mod_lemma=? ;");
		ps.setString(1, reln);
		ps.setString(2, gov_pos);
		ps.setString(3, gov);
		ps.setString(4, dep_pos);
		ps.setString(5, dep);
		ResultSet rs = ps.executeQuery();
		if(rs.next()){
			count = rs.getDouble("counts"); 
		}
		rs.close();
		rs=null;
		ps.close();
		ps=null;

		//System.out.println("count: "+count);
		PreparedStatement ps2 = conn.prepareStatement("select total_counts from relation_counts where head_rel=? ;");
		ps2.setString(1, reln);
		ResultSet rs2 = ps2.executeQuery();
		if(rs2.next()){
			denominator = rs2.getDouble("total_counts"); 
		}
		else{
			//System.err.println("relation "+reln+" doesn't exist in database");
			return 0;
		}

		//System.out.println("denominator: "+denominator);
		rs2.close();
		rs2=null;
		ps2.close();
		ps2=null;

		//System.out.println("Count+1: "+ (count+1));
		prob = (count+1)/denominator;
		//System.out.println("prob: "+prob);
		return prob;
	}

	public double getProbWithLikePOS(String reln, String dep, String dep_pos, String gov, String gov_pos ) throws SQLException{
		double count = 0;
		double denominator = 0;
		double prob = 0;

		PreparedStatement ps = conn.prepareStatement("select counts from head_mod_counts where head_rel=? and head_POS LIKE ? and head_lemma=? and mod_POS LIKE ? and mod_lemma=? ;");
		ps.setString(1, reln);
		ps.setString(2, gov_pos);
		ps.setString(3, gov);
		ps.setString(4, dep_pos);
		ps.setString(5, dep);
		ResultSet rs = ps.executeQuery();
		if(rs.next()){
			count = rs.getDouble("counts"); 
		}
		rs.close();
		rs=null;
		ps.close();
		ps=null;

		//System.out.println("count: "+count);
		PreparedStatement ps2 = conn.prepareStatement("select total_counts from relation_counts where head_rel=? ;");
		ps2.setString(1, reln);
		ResultSet rs2 = ps2.executeQuery();
		if(rs2.next()){
			denominator = rs2.getDouble("total_counts"); 
		}
		else{
			//System.err.println("relation "+reln+" doesn't exist in database");
			return 0;
		}

		//System.out.println("denominator: "+denominator);
		rs2.close();
		rs2=null;
		ps2.close();
		ps2=null;

		//System.out.println("Count+1: "+ (count+1));
		prob = (count+1)/denominator;
		//System.out.println("prob: "+prob);
		return prob;
	}

	public ArrayList<NewsObject> getNews(LocalDate newsDate) {
		ArrayList<NewsObject> returnedNews = new ArrayList<NewsObject>();
		try {
			String dateString = newsDate.format(DateTimeFormatter.ISO_DATE); //"yyyy-MM-dd"
			PreparedStatement ps = conn.prepareStatement("select * from news where newsdate=?;");
			ps.setString(1, dateString);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String headline = rs.getString("headline");
				String description = rs.getString("description");
				String source =  rs.getString("source");
				Integer sentiment =  rs.getInt("sentiment"); //returns 0 even if null
				if (rs.wasNull()) 
					sentiment = null;
				NewsObject retrieved = new NewsObject(headline, description, source, newsDate);
				retrieved.sentiment = sentiment;
				returnedNews.add(retrieved);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return returnedNews;
	}

	public ArrayList<String> getDates() {
		ArrayList<String> returnedNews = new ArrayList<String>();
		try {
			SimpleDateFormat db_DF = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat js_DF = new SimpleDateFormat("dd-MM-yyyy");

			PreparedStatement ps = conn.prepareStatement("select distinct newsdate from news;");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String DBdateString = rs.getString("newsdate");
				Date date = db_DF.parse(DBdateString);
				returnedNews.add(js_DF.format(date));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnedNews;

	}

	public void putNews( ArrayList<NewsObject> news) {
		try {
			PreparedStatement ps = conn.prepareStatement("insert into news (headline,description,source,newsdate,sentiment) values(?,?,?,?,?);");
			for (NewsObject myNewsElement : news) {
				ps.setString(1, myNewsElement.headline);
				ps.setString(2, myNewsElement.description);
				ps.setString(3, myNewsElement.source);
				ps.setString(4, myNewsElement.date.format(DateTimeFormatter.ISO_LOCAL_DATE));
				ps.setInt(5, myNewsElement.sentiment);
				ps.executeUpdate();
			}			
		} catch (SQLException e) {
			if (!e.toString().contains("Duplicate entry")) 
				e.printStackTrace();
			// we do not print the stacktrace because every duplicated news would print an annoyint ST
			// due to a constraint in the DB
		}	
	}

	public void disconnect_Db(){
		try{		
			conn.close();
			conn= null;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	
	public ArrayList<NewsObject> getBookmarkedNews() {
		ArrayList<NewsObject> returnedNews = new ArrayList<NewsObject>();
		try {
			PreparedStatement ps = conn.prepareStatement("select * from bookmarkednews;");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String headline = rs.getString("headline");
				String description = rs.getString("description");
				String source =  rs.getString("source");
				String dateString = rs.getString("newsdate");
				Integer sentiment =  rs.getInt("sentiment"); //returns 0 even if null
				if (rs.wasNull()) 
					sentiment = null;
				LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE); //yyyy-MM-dd
				NewsObject retrieved = new NewsObject(headline, description, source, date);
				retrieved.sentiment = sentiment;

				returnedNews.add(retrieved);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnedNews;
	}
	public void bookmark(NewsObject article) {
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO `bookmarkednews` (`headline`, `description`, `source`, `newsdate`, `sentiment`)"+
														 " VALUES (?, ?, ?, ?, ?);");
			ps.setString(1, article.headline);
			ps.setString(2, article.description);
			ps.setString(3, article.source);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String dateString = df.format(article.date);
			ps.setString(4, dateString);
			ps.setInt   (5, article.sentiment);
			ps.executeUpdate();
		}
		catch (Exception e) {
		}

	}


}
