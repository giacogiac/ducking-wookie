package poc;

import java.io.Serializable;

public class CoursBoursier implements Serializable {
	public long time;
	public double valeur;
	
	public CoursBoursier(long time, double valeur) {
		this.time = time;
		this.valeur = valeur;
	}
}
