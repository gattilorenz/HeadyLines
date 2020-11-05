package news;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureConversionUtils;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class StanfordParser {
   private TreebankLanguagePack tlp;
   private GrammaticalStructureFactory gsf;
   private LexicalizedParser lp;
   private Morphology morph; 
   
   public StanfordParser(){
	    lp = LexicalizedParser.loadModel();
		tlp = new PennTreebankLanguagePack();
		gsf = tlp.grammaticalStructureFactory();
		lp.setOptionFlags("-maxLength", "80", "-retainTmpSubcategories");
		morph = new Morphology();
		// props = new Properties();
		// props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		// pipeline = new StanfordCoreNLP(props);
   } 
   
   public StanfordParser(String pathToModel) {
	   this();
   }
   
   public Morphology getMorphology (){
	   return this.morph;
   }
   
  public DocumentPreprocessor tokenize(String paragraph) throws IOException {	  
      Reader reader = new StringReader(paragraph);

      DocumentPreprocessor dp = new DocumentPreprocessor(reader);
      return dp;
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
public Tree parse(List sentence){
	  try{
	  	Tree parse = lp.apply(sentence);
	  	return parse;
	  }
	  catch(Exception e){
		  return null;
	  }
  }
  

  @SuppressWarnings({ "rawtypes", "unchecked" })
public String connlParse(List sentence){
	  try{
		  Tree parse = lp.apply(sentence);
		  GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	      return GrammaticalStructureConversionUtils.dependenciesToString(gs, gs.typedDependencies(), parse, true, false, false);
	  }
	  catch(Exception e){
		  return null;
	  }
  }
  
  public Collection<TypedDependency> dependencyParse(Tree parse){
	  try{
		  GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		  Collection<TypedDependency>  depParse = gs.typedDependencies();//.typedDependenciesCCprocessed(true);
	     // System.out.println(depParse.toString());
	      return depParse;
	  }
	  catch(Exception e){
		  return null;
	  }
    //  System.out.println(tdl);
    //  System.out.println();
  }
  
  public ArrayList<TaggedWord> POStag(Tree parse){
	  return parse.taggedYield();
  }
  
  public Collection<TypedDependency> dependencyParseCoreLabelList(List<CoreLabel> sentence){
//	  System.out.println("SENTENCE: " + sentence);
	  try{
		  Tree parse = lp.apply(sentence);
		  GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		  Collection<TypedDependency>  depParse = gs.typedDependencies();//.typedDependenciesCCprocessed(true);
	   //   System.out.println(depParse.toString());
	      return depParse;
	  }
	  catch(Exception e){
		  return null;
	  }
    //  System.out.println(tdl);
    //  System.out.println();
  }
  
  public static void main(String [] args) throws IOException{
	  StanfordParser stnParser = new StanfordParser();
	  String paragraph = "Mortar assault leaves at least 18 dead";
	  DocumentPreprocessor dp = stnParser.tokenize(paragraph);
		for (List<HasWord> tokenizedSentence : dp) {
			//System.out.println("tokenizedSentence: "+ tokenizedSentence);
			
			Tree parse = stnParser.parse(tokenizedSentence);
			if(parse==null){
				continue;
			}
			System.out.println("PARSE: "+parse.toString());
		//	ArrayList<TaggedWord> taggedWords = stnParser.POStag(parse);
			
			//for (TaggedWord tw : taggedWords){
				//System.out.println("WORD: "+ tw.word()+ " TAG: "+ tw.tag());
			//}
			
			Collection<TypedDependency> depParse = stnParser.dependencyParse(parse);
			System.out.println(depParse.toString());
	  }
  }
}
