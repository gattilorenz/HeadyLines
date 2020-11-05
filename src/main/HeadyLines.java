package main;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;


public class HeadyLines {

	//Add debug info, or be REALLY VERBOSE
	static boolean debug = false;
	static boolean verbose = false;

	public static String expressionType = "reallyfamoussongs";

	static boolean readSerializedQuotes = true;

	final boolean onlySentLemma = false;

	//which PoS should be changed in the sentence
	final String wnPoStoConsider = "";


	//this activates or not the depscore (but you also have to set the threshold if you activate it)
	static boolean enableDepScore = true;
	//the dependency threshold: do not swap if depScore is below this
	static double depFilter =  1e-6; // 1e-5 in the short movies works better, 1e-6 for the slogans (but can be lowered to 1e-5)	

	//use only l b from headLine ingredients
	boolean similarityFromHeadline = true;

	//the min similarity of the word to be replaced in the quote with love#n
	final double minSimilarityWithDomain = 0.0; //loved#v is similar 0.53 with love#n, so it seems reasonable to set 0.5

	//the min probability for an ingredient to be considered
	public final static double minIngredientProb = 0.05;
	
	//get "topNHeadlines" most similar news
	static boolean sortAndSelectNews = true;
	static int 	  topNHeadlines = 10;

	//the threshold for rejecting a news (applies only if sortAndSelectNews is true)
	public static double quote_headlineSimThreshold = 0.45; //0.60 for slogans and quotes

	public static Quote[] quotes;

	static String baseFolder;
	
	DBOperations dbOps_loc ;
	public HeadyLines() throws Exception{
       
		baseFolder = System.getProperty("user.dir");
		//if started by Tomcat/Eclipse, this will be broken, so unfortunately this has to be hardcoded
		if (baseFolder.contains("Eclipse")) {
			baseFolder = "/Users/lorenzo/Projects/HeadyLinesGUI";
		}
		System.out.println(baseFolder);
		
		
		dbOps_loc = new DBOperations();
		dbOps_loc.connect_Db("dependencies"); //TODO: set up configuration file for this kind of variables

		ObjectInputStream in;
		try {	
			in = new ObjectInputStream(new FileInputStream(baseFolder+"/resources/w2v_semantic_space.ser"));
			similarity = (Similarity) in.readObject();
			in.close();	
			System.out.println("Serialized semantic space loaded!");
		} catch (Exception e) {
			System.out.println("Error in deserializing the semantic space");
			e.printStackTrace();
		}
//		if (similarity == null) {
//			similarity = new Similarity(baseFolder+"/model-skip.bin");
//		}



		System.out.println("Trying to load serialized quotes...");
		File f = new File(baseFolder+"/resources/"+expressionType+".ser");
		if ( f.exists()) {
			quotes = deserializeQuotes(f.getAbsolutePath());
		}
		else {
			try {
				System.out.println("Failed, loading txt file instead...");
				quotes = Quote.readQuotes(f.getAbsolutePath().replaceFirst("\\.ser$", ".txt"));
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Could not load " + f.getAbsolutePath().replaceFirst("\\.ser$", ".txt"));
				throw e1;
			}
			parseQuotes(quotes);
			System.out.println("Trying to serialize output...");
			serializeQuotes(quotes, f.getAbsolutePath());
			System.out.println("Serialized quotes at "+f.getAbsolutePath());
		}


		if ((quotes == null) || (quotes.length == 0)) {
			System.out.println("No quotes loaded!");
			Exception e = new Exception("No quotes loaded");
			throw e;
		}		
	}

	private static Parsing parser;
	private static Similarity similarity;

	private Random rnd = new Random();

	/**
	 * Currently only used to test if the system can find all the files it needs and if
	 * the connection with the MySQL database works. No headlines are generated from here!
	 * @param args Unused
	 * @throws Exception Will throw exceptions if
	 * 					<ul> 
	 * 				      <li> can't connect to MySQL "dependencies" db </li>
	 * 					  <li> "quote file" is not found </li>
 	 *					  <li> semantic space is not found </li>
 	 *					</ul>
	 */
	public static void main(String[] args) throws Exception {

		@SuppressWarnings("unused")
		final HeadyLines headyLines = new HeadyLines();


		if ((quotes == null) || (quotes.length == 0)) {
			System.out.println("No quotes loaded!");
			return;
		}

	}




	/**
	 * Creates a serialized list of quotes to speed up loading times.
	 * Quotes are already pre-parsed and thus the CoreNLP pipeline need not to run.
	 * @param quotes the quotes to serialize
	 * @param quotesFileName the output file
	 */
	public void serializeQuotes(Quote[] quotes, String quotesFileName) {
		ObjectOutputStream out;
		try {		
			out = new ObjectOutputStream(new FileOutputStream(quotesFileName));
			out.writeObject(quotes);
			out.flush();
			out.close(); 	        
		} catch (IOException e) {
			e.printStackTrace();
		}			

	}



	public Quote[] deserializeQuotes(String quotesFileName) {
		System.out.println("Loading serialized "+quotesFileName);
		ObjectInputStream in;
		try {	
			in = new ObjectInputStream(new FileInputStream(quotesFileName));
			Quote[] quotes = (Quote[]) in.readObject();
			in.close();	
			System.out.println("Serialized "+expressionType+" loaded!");
			return quotes;
		} catch (Exception e) {
			System.out.println("Error in deserializing "+expressionType);
			e.printStackTrace();
			return null;
		}

	}


	/**
	 * Calculate a probability score for the replacement of a word in an expression with a candidate.
	 * @param dependencies the dependency parse of the original expression
	 * @param slotNumber a number representing which word has to be replaced
	 * @param candidateWord the candidate word for replacement
	 * @return a score representing how grammatical the replacement would be
	 */
	public double getDependencyScore4Replacement(SemanticGraph dependencies, int slotNumber, String candidateWord){
		double sumOfLogProb = 0;
		int numOfRelns = 0;

		for(SemanticGraphEdge edge : dependencies.edgeIterable())
		{
			IndexedWord dep = edge.getDependent();
			IndexedWord gov = edge.getGovernor();
			GrammaticalRelation relation = edge.getRelation();
			//System.out.println("Relation: "+relation.toString()+" Dependent ID: "+dep.index()+" Dependent: "+dep.word()+ " pos: " +dep.tag()+" Governor ID: "+gov.index()+" Governor: "+gov.word()+ " pos: "+gov.tag());
			double prob = 0;

			//candidateWord and dep.word are swapped, depending whether we are considering the head or the modifier
			//for replacement
			try {
				if(slotNumber == dep.index()){
					//String reln, String dep, String dep_pos, String gov, String gov_pos
					prob = dbOps_loc.getProb(relation.toString(), candidateWord, dep.tag(), gov.lemma(), gov.tag());
				}
				else if (slotNumber == gov.index()){
					prob = dbOps_loc.getProb(relation.toString(), dep.lemma(), dep.tag(), candidateWord, gov.tag());
				}
			} catch (SQLException e) {
				//avoid printing encoding problems
				if (debug && !e.getMessage().startsWith("Illegal mix of collations")) 
				{
					System.out.println("Error with sentence: "+dependencies.toCompactString());
					if(slotNumber == dep.index())
						System.out.println("Parameters: candidateword="+candidateWord +" gov.lemma="+ gov.lemma());
					if (slotNumber == gov.index())
						System.out.println("Parameters: candidateword="+candidateWord +" dep.lemma="+ dep.lemma());
					e.printStackTrace();
				}
				else
					if (verbose)
						System.out.println("Error with sentence: "+dependencies.toCompactString());
			}
			if(prob!=0){
				sumOfLogProb += Math.log(prob);
				numOfRelns++;
			}
		}	

		if (debug) 
			System.out.println("numOfRelns: "+numOfRelns);
		return Math.exp(sumOfLogProb/numOfRelns);
	}

