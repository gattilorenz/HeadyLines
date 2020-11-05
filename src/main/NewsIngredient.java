package main;

public class NewsIngredient implements Comparable<NewsIngredient>, java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2178794645426029530L;
	public String lemma;
	public String wnPOS;
	public String type; 
	public float probability; 

	
	public NewsIngredient(String lemma, String wnPOS, String type, float probability) {
		this.lemma = lemma;
		this.wnPOS = wnPOS;
		this.type = type;
		this.probability = probability;
	}

	  public int compareTo(NewsIngredient anotherIngredient) {
	    float anotherIngredientProb = ((NewsIngredient) anotherIngredient).probability;
	    if ( this.probability < anotherIngredientProb) return -1;
	    if ( this.probability > anotherIngredientProb) return 1;
	    return 0;	        
	  }
}
