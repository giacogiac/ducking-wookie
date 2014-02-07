package poc;

import java.io.Serializable;

public class CoursBoursier implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2670013256597336386L;

	public long time;
	public double valeur;
	public String name;
	public String company;
	public String stockExchanges;

	public CoursBoursier(long time, double valeur, String name, String company,
			String stockExchanges) {
		super();
		this.time = time;
		this.valeur = valeur;
		this.name = name;
		this.company = company;
		this.stockExchanges = stockExchanges;
	}

	@Override
	public String toString() {
		return "CoursBoursier [time=" + time + ", valeur=" + valeur + ", name="
				+ name + ", company=" + company + ", stockExchanges="
				+ stockExchanges + "]";
	}

}
