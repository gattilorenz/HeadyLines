package news;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import edu.stanford.nlp.pipeline.Annotation;
import net.didion.jwnl.JWNLException;

public class NewsObject {
	public String headline;
	public String description;
	public String source;
	public LocalDate date;
	public String URL;
	public String imageURL;
	public Integer sentiment;
	Vector<Ingredient> ingredients;
	public Annotation annotation;
	
	
	
	public NewsObject (String headline, String description, String source, LocalDate date, Vector<Ingredient> ingredients) {
		this.headline = headline;
		this.description = description;
		this.source = source;
		this.date = date;
		this.ingredients = ingredients;
	}
	
	public NewsObject(String headline, String description, String source, LocalDate date2) {
		this.headline = headline;
		this.description = description;
		this.source = source;
		this.date = date2;
		this.ingredients = new Vector<Ingredient>();
	}

	public Vector<Ingredient> getIngredients() {
		return ingredients;
	}
	
	public void setIngredients(Vector<Ingredient> ingredients_vec) {
		this.ingredients = ingredients_vec;
	}
	
	public Vector<Ingredient> getIngredientsBelowThreshold( float threshold) {
		HashMap<String,Ingredient> true_values = ingredientFromDerivation();
		Vector<Ingredient> ingredients_below_threshold = new Vector<Ingredient>();
		for (Ingredient myIngredient : ingredients) {
			String ingredient_name = myIngredient.name+"#"+myIngredient.pos;
			if (true_values.get(ingredient_name).lemmaProb < threshold) {
				ingredients_below_threshold.add(myIngredient);
			}
		}
		return ingredients_below_threshold;
	}
	
	private HashMap<String, Ingredient> ingredientFromDerivation () {
		HashMap<String,Ingredient> returnMap = new HashMap<String,Ingredient>();
		Vector<Ingredient> derived_ingredients = new Vector<Ingredient>();
		for (Ingredient myIngredient : ingredients) {
			if (myIngredient.relation.startsWith("sent_lemma") || myIngredient.relation.startsWith("named_entity"))
				returnMap.put(myIngredient.name+"#"+myIngredient.pos, myIngredient);
			else derived_ingredients.add(myIngredient);
		}
		for (Ingredient derivedIngredient : derived_ingredients) {
			String derivation = derivedIngredient.relation.substring(0, derivedIngredient.relation.lastIndexOf("#"));
			Ingredient derivedFrom = returnMap.get(derivation);
			returnMap.put(derivedIngredient.name+"#"+derivedIngredient.pos, derivedFrom);
		}
		
		return returnMap;
	}
	
	
	
	public void printIngredients(BufferedWriter bufWriter) throws IOException {
		Vector<Ingredient> ingredients_vec = new Vector<Ingredient>(ingredients);
		Collections.sort(ingredients_vec);
		for (Ingredient ing:ingredients_vec){
			//System.out.println(ing.name+" "+ing.pos+" "+ing.relation+" prob: "+ing.lemmaProb);
			bufWriter.write(ing.name+"\t"+ing.pos+"\t"+ing.relation+"\t"+ing.lemmaProb+"\n");
		}
	}

	
;

	
	public void addSynonymsAndDerivations(HashMap<String, Float> lemmaProbs, WordnetChecker wn) throws JWNLException{
		HashSet<Ingredient> toAdd = new HashSet<Ingredient>();
		for(Ingredient ingredient: ingredients){
			if (ingredient.lemmaProb>0.05)
				continue;
			toAdd.addAll(addSynonyms(lemmaProbs, ingredient.name, ingredient.pos, ingredient.relation, wn));
			toAdd.addAll(addDerivations(lemmaProbs, ingredient.name, ingredient.pos, ingredient.relation, wn));
		}
		ingredients.addAll(toAdd);
	}
	
	

	public Vector<Ingredient> addSynonyms(HashMap<String, Float> lemmaProbs, String word, String pos, String reln, WordnetChecker wn) throws JWNLException{
		Vector<Ingredient> allSynonyms = new Vector<Ingredient>();
		if(!reln.equals(StaticDefinitions.SENT_TOKEN)){ //only deal with lemmas, not tokens
			Vector <Ingredient> synonyms = wn.fetchSynonymVector(lemmaProbs, word, pos, reln);
			for (Ingredient synonym : synonyms){
				//System.out.println(synonym.name + " is derived from " +word);
				synonym.derived_from = word;
				// 	System.out.println("Name: "+ twSynonymList.elementAt(syInd).name+ " Pos: "+twSynonymList.elementAt(syInd).pos);
				if(/*!ingredients.contains(synonym) && */!synonym.name.contains("_")){ //filter multiwords 
					//	System.out.println("Synonym: "+twSynonymList.elementAt(syInd)+" eklendi");
					/*TODO_Interface	*///updateIngredientsMap(twSynonymList.elementAt(syInd));
					//pronunciation her zaman olmak zorunda degil, ondan ingredientsMape ek olarak ingredientsta her seyi ayrica topladim.
					allSynonyms.add(synonym);
				}
			}
		}
		return allSynonyms;
	}

	
	public void addDerivations( HashMap<String, Float> lemmaProbs, WordnetChecker wn /*, Phonetics phonetics*/) throws JWNLException{
		HashSet<Ingredient> toAdd= new HashSet<Ingredient>();
		for(Ingredient ingredient: ingredients){
			toAdd.addAll(addDerivations(lemmaProbs, ingredient.name, ingredient.pos, ingredient.relation, wn));
		}
		ingredients.addAll(toAdd);
	}

	public Vector<Ingredient> addDerivations(HashMap<String, Float> lemmaProbs, String word, String pos, String reln, WordnetChecker wn) throws JWNLException{
		Vector<Ingredient> allDerivations = new Vector<Ingredient>();
		if(!reln.equals(StaticDefinitions.SENT_TOKEN)){ //only deal with lemmas, not tokens
			Vector <Ingredient> derivations = wn.fetchDerivations(lemmaProbs, word, pos, reln);
			if(derivations!=null){
				for (Ingredient derivation : derivations){
					// 	System.out.println("Name: "+ twSynonymList.elementAt(syInd).name+ " Pos: "+twSynonymList.elementAt(syInd).pos);
					if(/*!ingredients.contains(derivation) && */!derivation.name.contains("_")){ //filter multiwords 
						/*TODO_Interface	*///updateIngredientsMap(twSynonymList.elementAt(syInd));
						//pronunciation her zaman olmak zorunda degil, ondan ingredientsMape ek olarak ingredientsta her seyi ayrica topladim.
						allDerivations.add(derivation);
					}
				}
			}
		}
		return allDerivations;
	}
	
}
