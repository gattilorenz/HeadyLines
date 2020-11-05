package news;
import java.io.BufferedReader;
import java.util.Collection;
import java.util.HashMap;

public class WordUtils {
	private static final String britishAmericanResourcePath = "resources/lexical/varcon_machine_friendly.txt";

	public static boolean isContentPos(String pos) {
		if (pos.startsWith("N") || pos.startsWith("R") || pos.startsWith("V") || pos.startsWith("J")) {
			return true;
		}
		return false;
	}

	public static float getLemmaProb(HashMap<String, Float> lemmaProbs, String lemma){
		float lemmaProb = -1;
		if(lemmaProbs!=null){
			if(lemmaProbs.containsKey(lemma)){
				lemmaProb = lemmaProbs.get(lemma);
			}
			else{ //if there's no value, try with lowercase
				if(Character.isUpperCase(lemma.charAt(0))){
					lemma = lemma.toLowerCase();
					if(lemmaProbs.containsKey(lemma)){
						lemmaProb = lemmaProbs.get(lemma);
					}
				}
			}
		}
		
		return lemmaProb;
	}
	
	
	public static boolean isProperNoun(String pos) {
		return pos.startsWith("NNP");
	}

	public static String convertToOneLetterPos(String threeLetterPos){
		String oneLetterPos = "";
		char firstLetter = threeLetterPos.charAt(0);
		switch(firstLetter){
		case 'N':
			oneLetterPos = "n";
			break;
		case 'V':
			oneLetterPos = "v";
			break;
		case 'R':
			oneLetterPos = "r";
			break;
		case 'J':
			oneLetterPos = "a";
			break;
		default:
			break;
		}
		return oneLetterPos;
	} 
	
	private HashMap<String, String> britishAmericanMapping = null;

	/*
	 * Americanize a word.
	 * 
	 * The british-american mapping is loaded when the function is called for
	 * the first time. Return the americanized version of the input string, or
	 * null if the input string is not found in the mapping.
	 */
	public String americanize(String word) {
		if (britishAmericanMapping == null) {
			LoadBritishToAmericanMapping();
		}
		String key = word.toLowerCase();
		if (britishAmericanMapping.containsKey(key)) {
			return britishAmericanMapping.get(key);
		}
		return null;
	}

	/*
	 * Load a mapping of british to american spellings from a bundled resource.
	 */
	private void LoadBritishToAmericanMapping() {
		BufferedReader reader = new BufferedReader(ResourceUtils.getReaderForResource(britishAmericanResourcePath));
		String line;
		britishAmericanMapping = new HashMap<String, String>();
		try {
			while ((line = reader.readLine()) != null) {
				// DO THE PARSING HERE AND ADD STUFF TO THE MAP
				String[] splittedLine = line.split("\\s+");
				for (int sInd = 0; sInd < splittedLine.length - 1; sInd++) {
					britishAmericanMapping.put(splittedLine[sInd], splittedLine[splittedLine.length - 1]);
				}
			}
		} catch (Exception ex) {
			System.err.println("Failed reading from resource: " + britishAmericanResourcePath);
			ex.printStackTrace();
			System.exit(1);
		}
	}

	public static String joinStrings(Collection<String> parts, String connector) {
		StringBuffer result = new StringBuffer();
		for (String part : parts) {
			if (result.length() != 0) {
				result.append(connector);
			}
			result.append(part);
		}
		return result.toString();
	}

	public static String createOneLetterPos(String originalPos) {
		String oneLetterPos = null;
		if (originalPos.startsWith("J")) {
			oneLetterPos = "a";
		} else if (originalPos.startsWith("N")) {
			oneLetterPos = "n";
		} else if (originalPos.startsWith("V")) {
			oneLetterPos = "v";
		} else if (originalPos.startsWith("R")) {
			oneLetterPos = "r";
		}
		return oneLetterPos;
	}

	public static boolean isVerb(String pos) {
		return pos.toUpperCase().startsWith("V");
	}

}
