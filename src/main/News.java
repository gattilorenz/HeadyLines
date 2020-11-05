package main;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;

public class News implements Comparable<News>, java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3632899544241352658L;
	
	
	public String newsTitle;
	public String description;
	public double similarityWithQuote; //TODO: change this somehow
	public double maxDependency;
	public IndexedWord bestSlot;
	public NewsIngredient bestIngredient;
	public int indexSortedSimilarity;	
	//public int 
	
	public NewsIngredient[] ingredients; //lemma, pos, source, probability
	public Annotation annotation;
	public Annotation annotated_description;
	
	public int similarityIndex;
	public int dependencyIndex;

	public News(String title, String description, NewsIngredient[] ingredients) {
		this.newsTitle= title;
		this.description = description;
		this.ingredients = ingredients;
	}



	public static News[] readNews(String fileName) throws IOException {
		Path fFilePath = Paths.get(fileName);
		//News[] returnArray = null;
		if (!Files.exists(fFilePath) || Files.isDirectory(fFilePath)) {
			throw new IOException("File "+fFilePath.toString()+" doesn't exists (or is a directory)");
		}
		
		ArrayList<News> news = new ArrayList<News>();
		Charset ENCODING = StandardCharsets.UTF_8;  
		Scanner scanner;
		try {
			scanner = new Scanner(fFilePath, ENCODING.name());
		String currTitle = "";
		String currDesc = "";
		ArrayList<NewsIngredient> currIngredients = new ArrayList<NewsIngredient>();
		while (scanner.hasNextLine()){
			String line = scanner.nextLine();
			
			if (line.trim().isEmpty()) //skip empty lines
				continue;
			
			//titles end with \t\t
			//if (line.endsWith("\t\t")) {
			if (line.startsWith("TITLE:")) {
				//if it's not the first title, add the previous one
				if (!currTitle.isEmpty() && currIngredients.size()>0)
					news.add(new News(currTitle, currDesc, currIngredients.toArray(new NewsIngredient[currIngredients.size()])));
				String ingredientLine[] = line.split("\t");
				String tmpString = "TITLE: ";
				currTitle = ingredientLine[0].substring(tmpString.length()).trim();
				currTitle = currTitle.replaceAll("^(VIDEO|AUDIO): ", "");
				
				tmpString = "DESC: ";
				currDesc = ingredientLine[1].substring(tmpString.length()).trim();
				
				currIngredients.clear();
				continue;
			}
			
			String ingredientLine[] = line.split("\t");
			if (ingredientLine.length != 4) {
				System.out.println("Error: line '"+line+"' has too many fields (expected 4, found"+ingredientLine.length+"). SKIPPED!");
				continue;
			}
			currIngredients.add(new NewsIngredient(ingredientLine[0],ingredientLine[1],ingredientLine[2],Float.parseFloat(ingredientLine[3])));
		}
		scanner.close();		

		if (!currTitle.isEmpty() && currIngredients.size()>0)
			news.add(new News(currTitle, currDesc, currIngredients.toArray(new NewsIngredient[currIngredients.size()])));
		
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}		
		return news.toArray(new News[news.size()]);
	}



	@Override
	public int compareTo(News anotherNews) {
	    double anotherNewsProb = anotherNews.similarityWithQuote;
	    if ( this.similarityWithQuote > anotherNewsProb) return -1;
	    if ( this.similarityWithQuote < anotherNewsProb) return 1;
	    return 0;	
	}

}