	/**
	 * Calculate a probability score for adding a candidate ADJ as a modifier to a noun in an existing sentence
	 * @param dependencies the dependency parse of the original expression
	 * @param slotNumber a number representing the noun to which the ADJ has to be added
	 * @param candidateAdj the candidate adjective
	 * @return a score representing how grammatical the relation JJ -> amod -> NN is 
	 */
	public double getDependencyScore4Adj(SemanticGraph dependencies, int nounSlotNumber, String candidateAdj){
		double sumOfLogProb = 0;
		int numOfRelns = 0;

		for(SemanticGraphEdge edge : dependencies.edgeIterable())
		{
			IndexedWord dep = edge.getDependent();
			IndexedWord gov = edge.getGovernor();
			GrammaticalRelation relation = edge.getRelation();
			double prob = 0;

			//candidateWord and dep.word are swapped, depending whether we are considering the head or the modifier
			//for replacement
			try {
				if(nounSlotNumber == dep.index()){
					//String reln, String dep, String dep_pos, String gov, String gov_pos
					prob = dbOps_loc.getProb(relation.toString(), candidateAdj, dep.tag(), gov.lemma(), gov.tag());
					//TODO: maybe make code more flexible so that NN + NN or JJ + NN are counted together?
					prob = dbOps_loc.getProb("amod", candidateAdj, "JJ", gov.lemma(), gov.tag());
				}
				else if (nounSlotNumber == gov.index()){
					prob = dbOps_loc.getProb(relation.toString(), dep.lemma(), dep.tag(), candidateAdj, gov.tag());
				}
			} catch (SQLException e) {
				if (verbose && !e.getMessage().startsWith("Illegal mix of collations")) //avoid printing encoding problems
				{
					System.out.println("Error with sentence: "+dependencies.toCompactString());
					if(nounSlotNumber == dep.index())
						System.out.println("Parameters: candidateword="+candidateAdj +" gov.lemma="+ gov.lemma());
					if (nounSlotNumber == gov.index())
						System.out.println("Parameters: candidateword="+candidateAdj +" dep.lemma="+ dep.lemma());
					e.printStackTrace();
				}
				else
					if (verbose)
						System.out.println("Error with sentence: "+dependencies.toCompactString());
			}
			if(prob!=0){
				sumOfLogProb += Math.log(prob);
				numOfRelns++;
			}
		}	

		//if (debug)
		//	System.out.println("numOfRelns: "+numOfRelns);
		return Math.exp(sumOfLogProb/numOfRelns);
	}


	public void parseQuotes(Quote[] quotes)  {
		if (parser==null)
			parser = new Parsing();
		for (int i = 0; i<quotes.length; i++) {
			Quote currentQuote = quotes[i];
			Annotation annotated_quote = parser.parseSentence( currentQuote.quote );
			currentQuote.annotation = annotated_quote;
			if (!verbose)
				continue;
			List<CoreMap> sentences = annotated_quote.get(SentencesAnnotation.class);
			for(CoreMap sentence: sentences) {
				SemanticGraph dependencies1 = sentence.get(BasicDependenciesAnnotation.class);
				System.out.println("Sentence: "+sentence.toString());

				//print all the dependencies:
				for(SemanticGraphEdge edge : dependencies1.edgeIterable())
				{
					IndexedWord dep = edge.getDependent();
					IndexedWord gov = edge.getGovernor();
					GrammaticalRelation relation = edge.getRelation();
					System.out.println("Relation: "+relation.toString()+"\tDependent ID: "+dep.index()+"\tDependent: "+dep.word()+ " tag: " +dep.tag()+" Governor ID: "+gov.index()+" Governor: "+gov.word());
				}
			}
		}

	}	

	/**
	 * Returns an array with same size as news, in which each element is an array
	 * of NewsIngredients coming from that news with the right PoS, excluding the ones with a high (>0.1)
	 * probability scores or derived from a word with a high probability score (>0.2)
	 * @param news the array of news
	 * @param wnPoStoChange which PoS should be considered
	 * @return
	 */
	public ArrayList<ArrayList<NewsIngredient>> getAppropriateIngredients (News[] news, String wnPoStoChange){
		if (news==null)
			return null;
		ArrayList<ArrayList<NewsIngredient>> ingredientsFromNews = new ArrayList<ArrayList<NewsIngredient>>();
		for (int newsIndex = 0; newsIndex<news.length; newsIndex++) {
			ArrayList<NewsIngredient> nounsFromIngredient = new ArrayList<NewsIngredient>();
			if (verbose) {
				System.out.println("");
				System.out.println("news:" + news[newsIndex].newsTitle + " - " + news[newsIndex].description);
			}
			ingredientsloop: //label to continue from internal loops
				for (int  ingIndex = 0; ingIndex < news[newsIndex].ingredients.length; ingIndex++) {

					NewsIngredient ingredient =  news[newsIndex].ingredients[ingIndex];

					//remove crap from FreeBase
					if (ingredient.type.contains("/")) {
						if (ingredient.type.matches("notable_")) {
							if (ingredient.lemma.equals("City") || 
									ingredient.lemma.equals("Town") || 
									ingredient.lemma.equals("Village") ||
									ingredient.lemma.equals("Person") ||
									ingredient.lemma.equals("Organization")
									)
								continue;				
							ingredient.lemma = ingredient.lemma.toLowerCase();
							//if it's from freebase it's (probably)accurate, unexpected and important, so change its probability
							ingredient.probability = (float) 0.00001;
						}
					}


					//skip if ingredient has no wnPOS
					if (ingredient.wnPOS == null)
						continue;

					//skip if we need to find one specific PoS and this is not it
					if (!wnPoStoChange.equals("") && !ingredient.wnPOS.equals(wnPoStoChange))
						continue;

					//check that the ingredient is not a derivation of a really really common ingredient
					if (ingredient.type.startsWith("derivation#") || ingredient.type.startsWith("synonym#")) {
						int indexOfFirstSep = ingredient.type.indexOf("#");
						String originalLemma = ingredient.type.substring(indexOfFirstSep+1,ingredient.type.indexOf("#", indexOfFirstSep+1));
						String originalwnPOS = ingredient.type.substring(ingredient.type.indexOf("#", indexOfFirstSep+1)+1);
						//for (int  derivIndex = 0; derivIndex < ingIndex ; derivIndex++) {
						for (int  derivIndex = 0; derivIndex < news[newsIndex].ingredients.length ; derivIndex++) {
							if (derivIndex==ingIndex)
								continue;
							NewsIngredient originalIngredient = news[newsIndex].ingredients[derivIndex];
							//if the ingredient is derived from a common one 
							//we do not add it and we continue the external loop
							if (originalIngredient.wnPOS.equals(originalwnPOS) && originalIngredient.lemma.equals(originalLemma)) {
								if (verbose)
									if (originalIngredient.probability > minIngredientProb) {
										System.out.println(ingredient.lemma + "#" + ingredient.wnPOS + " derives from " +originalLemma+"#"+originalwnPOS + " and will be removed");
										continue ingredientsloop; //this feels so much like a GOTO, and I'm proud of it
									}
							}
						}
					}

					//add only "rare" ingredients
					if (ingredient.probability > minIngredientProb)
						continue;

					//skip ingredients that are not in the database
					if (ingredient.probability  < 0)
						continue;						
					nounsFromIngredient.add(ingredient);
				}

			if (nounsFromIngredient.size() > 0) 
				//sort ingredients from lowest to higher probability
				Collections.sort(nounsFromIngredient);

			//add to the array of ingredients
			ingredientsFromNews.add(nounsFromIngredient);
		}
		return ingredientsFromNews;
	}

