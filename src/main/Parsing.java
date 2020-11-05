package main;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Parsing {

	private static StanfordCoreNLP pipeline;
	private static Annotation annotation;	
	
	public Parsing() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, depparse");
		//props.setProperty("parse.model", "edu/stanford/nlp/models/parser/nndep/english_SD.gz");
		props.setProperty("depparse.model", "edu/stanford/nlp/models/parser/nndep/english_SD.gz");
        
        
		System.out.println("Loading Stanford CoreNLP...");

		pipeline = new StanfordCoreNLP(props);

		System.out.printf("CoreNLP finished loading.\n");
		
	}
	
	public Annotation parseSentence(String sentence) {
	annotation = new Annotation(sentence);
	pipeline.annotate(annotation);
	return annotation;
	}

	public String annotatedSentenceToString(Annotation annotation) {
		Writer result = new StringWriter();
		PrintWriter out = new PrintWriter(result);
		pipeline.prettyPrint(annotation, out);
		return result.toString();
	}
}
