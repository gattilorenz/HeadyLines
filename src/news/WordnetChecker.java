package news;



import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.MorphologicalProcessor;


public class WordnetChecker {
	private Dictionary wordnetJWNL;
	private MorphologicalProcessor morph;

	private HashMap<String,String> posMap = new HashMap<String,String>();
	private HashMap<String,String> wnDomsMap = new HashMap<String,String>();

	public void fillInPosMap(){
		posMap.put("a", "adjective");
		posMap.put("n", "noun");
		posMap.put("r", "adverb");
		posMap.put("v", "verb");
	}
	
	public WordnetChecker() throws JWNLException, IOException {
		initializeJWNL();
		fillInPosMap();
		//loadWNdoms();
	}

//	IndexWord word = wordnetJWNL.getIndexWord(POS.ADJECTIVE, word);
//	if(word == null){
	
	@SuppressWarnings("rawtypes")
	public Vector<Ingredient> fetchHypernymsOfTargetWord(String word /*, Phonetics phonetics*/) throws JWNLException{
		Vector<Ingredient>  hypernymVector =new  Vector<Ingredient> ();
		IndexWord indexWord= wordnetJWNL.getIndexWord(POS.NOUN, word); //category name should be a noun
		PointerTargetNodeList hypernymList=new PointerTargetNodeList();
		if(indexWord!=null){
			hypernymList = PointerUtils.getInstance().getDirectHypernyms(indexWord.getSenses()[0]);
		    Iterator hypernymListIt = hypernymList.iterator();
		//	System.out.println("hypernyms ******************");
			while (hypernymListIt.hasNext()) {
				  PointerTargetNode hypernymNode = (PointerTargetNode) hypernymListIt.next();
				  Word[] hypernymWords = hypernymNode.getSynset().getWords();
				  for(int j=0;j<hypernymWords.length;j++){
					 // System.out.print(hypernymWords[j].getLemma()+" ");
					  String oneHypernym = hypernymWords[j].getLemma();
					  if(!oneHypernym.contains("_") && Character.isLowerCase(oneHypernym.charAt(0))){
						  hypernymVector.add(new Ingredient(oneHypernym,"n", StaticDefinitions.HYPERNYM+"#"+word));
					  }
				  }
			}
			
			return hypernymVector;
		}
		else{
			System.out.println("HYPERNYM LIST NULL**********");
			return null;
		}
	}
	
	public String convertWordNetPosToOneLetter(POS pos){
		if(pos.equals(POS.ADJECTIVE)){
			return "a";
		}
		else if(pos.equals(POS.ADVERB)){
			return "r";
		}
		else if(pos.equals(POS.VERB)){
			return "v";
		}
		else if(pos.equals(POS.NOUN)){
			return "n";
		}
		return null;
	}
	public Vector<Ingredient> fetchDerivations(HashMap<String, Float> lemmaProbs, String word, String oneLetterPos,  String relation) throws JWNLException{
		//System.out.println("fetchDerivations a girdi!!!");
		Vector<Ingredient>  derivationVector =new  Vector<Ingredient> ();
		POS wnPos = convert1LetPos(oneLetterPos);
		//System.out.println("POS: "+wnPos);
		IndexWord indexWord= wordnetJWNL.getIndexWord(wnPos, word); //category name should be a noun
		if(indexWord!=null){
			//System.out.println("indexWord!=null");
			Synset synset = indexWord.getSenses()[0];
			Pointer[] pointers = synset.getPointers(PointerType.NOMINALIZATION);
			/*for(Pointer pointer:pointers){
				System.out.println("*******"+pointer.toString());
			}
			*/
			for (Pointer pointer : pointers) {
		       // System.out.println(pointer.getType().toString());
		        Word [] derivationWords =   (pointer.getTargetSynset().getWords());
		        for(int j=0;j<derivationWords.length;j++){
					  String derivation_lemma = derivationWords[j].getLemma();
					  String derivation_pos = convertWordNetPosToOneLetter(derivationWords[j].getPOS());
					//  System.out.println("derivation lemma: "+derivation_lemma+" pos: "+derivation_pos);
					  if(!derivation_lemma.contains("_") && Character.isLowerCase(derivation_lemma.charAt(0))){
						  //System.out.println("derivation: "+derivation_lemma+ " from "+ word+"#"+oneLetterPos);
						 derivationVector.add(new Ingredient(WordUtils.getLemmaProb(lemmaProbs, derivation_lemma), derivation_lemma, derivation_pos, /*relation+"#"+*/StaticDefinitions.DERIVATION+"#"+word+"#"+ oneLetterPos));
					  }
				 }
		    }
			
			/*
			derivationList = PointerUtils.getInstance().getDerived(synset);
		    Iterator derivationListIt = derivationList.iterator();
		//	System.out.println("hypernyms ******************");
			while (derivationListIt.hasNext()) {
				  PointerTargetNode derivationNode = (PointerTargetNode) derivationListIt.next();
				  Word[] derivationWords = derivationNode.getSynset().getWords();
				  for(int j=0;j<derivationWords.length;j++){
					  String oneDerivation = derivationWords[j].getLemma();
					  System.out.println("oneDerivation: "+oneDerivation);
					  if(!oneDerivation.contains("_") && Character.isLowerCase(oneDerivation.charAt(0))){
						  System.out.println("derivation: "+oneDerivation+ " for "+ word+" "+oneLetterPos);
						  derivationVector.add(new Ingredient(oneDerivation,"n",  relation+"#"+StaticDefinitions.DERIVATION+"#"+word+"#"+ oneLetterPos, phonetics.getPronunciation(derivationWords[j].getLemma())));
					  }
				  }
			}
			*/
			return derivationVector;
		}
		else{
			//System.out.println("DERIVATION LIST NULL**********");
			return null;
		}
	}
	
