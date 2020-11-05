
package news;

public class StaticDefinitions {
	final static String jwnl_propsFile="/Users/lorenzo/Projects/HeadyLinesGUI/resources/file_properties.xml";

	final static String HYPERNYM = "hypernym";
	final static String SYNONYM = "synonym";
	final static String ANTONYM = "antonym";
	final static String DERIVATION = "derivation";
	public final static String NAMED_ENTITIY = "named_entity";
	//public final static String nerPath = "/Volumes/Dati/Utente/Google Drive/Work/tools/english.all.3class.distsim.crf.ser.gz";	
	final static String SENT_LEMMA = "sent_lemma";
	final static String SENT_TOKEN = "sent_token";

	final static String NYTlemmaProbsPath = "/Users/lorenzo/Projects/HeadyLinesGUI/resources/lexical/lemmaProbs_nyt_eng_199808_199909.txt";
	
/*
	final static String METAPHOR_ConceptNet = "metaphor_ConceptNet";
	final static String METAPHOR_GoogleSuggestions = "metaphor_GoogleSuggestions";

	final static String ADJ_SENTIWORDNET = "adj_senti";
	public final static String MOSTFREQNOUN_CONCEPTNET = "most_freq_n_conceptnet";
	public final static String ADDED_BY_USER = "added_by_user";

	
	final static String enMappingsPath = "/Users/Guditta/Documents/workspace/Naming_interface/WebContent/dict/phonemeMappings/englishMappings.txt";
	final static String enPronPath = "/Users/Guditta/Documents/workspace/Naming_interface/WebContent/dict/englishPron/cmudict0.3";
	
	final static String inputFile = "input.txt";
	final static String logFile = "/Users/Guditta/Documents/workspace/Naming_interface/WebContent/logFile.txt";
	final static String outputFile = "/Users/Guditta/Documents/workspace/Naming_interface/WebContent/output.txt";
	final static String outputFile_conceptnet4 = "output_conceptnet4.txt";
    final static String outputFile_conceptnet4_noAtLocation = "output_conceptnet4_noAtLocation_with_latin.txt";
	
	//final static String punFileTxt = "punFile.txt";
	//final static String punFileTxt = "punFile_conceptnet4.txt";
    final static String punFileTxt = "punFile_conceptnet4_noAtLocation.txt";
	final static String latinSuffixation_annotationFile = "latinSuffixation_annotationFile.txt";
	final String conceptNetDBpath =  "/Users/Guditta/.conceptnet/ConceptNet.db";
	final static String latinModelPath = "/Users/Guditta/Documents/workspace/Naming/jars/latin_model.arpa";
	final static String englishModelPath =  "/Users/Guditta/Documents/workspace/Naming/jars/model.arpa";
	final static int punCountToPresent = 20;
	final static int punCountToUseForSuffixation = 10;

	final static String words_alignment = "/Users/Guditta/Documents/workspace/Naming_interface/WebContent/alignment/words.txt";
	final static String prons_alignment = "/Users/Guditta/Documents/workspace/Naming_interface/WebContent/alignment/prons.txt";

	final static String words_alignment_cleaned = "/Users/Guditta/Documents/workspace/Naming_interface/WebContent/alignment/words_cleaned.txt";

	final static String alignmentPath = "/Users/Guditta/Documents/workspace/Naming_interface/WebContent/dict/alignment.txt";
	
	final static String stopWords = "/Users/Guditta/Documents/workspace/Naming_interface/WebContent/dict/stopWords.txt";
	
	final static String languageModelPath = "/Users/Guditta/Documents/workspace/Naming/jars/checkWordInLanguageModel.sh";
	final static String languageModelPath_latin = "/Users/Guditta/Documents/workspace/Naming/jars/checkWordInLanguageModel_latin.sh";

	//final static String conceptNet_dbPath = "/Users/Guditta/Documents/workspace/Naming_interface/WebContent/ConceptNet.database";
	
	public final static String determineInput  = "START COOKING";
	public final static String latinLoverExplanation = "A Tasteful Supporter for Creative Naming";
	public final static String discoPath = "/Users/Guditta/Documents/workspace/Naming_interface/WebContent/disco/en-BNC-20080721";
	
	
	static float lev_groupingDist;
	public static float lev_normDist_threshold;
	public static float lanModelScore_threshold;
	
	public static int maxNumIng = 10;
	public static int maxNumMet = 10;

	public static void setThresholds (float new_lev_groupingDist, float new_lev_normDist_threshold, float new_lanModelScore_threshold, int new_maxNumIng, int new_maxNumMet){
		lev_groupingDist = new_lev_groupingDist;
		lev_normDist_threshold = new_lev_normDist_threshold;
		lanModelScore_threshold = new_lanModelScore_threshold;
		maxNumIng = new_maxNumIng;
		maxNumMet = new_maxNumMet; 
		System.out.println("setThresholdsa girdi lev_normDist_threshold: "+lev_normDist_threshold+" lanModelScore_threshold: "+lanModelScore_threshold + " maxNumIng: "+ maxNumIng);
	}
	
	public static void setThresholdsString (String phoneticSim, String lanModel, String maxNumIngStr, String maxNumMetStr){
		System.out.println("setThresholdsStringe girdi");
		float phoneticSimFloat ;
		float lanModelFloat ;
		int maxNumIng = Integer.parseInt(maxNumIngStr);
		int maxNumMet = Integer.parseInt(maxNumMetStr);
		if(phoneticSim.equals("strict")){
			phoneticSimFloat = (float) 0.33;
		}
		else if(phoneticSim.equals("medium")){
			phoneticSimFloat = (float) 0.5;
		}
		else if(phoneticSim.equals("relaxed")){
			phoneticSimFloat = (float) 0.66;
		}
		else{
			phoneticSimFloat = (float) 0.66;
		}
		
		if(lanModel.equals("strict")){
			lanModelFloat = (float) -3;
		}
		else if(lanModel.equals("medium")){
			lanModelFloat = (float) -3.5;
		}
		else if(lanModel.equals("relaxed")){
			lanModelFloat = (float) -4;
		}
		else{
			lanModelFloat = (float) -4;
		}
		System.out.println();
		StaticDefinitions.setThresholds((float)0, phoneticSimFloat, lanModelFloat, maxNumIng, maxNumMet );

	}*/
}
