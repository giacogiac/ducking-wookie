package poc;

import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {
		ArrayList<CoursBoursier> coursBoursiers = Util.GetCoursFromCSV();

		for (CoursBoursier coursBoursier : coursBoursiers) {
			System.out.println(coursBoursier);
		}
	}
}