	public boolean isNoun (String word) throws JWNLException{
		IndexWord indexWord = wordnetJWNL.getIndexWord(POS.NOUN, word);
		if(indexWord==null){
			return false;
		}
		else 
			return true;
	}
	
	String lemmatizeNoun(String word)throws JWNLException{
		IndexWord indexWord = wordnetJWNL.getIndexWord(POS.NOUN, word);
		if(indexWord!=null){
			String lemma = indexWord.getLemma();
			if(isNoun(lemma)){
				return lemma;
			}
			else return null;
		}
		else
			return null;
	}
	
	public String lemmatize(String word, String stParserPos) {
		POS pos = null;
		word = word.toLowerCase();
		if(stParserPos.startsWith("J")){
			pos = POS.ADJECTIVE;
		}	
		else if(stParserPos.startsWith("N")){
			pos = POS.NOUN;
		}	
		else if(stParserPos.startsWith("V")){
			pos = POS.VERB;
		}	
		else if(stParserPos.startsWith("R")){
			pos = POS.ADVERB;
		}	

		if(pos==null){
			//System.out.println("Strange pos: "+stParserPos+"!!!");
			return null;
		}	
		try {
			//System.out.println("morph: "+morph);
			IndexWord indexWord = morph.lookupBaseForm(pos, word);

			if (indexWord != null) {
				//System.out.println(word + " " + pos + " => " + indexWord.getLemma());
				return indexWord.getLemma();

			}
			// This means it was a wrong part of speech!
		//	System.out.println(word + " is not a " + pos + " (stage 2)");
			return null;


		} catch (JWNLException ex) {
		//	System.out.println("ERROR could not get lemma: " + ex.getMessage());
			return null;
		}
	}
	
	public Vector<String> getPossiblePosList (String word) throws JWNLException{
		Vector<String> possiblePosList = new Vector<String>();
		HashMap <String, POS> posStrToJWNLposMap = new HashMap <String,POS>();
		posStrToJWNLposMap.put("a", POS.ADJECTIVE);
		posStrToJWNLposMap.put("v", POS.VERB);
		posStrToJWNLposMap.put("r", POS.ADVERB);
		posStrToJWNLposMap.put("n", POS.NOUN);
		
		for (Map.Entry<String, POS> entry : posStrToJWNLposMap.entrySet()){
			String pos = entry.getKey();
			POS pos_JWNL = entry.getValue();
			if(wordnetJWNL.getIndexWord(pos_JWNL, word)!=null){
				possiblePosList.add(pos);
			}
		}
		return possiblePosList;
	}
	
	@SuppressWarnings("rawtypes")
	public Vector<String> fetchAntonymVector (String word) throws JWNLException{
		Vector<String>  antonymVector = new  Vector<String> ();
		IndexWord indexWord = wordnetJWNL.getIndexWord(POS.ADJECTIVE, word);
		
		
		PointerTargetNodeList antonymList=new PointerTargetNodeList();
		if(indexWord!=null){
			antonymList = PointerUtils.getInstance().getAntonyms(indexWord.getSenses()[0]);
		    Iterator antonymListIt = antonymList.iterator();
		//	System.out.println("hypernyms ******************");
			while (antonymListIt.hasNext()) {
				  PointerTargetNode antonymNode = (PointerTargetNode) antonymListIt.next();
				  Word[] antonymWords = antonymNode.getSynset().getWords();
				  for(int j=0;j<antonymWords.length;j++){
					 // System.out.print(hypernymWords[j].getLemma()+" ");
					  String oneAntonym = antonymWords[j].getLemma();
					  if(!oneAntonym.contains("_")){ //avoid multiwords
						  antonymVector.add(oneAntonym);
					  }
		          }
			}
			return antonymVector;
		}
		else{
			System.out.println("ANTONYM LIST NULL**********");
			return null;
		}
	}
	
