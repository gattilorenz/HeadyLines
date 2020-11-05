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

public class Quote implements Comparable<Quote>, java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3163336262030499055L;
	public String quote;
	public Annotation annotation;
	public double similarityWithNews;
	public int indexSortedSimilarity;
	public double maxDependency;
	public IndexedWord bestSlot;
	public NewsIngredient bestIngredient;
	
	public int dependencyIndex;	
	public int similarityIndex;
	
	public Quote(String quote) {
		this.quote = quote;
	}
	
	private static Path fFilePath;
	private static Charset ENCODING = StandardCharsets.UTF_8;  	


	public static Quote[] readQuotes(String fileName) throws IOException {
		fFilePath = Paths.get(fileName);
		Quote[] quotes = null;
		if (Files.exists(fFilePath) && !Files.isDirectory(fFilePath))
			try {
				quotes = processLineByLine();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		else {
			throw new IOException("File "+fFilePath.toString()+" doesn't exists (or is a directory)"); 
		
		}
		return quotes;
	}


	private final static Quote[] processLineByLine() throws IOException {
		ArrayList<Quote> quotes = new ArrayList<Quote>();
		Scanner scanner =  new Scanner(fFilePath, ENCODING.name());
			while (scanner.hasNextLine()){
				String quoteLine = scanner.nextLine();
				if (quoteLine == null || quoteLine.equals("") || quoteLine.startsWith("***"))
					continue;
				Quote newQuote = new Quote(quoteLine.trim());
				quotes.add(newQuote);
			}
		scanner.close();
		return quotes.toArray(new Quote[quotes.size()]);
	}	

	public int compareTo(Quote anotherQuote) {
	    double anotherQuoteProb = anotherQuote.similarityWithNews;
	    if ( this.similarityWithNews > anotherQuoteProb) return -1;
	    if ( this.similarityWithNews < anotherQuoteProb) return 1;
	    return 0;	
	}

	
}