	public void findQuoteForNews(Quote[] quotes, News[] news, String posToChange) {

		BufferedWriter writer = null;
		try {
			File debugFile = new File("debug_"+expressionType+".txt");
			System.out.println("Saving debug output in "+debugFile.getCanonicalPath());
			writer = new BufferedWriter(new FileWriter(debugFile));

			//iterate over all the news
			ArrayList<ArrayList<NewsIngredient>> newsWithAppropriateIngredients = getAppropriateIngredients(news,posToChange);
			for (int newsIndex = 0; newsIndex < news.length; newsIndex++) {
				News currentNews = news[newsIndex];
				Quote[] similarQuotes = null;
				if (sortAndSelectNews)
					similarQuotes = findNMostSimilarQuotes(currentNews, quotes, topNHeadlines);
				else
					similarQuotes = quotes;
				if (similarQuotes == null) {
					if (debug) {
						writer.write("=======================================================\n");
						writer.write("No quotes above threshold for NEWS:\n");
						writer.write(currentNews.newsTitle+"\n");
					}
					continue;
				}

				writer.write("=======================================================\n");
				writer.write("Current title: "+currentNews.newsTitle+"\n");
				writer.write(currentNews.description+"\n");


				//get and iterate over the dependencies of quote


				//if we have ranked and then selected a subset of the quotes, print which ones
				if (similarQuotes[0].similarityWithNews>-1) {
					writer.write("Selected "+similarQuotes.length +" similar (>"+quote_headlineSimThreshold+") "+expressionType);
					if (debug) {
						writer.write(":\n");
						for (int i =0; i < similarQuotes.length; i++)
							writer.write(similarQuotes[i].similarityWithNews + " " + similarQuotes[i].quote+"\n");
					}
					else
						writer.write("\n");
				}		

				ArrayList<NewsIngredient> ingredients = newsWithAppropriateIngredients.get(newsIndex);

				for (int quoteIndex = 0; quoteIndex < similarQuotes.length; quoteIndex++) {
					Quote currentQuote = similarQuotes[quoteIndex];
					Annotation annotatedQuote = currentQuote.annotation ;
					List<CoreMap> quoteSentences = annotatedQuote.get(SentencesAnnotation.class);
					double quoteMaxDep = -1;
					NewsIngredient bestIngredient = null;
					IndexedWord bestSlot = null;
					for (CoreMap quoteSentence : quoteSentences) {
						SemanticGraph dependencies1 = quoteSentence.get(BasicDependenciesAnnotation.class);
						for(SemanticGraphEdge edge : dependencies1.edgeIterable())
						{
							IndexedWord dep = edge.getDependent();
							IndexedWord gov = edge.getGovernor();
							//get information about dependent
							String quoteDepPOS = dep.tag();
							String quoteDepWNPOS = WordnetUtils.POStoWNPOS(quoteDepPOS);

							//get information about governor
							String quoteGovPOS = gov.tag();
							String quoteGovWNPOS = WordnetUtils.POStoWNPOS(quoteGovPOS);

							for (NewsIngredient ingredient : ingredients) {
								double depScore = -1;
								double govScore = -1;
								//keep same PoS and avoid replacing X for X
								if (ingredient.wnPOS.equals(quoteDepWNPOS) && !ingredient.lemma.equals(dep.lemma())) {
									if (enableDepScore) 
										depScore = getDependencyScore4Replacement(dependencies1, dep.index(), ingredient.lemma);
									//if we are not using dependencies, just use a random number
									//THIS DOES *NOT* INFLUENCE THE RANKING WHEN
									//DEP IS DISABLED BUT SIM IS!
									else depScore = rnd.nextDouble() + 1; 
								}

								if (ingredient.wnPOS.equals(quoteGovWNPOS) && !ingredient.lemma.equals(gov.lemma())) {
									if (enableDepScore)
										govScore = getDependencyScore4Replacement(dependencies1, gov.index(), ingredient.lemma);
									else govScore = rnd.nextDouble() + 1; //if we are not using dependencies, just use a random number
								}

								if (depScore > quoteMaxDep) {
									quoteMaxDep = depScore;
									bestIngredient = ingredient;
									bestSlot = dep;					
								}
								if (govScore > quoteMaxDep) {
									quoteMaxDep = govScore;
									bestIngredient = ingredient;
									bestSlot = gov;
								}										

							}
						}	
					}
					//here we know the best for this news
					similarQuotes[quoteIndex].maxDependency = quoteMaxDep;
					if (bestSlot==null) {
						writer.write("No suitable PoS in "+expressionType+" "+similarQuotes[quoteIndex].quote+"\n");
					}
					similarQuotes[quoteIndex].bestSlot = bestSlot;
					similarQuotes[quoteIndex].bestIngredient = bestIngredient;
				}

				//sort the news array according to the depScore
				if (enableDepScore)
					Arrays.sort(similarQuotes, new Comparator<Quote>() {
						@Override
						public int compare(Quote o1, Quote o2) {
							return Double.compare(o2.maxDependency, o1.maxDependency);
						}
					});

				ArrayList<Quote> quotesWithSimilarityAndDependency = new ArrayList<Quote>(similarQuotes.length);

				//add the news that have a good dependency threshold and that still have content words left 
				int dependencyIndex = -1; 

				//otherwise sort by depIndex
				//if (enableDepScore) 
				for (int i = 0; i < similarQuotes.length; i++) { //the problem here is that if the first element is null
					if (similarQuotes[i].maxDependency > depFilter) {
						dependencyIndex++;
						similarQuotes[i].dependencyIndex = dependencyIndex;	
						quotesWithSimilarityAndDependency.add(similarQuotes[i]);	
					}
					else {
						if (enableDepScore && verbose)
							writer.write("\n"+similarQuotes[i].quote + " not above depfilter\n");
					}
				}

				if (quotesWithSimilarityAndDependency.size()==0) {
					writer.write("0 "+ expressionType +" have enough similarity and depScore (maxDepScore="+similarQuotes[0].maxDependency+") to be considered\n");
					continue;
				}
				writer.write("\nRemaining "+quotesWithSimilarityAndDependency.size()+" " + expressionType +"(sorted by depScore):\n");


				float bestModification = 100;					
				int bestIdx=0;

				//for (News similarNew : newsWithSimilarityAndDependency) {
				for (int i = 0; i < quotesWithSimilarityAndDependency.size(); i++) {
					Quote similarNew = quotesWithSimilarityAndDependency.get(i);
					float averagedIndices = (float) ((similarNew.dependencyIndex+1)+(i+1))/2;

					//if we are not using depscore then averagedIndices = 2*(similarityIndex+1)
					if (!enableDepScore)
						averagedIndices = (float) (2*(i+1));
					//if we are not using similarity scores then averagedIndices = 2*(depscoreIndex+1)
					if (!sortAndSelectNews)
						averagedIndices = (float) (2*(similarNew.dependencyIndex+1));

					//if both things are disabled, just pick a random index (so that order will be random)
					if (!enableDepScore && !sortAndSelectNews) {
						averagedIndices = rnd.nextFloat();
					}

					if (averagedIndices < bestModification) {
						bestIdx = i;
						bestModification = averagedIndices ;
					}
					String simpleScore = Double.toString(similarNew.maxDependency);
					simpleScore = simpleScore.replaceAll("\\.(\\d{2})\\d+", ".$1");
					writer.write(similarNew.quote + " (depScore="+simpleScore+") "+
							similarNew.bestSlot.lemma()+"#"+WordnetUtils.POStoWNPOS(similarNew.bestSlot.tag())+
							"->"+similarNew.bestIngredient.lemma+"#"+similarNew.bestIngredient.wnPOS+" "+
							" (" + similarNew.bestIngredient.type+"): ");
					writer.write(realizeOutput(similarNew.annotation, similarNew.bestSlot, similarNew.bestIngredient)+"\n");
				}


				Quote bestNews = quotesWithSimilarityAndDependency.get(bestIdx);
				writer.write("\nBest modification: " + realizeOutput(bestNews.annotation, bestNews.bestSlot,	bestNews.bestIngredient)+"\n");
				writer.write("(from \""+bestNews.quote+"\"\n");

				writer.flush();
				if (newsIndex%100==0) {
					System.out.println(newsIndex+"/"+news.length);
					System.out.flush();
				}


			}

		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Close the writer regardless of what happens...
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}


	public String realizeOutput(Annotation annotatedSentence, IndexedWord wordToReplace, NewsIngredient ingredientToUse) {
		List<CoreMap> quoteSentences = annotatedSentence.get(SentencesAnnotation.class);
		String out = "";
		for (CoreMap sentence : quoteSentences) {
			List<CoreLabel> quoteTokens = sentence.get(TokensAnnotation.class); 
			for (CoreLabel token : quoteTokens) {
				if (wordToReplace != null && token.index() == wordToReplace.index() && token.lemma()==wordToReplace.lemma()) {
					if (ingredientToUse.wnPOS.equals("a"))
						out = out + " " + ingredientToUse.lemma;
					else 
						try {
							//"go+v+indic+pres+sing3";
							String morphology = ingredientToUse.lemma.toLowerCase() + "+" + ingredientToUse.wnPOS;

							if (ingredientToUse.wnPOS.equals("v")){
								if (wordToReplace.tag().endsWith("B"))
									morphology = morphology + "+infin+pres";
								if (wordToReplace.tag().endsWith("D"))
									morphology = morphology + "+indic+past";
								if (wordToReplace.tag().endsWith("G"))
									morphology = morphology + "+gerund+pres";
								if (wordToReplace.tag().endsWith("N"))
									morphology = morphology + "+part+past";
								if (wordToReplace.tag().endsWith("Z"))
									morphology = morphology + "+indic+pres+sing3";
								if (wordToReplace.tag().endsWith("P"))
									morphology = morphology + "+indic+pres+no3sing";
							}
							else if (ingredientToUse.wnPOS.equals("n")){
								if (wordToReplace.tag().endsWith("S"))
									morphology = morphology + "+plur";
								else
									morphology = morphology + "+sing";
							}

							String MorphoProPath = baseFolder+"/resources/MorphoPro";
							String[] cmd = {
									"/bin/sh",
									"-c",
									"echo \"" + morphology + "\" | "+
											MorphoProPath+"/bin/fstan/x86_64/fstan -s "+
											MorphoProPath+"/models/english-utf8.fsa"
							};
							Process fstan = Runtime.getRuntime().exec(cmd);
							fstan.waitFor();						
							if (fstan.exitValue()!=0) {
								throw new Exception("Error with MorphoPro, exit value = "+fstan.exitValue());
							}
							InputStream i = fstan.getInputStream();
							byte[] b = new byte[16];
							i.read(b, 0, b.length);
							String inflectedForm = (new String(b)).trim();
							if (inflectedForm == null || inflectedForm.length()==0) {
								throw new Exception("No output from MorphoPro");								
							}							
							if (inflectedForm.startsWith("*")) {
								inflectedForm = ingredientToUse.lemma;
								inflectedForm.replaceAll("#np", "");
								if (verbose)
									System.out.println("----\nerror with "+morphology+" - using "+ingredientToUse.lemma+" instead\n----\n");
							}
							//TODO: what if we are replacing a proper noun?
							//if (wordToReplace.tag().startsWith("N") && wordToReplace.tag().contains("P"))

							//TODO: check for determiner in relations, not here. Here we replace all a/an up until now!
							//correct a/an if we are adding something with a vowel
							if (inflectedForm.matches("^[aeiouAEIOU]")) {
								out = out.replaceAll("\\ba$", "an");
								out = out.replaceAll("\\bA$", "An");
							}
							else {
								out = out.replaceAll("\\ban$", "a");
								out = out.replaceAll("\\bAn$", "A");								
							}

							out = out + " " + inflectedForm;
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
				else out = out + " " + token.originalText();
			}
		}
		//out.replaceAll(" '", "'");
		out = out.replaceAll(" (n[\\W]t)", "$1");
		out = out.replaceAll(" ([,'’.!?])", "$1");

		//System.out.println(out);
		return out;

		/*
		String wordToReplacePOS = wordToReplace.tag();
		if (ingredientToUse.wnPOS.equals("n")) {
			//create objects for realizing the new word
			WordElement token = lexicon.lookupWord(ingredientToUse.lemma,LexicalCategory.NOUN);

			boolean isUncountable = false;
			if (token.hasInflectionalVariant(Inflection.UNCOUNT))
				isUncountable = true;
			NPPhraseSpec noun = nlgFactory.createNounPhrase(token);				
			//pluralize if necessary
			if (wordToReplacePOS.endsWith("S"))
				noun.setPlural(true);
			boolean hasDeterminer = false;
			String originalModifier;
			List<CoreMap> quoteSentences = annotatedSentence.get(SentencesAnnotation.class);
			for (CoreMap quoteSentence : quoteSentences) {
				for(SemanticGraphEdge edge : quoteSentence.get(CollapsedDependenciesAnnotation.class).edgeIterable()){
					IndexedWord gov = edge.getGovernor();
					if (gov==wordToReplace) {
						String rel = edge.getRelation().toString();
						if (rel.equals("det") || rel.equals("poss")) { //also my/his, etc 
							hasDeterminer=true;
							break;
						}
						//if we have a modifier, remove it from the sentence
						if (rel.equals("amod")) {
							originalModifier = edge.getDependent().originalText();
							WordElement modifierToken = lexicon.lookupWord(originalModifier.toLowerCase(),LexicalCategory.ADJECTIVE);
							noun.addModifier(modifierToken);
						}
					}
				}
			}

			if (!hasDeterminer && !isUncountable)
				if (!noun.isPlural())
					noun.setDeterminer("a");
				else
					noun.setDeterminer("the");

			//			clause.setSubject(noun);


		}
	//		output = realiser.realiseSentence(clause);
		 */
	}

	public News[] findNMostSimilarNews(Quote currentQuote, News[] news, int N) {
		String[] quoteLP = new String[50]; //the array important tokens from the quote will go
		List<CoreMap> quoteSentences = currentQuote.annotation.get(SentencesAnnotation.class);
		int i = 0;
		for (CoreMap sentence : quoteSentences) {
			List<CoreLabel> quoteTokens = sentence.get(TokensAnnotation.class); 
			for (CoreLabel token : quoteTokens) {
				String quoteLemma = token.lemma().toLowerCase();
				String quotePOS = token.tag();
				String quoteWNPOS = WordnetUtils.POStoWNPOS(quotePOS);
				if (quotePOS.startsWith("NNP")) { //check for proper nouns
					quoteLemma = token.lemma();
					quoteWNPOS = "np";
				}
				if (Stopwords.isStopword(quoteLemma))
					continue;

				if (quoteWNPOS == null || quoteWNPOS.length()<1)  //skip punctuation and other crap
					continue;
				String quoteLemmaPOS=quoteLemma+"#"+quoteWNPOS;
				quoteLP[i] = quoteLemmaPOS;
				i++;
			}
		}
		//no useful lemmas in the quotes
		if (i == 0) {
			System.out.println("\""+ currentQuote.quote + "\" has only stopwords or words without wnpos");
			return null;
		}

		//iterate over the news, create vectors from ingredients and calculate similarity with quote

		ArrayList<News> newsArrayList = new ArrayList<News>(news.length);

		for (int j = 0; j<news.length; j++) {
			News currentNews = news[j];
			//currentNews.originalPosition = j;
			String[] newsLP = new String[currentNews.ingredients.length];
			i=0;
			for (int y = 0; y < currentNews.ingredients.length; y++) {

				NewsIngredient ingredient = currentNews.ingredients[y];

				//skip all the ingredients that do not come directy from the news and skip those containing a space (we can't do anything with them either)
				if (!ingredient.type.contains("sent_lemma") && !ingredient.type.equals("named_entity") || ingredient.lemma.contains(" "))
					continue;
				//skip stopwords... just to have some degree of comparability with the quotes, where I don't have prob to filter
				if (Stopwords.isStopword(ingredient.lemma)) 
					continue;
				newsLP[i] = ingredient.lemma+"#"+ingredient.wnPOS;
				if (ingredient.type.equals("named_entity") && ingredient.wnPOS.equals("n"))
					newsLP[i] = newsLP[i]+"p"; //named entities in similarity are #np
				i++;
			}
			//we got the two arrays, now measure similarity
			double newsQuoteSimilarity = 0;
			if (i>0)
				newsQuoteSimilarity = similarity.calculateSim2Sets(quoteLP, newsLP);
			//if there are no words from the quote in the similarity space then it's pointless to go on
			if (newsQuoteSimilarity==-1)
				return null;
			if (newsQuoteSimilarity >= quote_headlineSimThreshold) {
				currentNews.newsTitle = currentNews.newsTitle.replaceAll("^(VIDEO|AUDIO): ", ""); //too annoying to be permitted :D
				currentNews.similarityWithQuote = newsQuoteSimilarity;
				newsArrayList.add(currentNews);
			}

		}
		if (newsArrayList.size()==0)
			return null;
		Collections.sort(newsArrayList);
		if (N>newsArrayList.size())
			N = newsArrayList.size();
		News[] nSimilarNews = new News[N];
		for (int j = 0; j < N; j++) {
			nSimilarNews[j] = newsArrayList.get(j);
			nSimilarNews[j].indexSortedSimilarity = j;
		}

		return nSimilarNews;
	}

	//returns 0 if no, returns 1 if present in headline, returns 2 if derived from something present in headline
	public int isIngredientFromHeadline(NewsIngredient ingredient, News news) {
		Annotation annotated_headline = news.annotation;
		List<CoreMap> headlineSentences = annotated_headline.get(SentencesAnnotation.class);

		String[] derives = ingredient.type.split("#");
		String derivationType = null;
		String derivedLemma = ingredient.lemma;
		String derivedPOS = ingredient.wnPOS;
		if (derives.length==3) {
			derivationType = derives[0];
			derivedLemma = derives[1];
			derivedPOS = derives[2];
		}
		else if (derives.length==1)
			derivationType = ingredient.type;
		else
			System.out.println("isIngredientFromHeadline - derives.length==2");

		int returnValue = 0;

		for (CoreMap sentence : headlineSentences) {
			List<CoreLabel> newsTokens = sentence.get(TokensAnnotation.class);
			for (CoreLabel token: newsTokens) {
				String lemma = token.lemma().toLowerCase();
				String POS = token.tag();
				String wnPOS = WordnetUtils.POStoWNPOS(POS);
				//sent_lemma -> return 1
				if (lemma.equalsIgnoreCase(ingredient.lemma) && wnPOS.equals(ingredient.wnPOS) && derivationType.equals("sent_lemma"))
					return 1;
				//we have a derivedLemma -> return 2
				if (derivedLemma != null && lemma.equalsIgnoreCase(derivedLemma) && wnPOS.equals(derivedPOS))
					returnValue = 2;
			}
		}
		//return either 0 o 2
		return returnValue;
	}

	//returns 0 if no, returns 1 if present in description, returns 2 if derived from something present in description
	public int isIngredientFromDescription(NewsIngredient ingredient, News news) {
		Annotation annotated_headline = news.annotated_description;
		List<CoreMap> headlineSentences = annotated_headline.get(SentencesAnnotation.class);		
		String[] derives = ingredient.type.split("#");
		String derivationType = null;
		String derivedLemma = ingredient.lemma;
		String derivedPOS = ingredient.wnPOS;
		if (derives.length==3) {
			derivationType = derives[0];
			derivedLemma = derives[1];
			derivedPOS = derives[2];
		}
		else if (derives.length==1)
			derivationType = ingredient.type;
		else
			System.out.println("isIngredientFromHeadline - derives.length==2");

		int returnValue = 0;

		for (CoreMap sentence : headlineSentences) {
			List<CoreLabel> newsTokens = sentence.get(TokensAnnotation.class);
			for (CoreLabel token: newsTokens) {
				String lemma = token.lemma().toLowerCase();
				String POS = token.tag();
				String wnPOS = WordnetUtils.POStoWNPOS(POS);
				if (lemma.equalsIgnoreCase(ingredient.lemma) && wnPOS.equals(ingredient.wnPOS) && derivationType.equals("sent_lemma"))
					return 1;
				//if (derivedLemma != null && lemma.equalsIgnoreCase(derivedLemma) && wnPOS.equals(derivedPOS))
				if (derivedLemma != null && lemma.equalsIgnoreCase(derivedLemma) && wnPOS.equals(derivedPOS))
					returnValue = 2;
			}
		}
		return returnValue;
	}

	public Quote[] findNMostSimilarQuotes(News currentNews, Quote[] quotes, int N) {

		String[] newsLP = new String[currentNews.ingredients.length];//the array important tokens from the news is here
		int i = 0;
		System.out.println("Ingredients:"+currentNews.ingredients.length);

		for (int j = 0; j < currentNews.ingredients.length; j++) {

			NewsIngredient ingredient = currentNews.ingredients[j];
			//skip all the ingredients that do not come directy from the news and skip those containing a space (we can't do anything with them either)
			if (!ingredient.type.contains("sent_lemma") && !ingredient.type.equals("named_entity") || ingredient.lemma.contains(" "))
				continue;
			//skip stopwords... just to have some degree of comparability with the quotes, where I don't have prob to filter
			if (Stopwords.isStopword(ingredient.lemma)) 
				continue;
			newsLP[i] = ingredient.lemma+"#"+ingredient.wnPOS;
			if (ingredient.type.equals("named_entity") && ingredient.wnPOS.equals("n"))
				newsLP[i] = newsLP[i]+"p"; //named entities in similarity are #np
			i++;
		}
		//no useful lemmas in the headline (how come?)
		if (i == 0) {
			System.out.println("WARNING: \""+ currentNews.newsTitle + "\" has only stopwords or words without wnpos");
			return null;
		}		

		//iterate over the quotes, create vectors from ingredients and calculate similarity with quote
		ArrayList<Quote> quotesArrayList = new ArrayList<Quote>(quotes.length);		
		for (int j = 0; j<quotes.length; j++) {
			Quote currentQuote = quotes[j];
			//currentNews.originalPosition = j;
			i = 0;
			List<CoreMap> quoteSentences = currentQuote.annotation.get(SentencesAnnotation.class);
			String[] quotesLP = new String[currentQuote.annotation.get(TokensAnnotation.class).size()];
			for (CoreMap sentence : quoteSentences) {
				List<CoreLabel> quoteTokens = sentence.get(TokensAnnotation.class);
				for (CoreLabel token : quoteTokens) {
					String quoteLemma = token.lemma().toLowerCase();
					String quotePOS = token.tag();
					String quoteWNPOS = WordnetUtils.POStoWNPOS(quotePOS);
					if (quotePOS.startsWith("NNP")) { //check for proper nouns
						quoteLemma = token.lemma();
						quoteWNPOS = "np";
					}
					if (Stopwords.isStopword(quoteLemma))
						continue;

					if (quoteWNPOS == null || quoteWNPOS.length()<1)  //skip punctuation and other crap
						continue;
					String quoteLemmaPOS=quoteLemma+"#"+quoteWNPOS;
					quotesLP[i] = quoteLemmaPOS;
					i++;
				}
			}

			double newsQuoteSimilarity = 0;
			if (i>0)
				newsQuoteSimilarity = similarity.calculateSim2Sets(newsLP, quotesLP);
			//if there are no words from the news in the similarity space then it's pointless to go on
			if (newsQuoteSimilarity==-1)
				return null;
			if (newsQuoteSimilarity >= quote_headlineSimThreshold) {
				currentQuote.similarityWithNews = newsQuoteSimilarity;
				quotesArrayList.add(currentQuote);
			}
		}
		System.out.println("quotes over threshold ("+quote_headlineSimThreshold+"):"+quotesArrayList.size());
		if (quotesArrayList.size()==0)
			return null;
		Collections.sort(quotesArrayList);
		if (N>quotesArrayList.size())
			N = quotesArrayList.size();
		Quote[] nSimilarQuotes = new Quote[N];
		for (int j = 0; j < N; j++) {
			nSimilarQuotes[j] = quotesArrayList.get(j);
			nSimilarQuotes[j].indexSortedSimilarity = j;
		}
		return nSimilarQuotes;
	}	


	public Quote[] sortQuotesBySimilarity(News currentNews, Quote[] quotes, int N) {

		String[] newsLP = new String[currentNews.ingredients.length];//the array important tokens from the news is here
		int i = 0;
		System.out.println("Ingredients:"+currentNews.ingredients.length);

		for (int j = 0; j < currentNews.ingredients.length; j++) {

			NewsIngredient ingredient = currentNews.ingredients[j];
			//skip all the ingredients that do not come directy from the news and skip those containing a space (we can't do anything with them either)
			if (!ingredient.type.contains("sent_lemma") && !ingredient.type.equals("named_entity") || ingredient.lemma.contains(" "))
				continue;
			//skip stopwords... just to have some degree of comparability with the quotes, where I don't have prob to filter
			if (Stopwords.isStopword(ingredient.lemma)) 
				continue;
			newsLP[i] = ingredient.lemma+"#"+ingredient.wnPOS;
			if (ingredient.type.equals("named_entity") && ingredient.wnPOS.equals("n"))
				newsLP[i] = newsLP[i]+"p"; //named entities in similarity are #np
			i++;
		}
		//no useful lemmas in the headline (how come?)
		if (i == 0) {
			System.out.println("WARNING: \""+ currentNews.newsTitle + "\" has only stopwords or words without wnpos");
			return null;
		}		

		//iterate over the quotes, create vectors from ingredients and calculate similarity with quote
		ArrayList<Quote> quotesArrayList = new ArrayList<Quote>(quotes.length);		
		for (int j = 0; j<quotes.length; j++) {
			Quote currentQuote = quotes[j];
			//currentNews.originalPosition = j;
			i = 0;
			List<CoreMap> quoteSentences = currentQuote.annotation.get(SentencesAnnotation.class);
			String[] quotesLP = new String[currentQuote.annotation.get(TokensAnnotation.class).size()];
			for (CoreMap sentence : quoteSentences) {
				List<CoreLabel> quoteTokens = sentence.get(TokensAnnotation.class);
				for (CoreLabel token : quoteTokens) {
					String quoteLemma = token.lemma().toLowerCase();
					String quotePOS = token.tag();
					String quoteWNPOS = WordnetUtils.POStoWNPOS(quotePOS);
					if (quotePOS.startsWith("NNP")) { //check for proper nouns
						quoteLemma = token.lemma();
						quoteWNPOS = "np";
					}
					if (Stopwords.isStopword(quoteLemma))
						continue;

					if (quoteWNPOS == null || quoteWNPOS.length()<1)  //skip punctuation and other crap
						continue;
					String quoteLemmaPOS=quoteLemma+"#"+quoteWNPOS;
					quotesLP[i] = quoteLemmaPOS;
					i++;
				}
			}
			double newsQuoteSimilarity = 0;
			if (i>0)
				newsQuoteSimilarity = similarity.calculateSim2Sets(newsLP, quotesLP);
			//if there are no words from the news in the similarity space then it's pointless to go on
			if (newsQuoteSimilarity==-1) {
				System.out.println("returnig null in sortQuotesBySimilarity");
				return null;
			}
			currentQuote.similarityWithNews = newsQuoteSimilarity;
			quotesArrayList.add(currentQuote);
		}
		if (quotesArrayList.size()==0)
			return null;
		Collections.shuffle(quotesArrayList);
		if (N>quotesArrayList.size())
			N = quotesArrayList.size();
		Quote[] nSimilarQuotes = new Quote[N];
		for (int j = 0; j < N; j++) {
			nSimilarQuotes[j] = quotesArrayList.get(j);
			nSimilarQuotes[j].indexSortedSimilarity = j;
		}
		return nSimilarQuotes;
	}	


	private HashMap<String, HashMap<String, Float>> getReplacements (News selectedNews, Quote[] similarQuotes) {
		HashMap<String, HashMap<String, Float>> finalResults = new LinkedHashMap<String, HashMap<String, Float>>();
		NewsIngredient[] ingredients = selectedNews.ingredients;
		for (int quoteIndex = 0; quoteIndex < similarQuotes.length; quoteIndex++) {
			Quote currentQuote = similarQuotes[quoteIndex];
			Annotation annotatedQuote = currentQuote.annotation ;
			List<CoreMap> quoteSentences = annotatedQuote.get(SentencesAnnotation.class);
			double quoteMaxDep = -1;
			NewsIngredient bestIngredient = null;
			IndexedWord bestSlot = null;
			for (CoreMap quoteSentence : quoteSentences) {
				SemanticGraph dependencies1 = quoteSentence.get(BasicDependenciesAnnotation.class);
				for(SemanticGraphEdge edge : dependencies1.edgeIterable())
				{
					IndexedWord dep = edge.getDependent();
					IndexedWord gov = edge.getGovernor();
					//get information about dependent
					String quoteDepPOS = dep.tag();
					String quoteDepWNPOS = WordnetUtils.POStoWNPOS(quoteDepPOS);

					//get information about governor
					String quoteGovPOS = gov.tag();
					String quoteGovWNPOS = WordnetUtils.POStoWNPOS(quoteGovPOS);
					for (int ingIndex = 0; ingIndex < ingredients.length; ingIndex++) {
						NewsIngredient ingredient = ingredients[ingIndex];
						double depScore = -1;
						double govScore = -1;
						//keep same PoS and avoid replacing X for X
						if (ingredient.wnPOS.equals(quoteDepWNPOS) && !ingredient.lemma.equals(dep.lemma())) {
							depScore = getDependencyScore4Replacement(dependencies1, dep.index(), ingredient.lemma);
						}

						if (ingredient.wnPOS.equals(quoteGovWNPOS) && !ingredient.lemma.equals(gov.lemma())) {
							govScore = getDependencyScore4Replacement(dependencies1, gov.index(), ingredient.lemma);
						}

						if (depScore > quoteMaxDep) {
							quoteMaxDep = depScore;
							bestIngredient = ingredient;
							bestSlot = dep;					
						}
						if (govScore > quoteMaxDep) {
							quoteMaxDep = govScore;
							bestIngredient = ingredient;
							bestSlot = gov;
						}										

					}
				}	
			}
			//here we know the best for this news
			similarQuotes[quoteIndex].maxDependency = quoteMaxDep;
			if (bestSlot==null) {
				System.out.println("No suitable PoS in "+expressionType+" "+similarQuotes[quoteIndex].quote+"\n");
			}
			similarQuotes[quoteIndex].bestSlot = bestSlot;
			similarQuotes[quoteIndex].bestIngredient = bestIngredient;
		}

		//sort the news array according to the depScore
		if (enableDepScore)
			Arrays.sort(similarQuotes, new Comparator<Quote>() {
				@Override
				public int compare(Quote o1, Quote o2) {
					return Double.compare(o2.maxDependency, o1.maxDependency);
				}
			});


		ArrayList<Quote> quotesWithSimilarityAndDependency = new ArrayList<Quote>(similarQuotes.length);

		//add the news that have a good dependency threshold and that still have content words left 
		int dependencyIndex = -1; 

		for (int i = 0; i < similarQuotes.length; i++) { //the problem here is that if the first element is null
			if (similarQuotes[i].maxDependency > depFilter) {
				dependencyIndex++;
				similarQuotes[i].dependencyIndex = dependencyIndex;	
				quotesWithSimilarityAndDependency.add(similarQuotes[i]);	
			}
		}

		if (quotesWithSimilarityAndDependency.size()==0) {
			System.out.println("0 quotes have enough similarity and depScore (maxDepScore="+similarQuotes[0].maxDependency+") to be considered\n");
			//return null;
		}
		System.out.println("\nRemaining "+quotesWithSimilarityAndDependency.size()+" " + "quotes (sorted by depScore):\n");

		//		float bestModification = 100;					
		//int bestIdx=0;
		for (int i = 0; i < quotesWithSimilarityAndDependency.size(); i++) {
			Quote similarNew = quotesWithSimilarityAndDependency.get(i);
			float averagedIndices = (float) ((similarNew.dependencyIndex+1)+(i+1))/2;

			//			if (averagedIndices < bestModification) {
			//				bestIdx = i;
			//				bestModification = averagedIndices ;
			//			}
			String realizedOutput = realizeOutput(similarNew.annotation, similarNew.bestSlot, similarNew.bestIngredient);
			HashMap<String, Float> output = new HashMap<String, Float>();
			output.put(realizedOutput, Float.valueOf(averagedIndices));
			finalResults.put(similarNew.quote, output);
		}


		return finalResults;
	}

	private HashMap<String, HashMap<String, Float>> getInsertions (News selectedNews, Quote[] similarQuotes) {
		HashMap<String, HashMap<String, Float>> finalResults = new LinkedHashMap<String, HashMap<String, Float>>();
		NewsIngredient[] ingredients = selectedNews.ingredients;
		if (similarQuotes == null) {
			return null;
		}

		HashMap<Quote,Boolean> replacementPosition = new HashMap<Quote,Boolean>();

		for (int quoteIndex = 0; quoteIndex < similarQuotes.length; quoteIndex++) {
			Quote currentQuote = similarQuotes[quoteIndex];
			Annotation annotatedQuote = currentQuote.annotation ;
			List<CoreMap> quoteSentences = annotatedQuote.get(SentencesAnnotation.class);
			double quoteMaxDep = -1;
			NewsIngredient bestIngredient = null;
			IndexedWord bestSlot = null;
			boolean insertBefore = true;
			for (CoreMap quoteSentence : quoteSentences) {
				SemanticGraph dependencies1 = quoteSentence.get(BasicDependenciesAnnotation.class);
				for(SemanticGraphEdge edge : dependencies1.edgeIterable())
				{
					IndexedWord dep = edge.getDependent();
					IndexedWord gov = edge.getGovernor();

					for (int ingIndex = 0; ingIndex < ingredients.length; ingIndex++) {
						NewsIngredient ingredient = ingredients[ingIndex];
						double depScore = -1;
						double govScore = -1;

						String relation = edge.getRelation().toString();
						Pair<Double,Boolean> govPair = getScoreForInsertedWord(gov,ingredient, relation, quoteSentence); 
						Pair<Double,Boolean> depPair = getScoreForInsertedWord(dep,ingredient, relation, quoteSentence);
						govScore = govPair.score;
						depScore = depPair.score;

						if (depScore > quoteMaxDep) {
							quoteMaxDep = depScore;
							bestIngredient = ingredient;
							bestSlot = dep;	
							insertBefore = depPair.insertBefore;
						}
						if (govScore > quoteMaxDep) {
							quoteMaxDep = govScore;
							bestIngredient = ingredient;
							bestSlot = gov;
							insertBefore = govPair.insertBefore;
						}										

					}
				}	

			}
			//here we know the best for this news
			similarQuotes[quoteIndex].maxDependency = quoteMaxDep;
			if (bestSlot==null) {
				System.out.println("No suitable PoS in "+expressionType+" "+similarQuotes[quoteIndex].quote+"\n");
			}
			similarQuotes[quoteIndex].bestSlot = bestSlot;
			similarQuotes[quoteIndex].bestIngredient = bestIngredient;
			replacementPosition.put(similarQuotes[quoteIndex], insertBefore);
		}

		//sort the news array according to the depScore
		if (enableDepScore)
			Arrays.sort(similarQuotes, new Comparator<Quote>() {
				@Override
				public int compare(Quote o1, Quote o2) {
					return Double.compare(o2.maxDependency, o1.maxDependency);
				}
			});

		
		ArrayList<Quote> quotesWithSimilarityAndDependency = new ArrayList<Quote>(similarQuotes.length);

		//add the news that have a good dependency threshold and that still have content words left 
		int dependencyIndex = -1; 

		for (int i = 0; i < similarQuotes.length; i++) { //the problem here is that if the first element is null
			if (similarQuotes[i].maxDependency > depFilter) {
				dependencyIndex++;
				similarQuotes[i].dependencyIndex = dependencyIndex;	
				quotesWithSimilarityAndDependency.add(similarQuotes[i]);	
			}
		}

		if (quotesWithSimilarityAndDependency.size()==0) {
			System.out.println("0 quotes have enough similarity and depScore (maxDepScore="+similarQuotes[0].maxDependency+") to be considered\n");
			//return null;
		}
		System.out.println("\nRemaining "+quotesWithSimilarityAndDependency.size()+" " + "quotes (sorted by depScore):\n");

		for (int i = 0; i < quotesWithSimilarityAndDependency.size(); i++) {
			Quote similarNew = quotesWithSimilarityAndDependency.get(i);
			float averagedIndices = (float) ((similarNew.dependencyIndex+1)+(i+1))/2;

			Boolean insertBefore = replacementPosition.get(similarNew);
			String realizedOutput = realizeOutputForInsertion(similarNew.annotation, similarNew.bestSlot, insertBefore, similarNew.bestIngredient);
			HashMap<String, Float> output = new HashMap<String, Float>();
			output.put(realizedOutput, Float.valueOf(averagedIndices));
			finalResults.put(similarNew.quote, output);
		}

		return finalResults;
	}

	@SuppressWarnings("hiding")
	public class Pair<Double,Boolean> {
		public final Double score;
		public final Boolean insertBefore;

		public Pair(Double score, Boolean insertBefore) {
			this.score = score;
			this.insertBefore = insertBefore;
		}
	};


	private Pair<Double,Boolean> getScoreForInsertedWord (IndexedWord head, NewsIngredient ingredient, String relation, CoreMap sentence) {
		double returnScore = -1;
		String quoteHeadPOS = head.tag();
		String quoteHeadWNPOS = WordnetUtils.POStoWNPOS(quoteHeadPOS);
		Boolean insertBefore = true;
		try {
			//if INGREDIENT is an ADJ and we have a noun, try to add it in front
			if (ingredient.wnPOS.equals("a") && quoteHeadWNPOS.equals("n")  && !relation.equals("amod"))
				//TODO: check that exp+log are actually the same, or if the rounding changed the results
				//TODO: check that we do not have additional adjectives on this thing
				returnScore = Math.exp(Math.log(dbOps_loc.getProb("amod", ingredient.lemma, "JJ", head.lemma(), head.tag())));

			//if INGREDIENT is an ADV and we have an ADJ or a VERB, we add it in front too
			if (ingredient.wnPOS.equals("r") && (quoteHeadWNPOS.equals("a") || quoteHeadWNPOS.equals("v")) && !relation.equals("advmod"))
				returnScore = Math.exp(Math.log(dbOps_loc.getProb("advmod", ingredient.lemma, "RB", head.lemma(), head.tag())));

			//we can also try compound nouns
			if (ingredient.wnPOS.equals("n") && quoteHeadWNPOS.equals("n")) {
				//TODO: change this to work with WordNet instead
				//first make sure we do not have a noun in the front or in the back
				List<CoreLabel> quoteTokens = sentence.get(TokensAnnotation.class);
				if ( (!WordnetUtils.POStoWNPOS(quoteTokens.get(head.index()-1).tag()).equals("n")) &&
						(!WordnetUtils.POStoWNPOS(quoteTokens.get(head.index()+1).tag()).equals("n"))) {

					String compound_noun = null;
					if (WordnetUtils.isLemmaPOSInWordnet(ingredient.lemma+"_"+head.lemma(), head.tag()))
						compound_noun = ingredient.lemma+"_"+head.lemma();
					if (WordnetUtils.isLemmaPOSInWordnet(head.lemma()+"_"+ingredient.lemma, head.tag()))
						compound_noun = head.lemma()+"_"+ingredient.lemma;
					if (compound_noun != null) {
						System.out.println("COMPOUND NOUN: "+compound_noun);
					}
					//first argument: second part of the compound noun
					double ingredient_first = Math.exp(Math.log(dbOps_loc.getProbWithLikePOS("nn", head.lemma(), head.tag(), ingredient.lemma, "NN%")));
					double ingredient_last = Math.exp(Math.log(dbOps_loc.getProbWithLikePOS("nn", ingredient.lemma, "NN%", head.lemma(), head.tag())));
					if (ingredient_first > ingredient_last) {
						returnScore = ingredient_first;
					}
					else {
						returnScore = ingredient_last;
						insertBefore = false;
					}
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		Pair<Double,Boolean> returnValue = new Pair<Double, Boolean>(returnScore,insertBefore);
		return returnValue;
	}

	public String realizeOutputForInsertion(Annotation annotatedSentence, IndexedWord originalWord, boolean insertBefore, NewsIngredient ingredientToUse) {
		List<CoreMap> quoteSentences = annotatedSentence.get(SentencesAnnotation.class);
		String out = "";
		for (CoreMap sentence : quoteSentences) {
			List<CoreLabel> quoteTokens = sentence.get(TokensAnnotation.class); 
			for (CoreLabel token : quoteTokens) {
				if (originalWord != null && token.index() == originalWord.index() && token.lemma()==originalWord.lemma()) {
					String newLemma = ingredientToUse.lemma;
					String originalText = originalWord.originalText();
					if (insertBefore) {
						if (originalWord.index() == 1) { 
							//if we are inserting before the first word we make the new word uppercase
							newLemma = newLemma.substring(0, 1).toUpperCase() + newLemma.substring(1);
							//unless the old starting text is part of a named entity we put it in lowercase
							if (originalWord.get(NamedEntityTagAnnotation.class).equals("O")) {
								originalText = originalText.substring(0, 1).toLowerCase() + originalText.substring(1);
							}
						}
						out = out + newLemma+ " " + originalText;
					}
					else {
						out = out + originalText + " " + ingredientToUse.lemma;
					}
					continue;
				}
				out = out + " " + token.originalText() + " ";
			}
		}
		out = out.replaceAll(" (n[\\W]t)", "$1");
		out = out.replaceAll("  ", " ");
		out = out.replaceAll(" $", "");
		out = out.replaceAll("^ ", "");
		out = out.replaceAll(" ([,'’.!?])", "$1");

		return out;

	}

	public HashMap<String, Float> createHeadlines (News selectedNews, Quote[] similarQuotes) {
		HashMap<String, Float> finalResults = new HashMap<String, Float>();

		HashMap<String, HashMap<String, Float>> replacements = getReplacements(selectedNews,similarQuotes);
		HashMap<String, HashMap<String, Float>> insertions = getInsertions(selectedNews,similarQuotes);

		Set<String> sentences = new HashSet<String>();
		if (replacements != null)
			sentences.addAll(replacements.keySet());
		if (insertions!= null)
			sentences.addAll(insertions.keySet());
		//get the best between insertion and modification and add it to the results set
		//TODO: maybe we want to test grammaticality of the insertion, more than the final score
		for (String sentence : sentences) {
			String modification = null;
			Float value = -1f;
			if (replacements != null && replacements.containsKey(sentence)) {
				HashMap<String, Float> mod = replacements.get(sentence);
				for (String modifiedSentence : mod.keySet()) { 
					modification = modifiedSentence;
					value = mod.get(modifiedSentence);
				}
			}
			if (insertions != null && insertions.containsKey(sentence)) {
				HashMap<String, Float> mod = insertions.get(sentence);
				for (String modifiedSentence : mod.keySet()) { 
					if (mod.get(modifiedSentence) < value) { //lower is better here //TODO: redo this
						System.out.println("Sentence with insertion: "+modifiedSentence);
						modification = modifiedSentence;
						value = mod.get(modifiedSentence);
					}
				}
			}
			if (modification != null)
				finalResults.put(modification, value);
		}
		return finalResults;
	}


} //end of main