	public POS convert1LetPos(String oneLetPos){
		POS wnPOS = null;
		char firstChar = oneLetPos.charAt(0);
		switch(firstChar){
			case 'n':
				wnPOS = POS.NOUN;
				break;
			case 'r':
				wnPOS = POS.ADVERB;
				break;
			case 'a':
				wnPOS = POS.ADJECTIVE;
				break;
			case 'v':
				wnPOS = POS.VERB;
				break;
			default:
				break;
		}
		return wnPOS;
	}
	
	public void loadWNdoms() throws IOException{
		System.out.println("loading wordnet domains...");
		InputStreamReader wndomReader   = ResourceUtils.getReaderForResource("resources/lexical/wn-domains-3.2-20070223");
		// Create Buffered/PrintWriter Objects
		BufferedReader wndomInputStream   = new BufferedReader(wndomReader);

		//fill the hashtable
		String inLine = null;
		while ((inLine = wndomInputStream.readLine()) != null) { 
			inLine = inLine.trim();
			if(inLine.length()>0){
				//	System.out.println("Okunan: "+inLine);
				String offset = inLine.split("\t")[0];
				String domain = inLine.split("\t")[1];
				//System.out.println("offset: "+offset+" dom: "+domain);
				wnDomsMap.put(offset , domain);
				/*
		            Offset: 00521963-r Domain: linguistics
		            Okunan: 00522083-r	biology*/ 
			}
		}
		wndomInputStream.close();
		System.out.println("loaded!");
	}

	public Vector<String> retrieveDomains(String lemma, POS pos, String oneLetterPos) throws JWNLException{
		IndexWord indexWord = wordnetJWNL.getIndexWord(pos, lemma);
		Vector<String> domainsForLemma = new Vector<String>();
		if(indexWord!=null){
			long [] synsetOffsets = indexWord.getSynsetOffsets();
			for(long synsetOffset:synsetOffsets ){
				String key = String.format("%08d", synsetOffset)+"-"+oneLetterPos;
				if(wnDomsMap.containsKey(key)){
					//sent.increase_content_word_senses_with_dom_label();
					//System.out.println("key exists in wn domains");
					String domsForKey = wnDomsMap.get(key);
					String[] domsArr = domsForKey.split("\\s+");
					for(String dom:domsArr){
						if(!domainsForLemma.contains(dom)){
							domainsForLemma.add(dom);
						}
					}
				}
			}
		}
		return domainsForLemma;
	}
	public Vector<Ingredient> fetchSynonymVector (HashMap<String, Float> lemmaProbs, String word, String possiblePosList, String relation) throws JWNLException{
		Vector<Ingredient>  synonymVector =new  Vector<Ingredient> ();
		IndexWord indexWord=null;
		String [] posArr = possiblePosList.split(",");
		for (int i=0; i<posArr.length; i++){
			POS wnPos = convert1LetPos(posArr[i]);
			if(wnPos != null){
				indexWord = wordnetJWNL.getIndexWord(wnPos,word);
			}
			
			if(indexWord != null){
				synonymVector.add(new Ingredient(WordUtils.getLemmaProb(lemmaProbs, word), word,posArr[i], relation)); //add the word itself together with its pos, can be added several times due to various pos values
				Word[] words = indexWord.getSenses()[0].getWords();
				for (int j = 0; j < words.length; j++) {
					String lemma = words[j].getLemma().toLowerCase();
					
					if(!lemma.contains("_") && Character.isLowerCase(lemma.charAt(0))){ //avoid multiwords and proper nouns
						synonymVector.add( new Ingredient(WordUtils.getLemmaProb(lemmaProbs, lemma), words[j].getLemma(), posArr[i], /*relation+"#"+*/StaticDefinitions.SYNONYM+"#"+word+"#"+posArr[i]));
					// System.out.print("Synonym eklendi: "+ words[j].getLemma()+" ");
					}
				} 
			}
		}
		
		return synonymVector;
	}

	public boolean wordExistsInWN (String targetWord) throws JWNLException{
		if(wordnetJWNL.getIndexWord(POS.NOUN, targetWord)!=null){
			return true;
		}
		else if(wordnetJWNL.getIndexWord(POS.ADJECTIVE, targetWord)!=null){
			return true;
		}
		else if(wordnetJWNL.getIndexWord(POS.VERB, targetWord)!=null){
			return true;
		}
		else if(wordnetJWNL.getIndexWord(POS.ADVERB, targetWord)!=null){
			return true;
		}
		else{
			return false;
		}
	}
	
	public String convertPOS(String pos_oneLetter){
		return posMap.get(pos_oneLetter);
	}
	
	public void initializeJWNL() throws FileNotFoundException, JWNLException{
	 	JWNL.initialize(new FileInputStream(StaticDefinitions.jwnl_propsFile));
		wordnetJWNL = Dictionary.getInstance();
		morph = Dictionary.getInstance().getMorphologicalProcessor();

		return;
	}
	
	
	public static void main(String[] args) throws JWNLException, IOException{
		//JWNL begin
		WordnetChecker wnChecker = new WordnetChecker();
		wnChecker.fetchDerivations(null, "free", "a", "aa");
	 }
}
