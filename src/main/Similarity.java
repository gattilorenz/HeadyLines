package main;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
 
public class Similarity implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2688386395928176580L;
	private int words = 0; 
	private int size = 0;
	private static int string_max_w = 50;
	
	private class SemanticSpace implements java.io.Serializable {
		private static final long serialVersionUID = -2310415211710358878L;
		LinkedHashMap<String, Integer> tokens;
		public float[][] features;
	}
	
	private SemanticSpace semantic_space;
	
	public Similarity(String SimilarityFile) {
		semantic_space = new SemanticSpace();
		loadSimilarityFile(SimilarityFile);
	}

	public LinkedHashMap<String,Float> getNMostSimilarWords(String lemmaPoS, int n) {
		Integer arrayIndex = semantic_space.tokens.get(lemmaPoS);
		if (arrayIndex==null) {
			System.out.println(lemmaPoS+" is not in the semantic space");
			return null;
		}
		float[] normalizedWordVector = semantic_space.features[arrayIndex];
		String[] keys = semantic_space.tokens.keySet().toArray(new String[words]);
		
		HashMap<String, Float> similarityValues = new HashMap<String, Float>(words);
		for (int i=0; i<words; i++) {
			float[] lexiconVal = semantic_space.features[i];			
			similarityValues.put(keys[i], calculateSim2Vectors(lexiconVal, normalizedWordVector)); 
		}
		similarityValues = sortByComparator(similarityValues);
		
		int i = 0;
		LinkedHashMap<String, Float> returnValues = new LinkedHashMap<String,Float>(n);
		for (Map.Entry<String,Float> entry : similarityValues.entrySet()) {
			if (lemmaPoS.equals(entry.getKey()))
				continue;
			returnValues.put(entry.getKey(), entry.getValue());
			i++;
			if (i==n)
				break;
		}
		
		return returnValues;
	}
	
	public LinkedHashMap<String,Float> getNMostSimilarWordsWithPOS(String lemmaPoS, String PoS, int n) {
		Integer arrayIndex = semantic_space.tokens.get(lemmaPoS);
		if (arrayIndex==null) {
			System.out.println(lemmaPoS+" is not in the semantic space");
			return null;
		}
		float[] normalizedWordVector = semantic_space.features[arrayIndex];
		String[] keys = semantic_space.tokens.keySet().toArray(new String[words]);
		
		HashMap<String, Float> similarityValues = new HashMap<String, Float>(words);
		for (int i=0; i<words; i++) {
			float[] lexiconVal = semantic_space.features[i];			
			similarityValues.put(keys[i], calculateSim2Vectors(lexiconVal, normalizedWordVector)); 
		}
		similarityValues = sortByComparator(similarityValues);
		
		int i = 0;
		LinkedHashMap<String, Float> returnValues = new LinkedHashMap<String,Float>(n);
		for (Map.Entry<String,Float> entry : similarityValues.entrySet()) {
			//avoid returning same lemma/pos 
			if (lemmaPoS.equals(entry.getKey()))
				continue;
			//skip if the word has not the lemmapos we need
			if (!entry.getKey().endsWith(PoS))
				continue;
			returnValues.put(entry.getKey(), entry.getValue());
			i++;
			if (i==n)
				break;
		}
		
		return returnValues;
	}
	
	
	public String stringOfNMostSimilarWords(String lemmaPos, int n) {
		LinkedHashMap<String, Float> similarValues = getNMostSimilarWords(lemmaPos,n);
		if (similarValues==null)
			return "";
		//System.out.println("The "+n+" most similar values to "+lemmaPos+" are:");
		String returnString = "The "+n+" most similar values to "+lemmaPos+" are:";
		for (Map.Entry<String,Float> entry : similarValues.entrySet()) {
			//System.out.println(entry.getKey() +" - "+ entry.getValue());
			returnString = returnString + "\n" + entry.getKey() +" - "+ entry.getValue();
		}
		return returnString;
	}
	
	
	public String stringOfNMostSimilarWordsWithPOS(String lemmaPos, String wnPOS, int n) {
		LinkedHashMap<String, Float> similarValues = getNMostSimilarWordsWithPOS(lemmaPos,wnPOS,n);
		if (similarValues==null)
			return "";
		//System.out.println("The "+n+" most similar #"+wnPOS+" to "+lemmaPos+" are:");
		String returnString = "The "+n+" most similar #"+wnPOS+" to "+lemmaPos+" are:";
		for (Map.Entry<String,Float> entry : similarValues.entrySet()) {
			//System.out.println(entry.getKey() +" - "+ entry.getValue());
			returnString = returnString + "\n" + entry.getKey() +" - "+ entry.getValue();
		}
		return returnString;
	}	
	
	
	public float calculateSim2Words(String lemmaPos1, String lemmaPos2){
		float sim=0;
		Integer arrayIndex = semantic_space.tokens.get(lemmaPos1);
		if (arrayIndex==null) {
			//System.out.println(lemmaPos1 + " is not present in the semantic space");
			//try the lower-case version
			arrayIndex = semantic_space.tokens.get(lemmaPos1.toLowerCase());
			if (arrayIndex==null)
				return 0;
		}
		float[] normalizedWordVector1 = semantic_space.features[arrayIndex];
		arrayIndex = semantic_space.tokens.get(lemmaPos2);
		if (arrayIndex==null) {
			//System.out.println(lemmaPos2 + " is not present in the semantic space");
			arrayIndex = semantic_space.tokens.get(lemmaPos2.toLowerCase());
			if (arrayIndex==null)
				return 0;			
			return 0;
		}		
		float[] normalizedWordVector2 = semantic_space.features[arrayIndex];

		if(normalizedWordVector1.length>0 && normalizedWordVector2.length>0){
			sim = calculateSim2Vectors(normalizedWordVector1, normalizedWordVector2);
		}
		return sim;
	}

	
	public float calculateSim2Sets(String[] wordsSet1, String[] wordsSet2) {
		if (wordsSet1==null || wordsSet2==null || wordsSet1.length==0 || wordsSet2.length==0) {
			System.out.println("calculateSim2Sets: wordsSet1 and wordsSet2 cannot be null or have 0 length");
			return (float)-1;
		}
		
		//for each word: try to get it from the similarity space, if not present try the lowercase version,
		//if not present try to skip it. If at the end there are no retrieved word, error message and return -1
		float [][] arraySet1 = new float[wordsSet1.length][size];
		int arraySetIndex1 = 0;
		for (int i=0; i<wordsSet1.length; i++) {
			String lemmaPos = wordsSet1[i];
			if (lemmaPos==null)
				continue;
			Integer arrayIndex = semantic_space.tokens.get(lemmaPos);
			if (arrayIndex==null) {
				//System.out.print("calculateSim2Sets: "+lemmaPos+" not in semantic space. ");
				arrayIndex = semantic_space.tokens.get(lemmaPos.toLowerCase());
				if (arrayIndex==null) {
					//System.out.println("Skipping it...");
					continue;
				}
				else {
					System.out.println("Using "+lemmaPos.toLowerCase()+" instead.");
				}
			}
			arraySet1[arraySetIndex1] = semantic_space.features[arrayIndex];
			arraySetIndex1++;
		}
				
		if (arraySetIndex1==0) {
			//System.out.print("calculateSim2Sets: no words from wordsSet1 were found in the semantic space. ");
			String words = "";
			for (int i=0; i<wordsSet1.length; i++) {
				if (wordsSet1[i]!=null)
					words = words + " " +wordsSet1[i];
			}
			System.out.println("Words were:" + words );
			return (float) -1;
		}
		
		//same thing for wordSet2
		float [][] arraySet2 = new float[wordsSet2.length][size];
		int arraySetIndex2 = 0;
		for (int i=0; i<wordsSet2.length; i++) {
			String lemmaPos = wordsSet2[i];
			if (lemmaPos==null)
				continue;			
			Integer arrayIndex = semantic_space.tokens.get(lemmaPos);
			if (arrayIndex==null) {
				//System.out.print("calculateSim2Sets: "+lemmaPos+" not in semantic space. ");
				arrayIndex = semantic_space.tokens.get(lemmaPos.toLowerCase());
				if (arrayIndex==null) {
					//System.out.println("Skipping it...");
					continue;
				}
				else {
					System.out.println("Using "+lemmaPos.toLowerCase()+" instead.");
				}
			}
			arraySet2[arraySetIndex2] = semantic_space.features[arrayIndex];
			arraySetIndex2++;
		}
		
		if (arraySetIndex2==0) {
			//System.out.println("calculateSim2Sets: no words from wordsSet2 were found in the semantic space. ");
			String words = "";
			for (int i=0; i<wordsSet2.length; i++) {
				if (wordsSet2[i]!=null)
					words = words + " " +wordsSet2[i];
			}
			System.out.println("Words were:" + words );			
			return (float) -2;
		}		

		//add the vectors toghether and return similarity
		float[] sumVector1 = addVectors(arraySet1);
		float[] sumVector2 = addVectors(arraySet2);
		float sim = calculateSim2Vectors(sumVector1, sumVector2);
		
		return sim;
	}
	
	public float[] normalizeVectorArr(float[] oneVectorArr){
		Vector<Float> normalizedVector = new Vector<Float>();
		float norm = (float)0;
		for (int i =0 ; i < oneVectorArr.length; i++){
			Float oneEntry = oneVectorArr[i];
			norm += oneEntry * oneEntry ;
		}
		norm = (float)Math.sqrt(norm);
		for (int i =0 ; i < oneVectorArr.length; i++){ 
			normalizedVector.add(oneVectorArr[i] / norm);
		}
		
		float[] returnVector = new float[normalizedVector.size()];
		for (int i=0; i< normalizedVector.size(); i++)
			returnVector[i] = normalizedVector.get(i).floatValue();
		return returnVector;
	}
	
	private float[] addVectors(float[][] vectors) {
		if (vectors == null || vectors.length==0)
			return null;
		int numOfVectors = vectors.length;
		float[] resultingVector = new float[size];
		for (int i=0; i<size; i++)
			resultingVector[i]=0;
		
		for (int word=0; word<numOfVectors; word++) {
			if (vectors[word]==null)
				continue;			
			for (int i=0; i<size; i++)
				resultingVector[i] += vectors[word][i] ;
		}
		
		return normalizeVectorArr(resultingVector);
	}

	private void loadSimilarityFile(String SimilarityFile) {
		long startTime = System.nanoTime();
		InputStream is = null;
		DataInputStream dis = null;
		try {
			is = new FileInputStream(SimilarityFile);		         
			// create new BUFFERED data input stream
			dis = new DataInputStream(new BufferedInputStream(is, 4*1024));

			//read the header with number of words and of features
			String tmp = "";
			while (dis.available()>0) {
				byte characterb = dis.readByte();
				char character = (char) characterb;
				if (character == ' ') {
					words = Integer.parseInt(tmp);
					tmp = "";
					continue;
				}
				if (character == '\n') {
					size = Integer.parseInt(tmp); 
					break;
				}
				tmp = tmp + character;				
			}

			System.out.println("Reading similarity file ("+words+" words with "+size+" dimensions each)");

			//initialize the arrays
			float[][] allFeatures = new float[words][size];
			String[] allWords = new String[words];
			
			// read till end of the file
			for (int b = 0; b < words; b++) {
				int a = 0;
				//if ((b % 10000) == 0)
				//	System.out.println("Reading word "+String.valueOf(b)+"/"+String.valueOf(words));
				String word = "";
				while(dis.available()>0) {
					byte characterb = dis.readByte();
					char character = (char) characterb;
					if (character == ' ') break;
					if (character != '\n') word = word+character;
					if ((a< string_max_w)&&(character == '\n')) a++;
				}


				float[] features = new float[size];
				//System.out.println(word);

				int xi = 0; 
				float xf = 0.0f; 
				byte[] byteArray = new byte[(4 * size)];
				dis.read(byteArray);
				float len = 0;
				for (int i=0; i < 4*size; i+=4) {					
					//we're reading a little endian file, and every 4 bytes we have a floating number
					xi = (byteArray[i] & 0xff);  
					xi = xi | ((byteArray[i+1] & 0xff) << 8); 
					xi = xi | ((byteArray[i+2] & 0xff) << 16); 
					xi = xi | ((byteArray[i+3] & 0xff) << 24); 
					xf = Float.intBitsToFloat(xi);
					features[i/4] = xf;
					len += xf*xf;
				}

				len = (float) Math.sqrt(len);
				for (int i = 0; i<size; i++) {
					features[i] = features[i] / len;
				}

				allFeatures [b] = features;
				allWords[b] = word;
			}
			
			//initialize class objects and copy items
			semantic_space.tokens = new LinkedHashMap<String, Integer>(words);
			semantic_space.features = allFeatures.clone();
			for (int b = 0; b<words; b++) {
				semantic_space.tokens.put(allWords[b],Integer.valueOf(b));
			}
			
			//since it's a linkedhashmap the order is deterministic
			//String[] keys = semantic_space.tokens.keySet().toArray(new String[words]);
			//for (int b = 0; b<words; b++) {
			//	if (!allWords[b].equals(keys[b])) {
			//		//this should *never* happen:
			//		System.out.println("Damn'! They're not ordered! allWords["+b+"]="+allWords[b]+" - keys["+b+"]="+keys[b]);
			//		throw new Exception("Similarity hashmap and array have different order!");
			//  }
			//}
			
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);
			//System.out.println("read in " + duration);
			System.out.printf("Similarity file loaded in memory. [%.1f sec]\n", duration / 1000000000.0f);
		}catch(Exception e){
			e.printStackTrace();
		}finally{

			try {
				if(is!=null) is.close();
				if (dis!=null) dis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private float calculateSim2Vectors(float[] normalizedVector1, float[] normalizedVector2){
		float dotProduct = 0.0f;

		if(normalizedVector1.length!=normalizedVector2.length){
			return (float)-2;
		}
		for(int i=0;i<normalizedVector1.length;i++){
			dotProduct += normalizedVector1[i]* normalizedVector2[i];
		}
		return dotProduct;
	}	


	private static LinkedHashMap<String, Float> sortByComparator(Map<String, Float> unsortMap) {
 
		// Convert Map to List
		List<Map.Entry<String, Float>> list = 
			new LinkedList<Map.Entry<String, Float>>(unsortMap.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
			public int compare(Map.Entry<String, Float> o1,
                                           Map.Entry<String, Float> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
 
		// Convert sorted map back to a Map
		LinkedHashMap<String, Float> sortedMap = new LinkedHashMap<String, Float>();
		for (Iterator<Map.Entry<String, Float>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Float> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

}
