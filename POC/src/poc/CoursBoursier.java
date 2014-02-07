package poc;

import java.io.Serializable;

public class CoursBoursier implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2670013256597336386L;

	public long time;
	public String ISIN;
	public String company;
	public double cotation;

	public CoursBoursier(long time, String iSIN, String company, double cotation) {
		super();
		this.time = time;
		ISIN = iSIN;
		this.company = company;
		this.cotation = cotation;
	}

	@Override
	public String toString() {
		return "CoursBoursier [time=" + time + ", ISIN=" + ISIN + ", company="
				+ company + ", cotation=" + cotation + "]";
	}

}
