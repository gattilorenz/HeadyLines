package news;
import java.util.List;
import java.util.Vector;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

public class NER {
	AbstractSequenceClassifier<CoreLabel> classifier;
	public NER(){
		//classifier = CRFClassifier.getClassifierNoExceptions(StaticDefinitions.nerPath);
		classifier = CRFClassifier.getDefaultClassifier();
	}

	public Vector<String> extract(String sent){
		Vector<String> entities = new Vector<String>();

		for (List<CoreLabel> sentence : classifier.classify(sent)) {
			String entity = "";
			for (CoreLabel word : sentence) {
				String token = word.word();
				String tag = word.get(CoreAnnotations.AnswerAnnotation.class);
				if(tag.equals("O")){
					//System.out.println("girdi!!");
					if(entity.length() > 0){
						entities.add(entity);
						entity = "";
					}
					else{
						continue;
					}
				}
				else{
					if(entity.length()>0){
						entity+=" ";
					}
					entity+=token;
				}
				//System.out.print(token + '/' + tag + ' ');
			}
			//if the last word is an entity, it hasn't been added to the list yet
			if(entity.length()>0){
				entities.add(entity);
			}
			//System.out.println();
		}

//		for(String entity: entities){
//			System.out.println("entity: "+entity);
//		}
		
		return entities;
		/* System.out.println(classifier.classifyWithInlineXML(sent));
	        System.out.println(classifier.classifyToString(sent, "xml", true));
	        int i=0;
	        for (List<CoreLabel> lcl : classifier.classify(sent)) {
	          for (CoreLabel cl : lcl) {
	            System.out.print(i++ + ": ");
	            System.out.println(cl);
	          }
	        }
		 */




		/*
		 for (List<CoreLabel> sentence :  classifier.classify(sent)) {
	          for (CoreLabel word : sentence) {
	            System.out.print(word.word() + '/' + word.get(CoreAnnotations.AnswerAnnotation.class) + ' ');
	          }
	          System.out.println();
		 }*/

	}

}
