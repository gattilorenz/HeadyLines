package main;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class Slogan {

	/*private String slogan;
	private String brand;
	private String domain[];
	private Annotation annotation;*/
	
	public String slogan;
	public String brand;
	public String domain[];
	public Annotation annotation;
	
	public Slogan(String slogan, String brand, String[] domain) {
		this.slogan = slogan;
		this.brand = brand;
		this.domain = domain.clone();
	}

	/*
	public String getSlogan() {
		return slogan;
	}

	public String getBrand() {
		return brand;
	}

	public String[] getDomain() {
		return domain;
	}

	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}	

	public Annotation getAnnotation() {
		return annotation;
	}	
	*/
	
	public String toString() {
		return "\"" + slogan + "\";\"" + brand + "\";\"" + String.join(",", domain) + "\"";
	}
	
	
	

	private static Path fFilePath;
	private static Charset ENCODING = StandardCharsets.UTF_8;  	


	public static Slogan[] readSlogans(String fileName) throws IOException {
		fFilePath = Paths.get(fileName);
		Slogan[] slogans = null;
		if (Files.exists(fFilePath) && !Files.isDirectory(fFilePath))
			slogans = processLineByLine();
		else
			System.out.println("File "+fFilePath.toString()+" doesn't exists (or is a directory)");
		return slogans;
	}


	private final static Slogan[] processLineByLine() throws IOException {
		ArrayList<Slogan> slogans = new ArrayList<Slogan>();
		Scanner scanner =  new Scanner(fFilePath, ENCODING.name());
			while (scanner.hasNextLine()){
				Slogan newSlogan = processLine(scanner.nextLine());
				if (newSlogan == null)
					continue;
				slogans.add(newSlogan);
			}
		scanner.close();
		return slogans.toArray(new Slogan[slogans.size()]);
	}


	private static Slogan processLine(String aLine){
		//use a second Scanner to parse the content of each line 
		Scanner scanner = new Scanner(aLine);
		scanner.useDelimiter("\t");
		Slogan returnValue = null;
		String slogan = "";
		String domainsField = "";
		String brand = "";
		if (scanner.hasNext())
			//this is the line
			slogan = scanner.next();
		if (scanner.hasNext())	    
			brand = scanner.next();
		if (scanner.hasNext())	    
			domainsField = scanner.next(); //this is ;-separated
		String[] domain = domainsField.split(";");
		returnValue = new Slogan(slogan,brand,domain);
		scanner.close();
		return returnValue;
	}
	
	public static Quote[] convertSloganWithoutBrandsToQuotes(Slogan[] slogans) {
		ArrayList<Quote> quotesArray = new ArrayList<Quote>(slogans.length);
		for (int i = 0; i < slogans.length; i++) {
			Slogan currentSlogan = slogans[i];
			if (currentSlogan==null)
				continue;
			//skip slogans that contain the brand name (but first remove (1988) from brand names
			currentSlogan.brand = currentSlogan.brand.trim().replaceAll("\\s*\\(.*?\\)\\s*", "");
			currentSlogan.brand = currentSlogan.brand.trim().replaceAll("Coca-Cola - ", "");
			
			
			currentSlogan.brand = currentSlogan.brand.trim().toLowerCase();
			if (currentSlogan.slogan.toLowerCase().indexOf(currentSlogan.brand) >= 0)
				continue;
			
			//if we have something like Burn. Fire to drink. -> remove Burn. Slogans often start with the name of the product/brand in a single sentence
			//somtimes we lose "Yeah!" or "M'm!" - but who cares.
			if (currentSlogan.annotation!=null) {
				List<CoreMap> quoteSentences = currentSlogan.annotation.get(SentencesAnnotation.class);
				if (quoteSentences.size()>1) {
					List<CoreLabel> sentenceTokens = quoteSentences.get(0).get(TokensAnnotation.class);
					if (sentenceTokens.size() <= 3 && sentenceTokens.get(0).tag().startsWith("N"))
							System.out.println("Might be better removing this sentence: "+ quoteSentences.get(0).toString());
							quoteSentences.remove(0);
							currentSlogan.slogan = quoteSentences.toString().substring(1); //to remove initial [
							currentSlogan.slogan = currentSlogan.slogan.substring(0, currentSlogan.slogan.length()-2);
				}  
			}
			Quote sloganToQuote = new Quote(currentSlogan.slogan.trim());
			quotesArray.add(sloganToQuote);
		}
		Quote[] returnQuotes = new Quote[quotesArray.size()];
		for (int i = 0; i < returnQuotes.length; i++)
			returnQuotes[i] = quotesArray.get(i);
		return returnQuotes;
	}

}
