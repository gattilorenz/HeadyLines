package news;

import java.io.Serializable;
import java.util.Vector;


public class Ingredient implements Serializable, Comparable<Ingredient>{
	 /**
	 * 
	 */
	private static final long serialVersionUID = -4480394816107607468L;
	int positionInIngredientList = -1; //just useful for the interface, when the user clicks the ones that he is interested in, we associate the ingredient to the main vector with this parameter
	 float lemmaProb = -1; //calculated from ldc
	 String name;
	 String pos;
	 String relation; 
	 String derived_from;
	 String pronunciation;
	 String original_name_beforeCut = "";
	 Vector<Float> lsaSimValues = new Vector<Float>();
	 String ingredients = ""; //only used for puns, the form of ing1#ing2
	 String wordInTheEnd = ""; //only used for puns
	 Vector<Ingredient> multiwords = new Vector<Ingredient>();
	 public Ingredient (String newName, String newPos, String newRelation){
		name = newName;
		pos = newPos;
		relation = newRelation; //Possible Values: pun/target_word / hypernym / synonym#whose synonym from the main relation words#pos of the preceding word
	}
	 public Ingredient (float newLemmaProb, String newName, String newPos, String newRelation){
			lemmaProb = newLemmaProb;
		 	name = newName;
			pos = newPos;
			relation = newRelation; //Possible Values: pun/target_word / hypernym / synonym#whose synonym from the main relation words#pos of the preceding word
	}
	 
	 public Ingredient (float newLemmaProb, String newName, String newPos, String newRelation, String derivedFrom){
			lemmaProb = newLemmaProb;
		 	name = newName;
			pos = newPos;
			relation = newRelation; //Possible Values: pun/target_word / hypernym / synonym#whose synonym from the main relation words#pos of the preceding word
			derived_from = derivedFrom;
	}
	 
	public Ingredient (Ingredient newIng){
		positionInIngredientList = newIng.positionInIngredientList;
		name = newIng.name;
		pos = newIng.pos;
		relation = newIng.relation;
		original_name_beforeCut = newIng.original_name_beforeCut;
		for(Float value:newIng.lsaSimValues){
			lsaSimValues.add(value);
		}
		ingredients = newIng.ingredients;
		wordInTheEnd = newIng.wordInTheEnd;
	}
	
	public int hashCode(){
		return (name+"#"+pos).hashCode();
	}
	
	Ingredient (Ingredient newIng, String newName){
		name = newName;
		pos = newIng.pos;
		relation = newIng.relation;
		pronunciation = newIng.pronunciation;
		for(Float value:newIng.lsaSimValues){
			lsaSimValues.add(value);
		}
		ingredients = newIng.ingredients;
		wordInTheEnd = newIng.wordInTheEnd;
		original_name_beforeCut = newIng.name;
	}
	
	public boolean equals(Object o1){
		if(o1 instanceof Ingredient){
			Ingredient ingO1 = (Ingredient) o1;
			return this.name.toLowerCase().equals(ingO1.name.toLowerCase()) && this.pos.equals(ingO1.pos);
		}
		else return false;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String new_name){
		this.name = new_name;
	}
	
	public void setWordInTheEnd (String newWordInTheEnd){
		wordInTheEnd = newWordInTheEnd;
	}
	
	public void setIngredients(String name1, String name2){
		ingredients = name1+"#"+name2;
	}
	
	public String getRelation(){
		return this.relation;
	}
	
	public String getPron(){
		return this.pronunciation;
	}
	
	public String getRelationExplanation(){
		String explanation = "";
		System.out.println("relation: "+relation);
		if(relation.contains("#")){
			String[] splittedRelation = relation.split("#");
			if(relation.startsWith("metaphor")){
				explanation = splittedRelation[1];
			}
			else if(relation.startsWith("hypernym")){
				explanation = "of "+ splittedRelation[1];
			}
			else if(relation.startsWith(StaticDefinitions.NAMED_ENTITIY)){
				/*if(relation.contains(StaticDefinitions.SYNONYM)){
					explanation = splittedRelation[1]+" of "+splittedRelation[2];
				}*/
				String withSlash = splittedRelation[1];
				String[] splitted =  withSlash.split("/");
				explanation = splitted[splitted.length-1].replace("_", " ")+" ("+ splittedRelation[2]+")";
			}
			else{
				String firstPart = splittedRelation[1];
				String secondPart = splittedRelation[2];
				explanation = firstPart+" of "+secondPart;
			}
		}
		else{
			if(relation.startsWith("target_word")){
				explanation = "category";
			} 
			else if(relation.startsWith("property")){
				explanation = "property";
			}
		
		}
		return explanation;
	}
	
	public String getPOS(){
		return pos;
	}
	public void setPositionInIngredientList(int position){
		this.positionInIngredientList = position;
	}
	public int getPositionInIngredientList(){
		return this.positionInIngredientList;
	}
	@Override
	public int compareTo(Ingredient o) {
		return (int) Math.signum(o.lemmaProb - this.lemmaProb)  ;
	}
	
	public float getLemmaProb() {
		return this.lemmaProb;
	}
	
}
