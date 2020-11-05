package main;

import java.io.File;
import java.io.IOException;

import edu.mit.jwi.*;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.POS;

public class WordnetUtils {
	
	private static IDictionary dict;
	
	public WordnetUtils() throws IOException {
		// construct the URL to the Wordnet dictionary directory
		String wnhome = System.getenv("WNHOME");
		if (wnhome==null) 
			wnhome="/usr/local/Cellar/wordnet/3.1"; //eclipse is a bitch
		String path = wnhome + File.separator + "dict";
		File wnDir = null;
		wnDir = new File(path);
	
		if(!wnDir.isDirectory()) {
			System.out.println(path + "is not a valid directory!");
			return;
		}
		
		// construct the dictionary object in memory, load it and open it.
		dict = new RAMDictionary(wnDir, ILoadPolicy.IMMEDIATE_LOAD);
		dict.open();
	}

	public static boolean isLemmaInWordnet(String lemma) {
		if (dict.getIndexWord(lemma,POS.ADJECTIVE) != null) return true;		
		if (dict.getIndexWord(lemma,POS.ADVERB) != null)    return true;
		if (dict.getIndexWord(lemma,POS.NOUN) != null)      return true;
		if (dict.getIndexWord(lemma,POS.VERB) != null)      return true;
		return false;
	}
	
	public static boolean isLemmaPOSInWordnet(String lemma, POS pos) {
		if (dict.getIndexWord(lemma,pos) != null) return true;		
		return false;
	}
	
	public static boolean isLemmaPOSInWordnet(String lemma, String pos) {
		if (dict.getIndexWord(lemma,POSStringtoWNPOS(pos)) != null) return true;		
		return false;
	}
	
	private static POS POSStringtoWNPOS(String PoS) {
		if (PoS.toLowerCase().startsWith("v")) return POS.VERB;
		if (PoS.toLowerCase().startsWith("j") || PoS.toLowerCase().startsWith("a") ) return POS.ADJECTIVE;
		if (PoS.toLowerCase().startsWith("r")) return POS.ADVERB;
		if (PoS.toLowerCase().startsWith("n")) return POS.NOUN;		
		return null;
	}
	
	public static String POStoWNPOS(String POS) {
		String wnpos = "";
		if (POS.toLowerCase().startsWith("vb")) wnpos = "v";
		if (POS.toLowerCase().startsWith("jj")) wnpos = "a";
		if (POS.toLowerCase().startsWith("rb")) wnpos = "r";
		if (POS.toLowerCase().startsWith("nn")) wnpos = "n";		
		return wnpos;
		
	}
	
}
