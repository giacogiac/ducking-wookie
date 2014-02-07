package poc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Util {

	public static ArrayList<CoursBoursier> GetCoursFromCSV() {

		ArrayList<CoursBoursier> coursBoursiers = new ArrayList<CoursBoursier>();

		FileReader fileReader;
		try {
			fileReader = new FileReader(new File("cours.csv"));

			BufferedReader buffer = new BufferedReader(fileReader);
			String line = null;
			do {
				try {
					line = buffer.readLine();

					if (null != line) {
						String[] data = line.split(";");

						coursBoursiers.add(new CoursBoursier(System
								.currentTimeMillis(), Double
								.parseDouble(data[3]), data[1], data[2],
								data[0]));
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} while (null != line);
			buffer.close();
			fileReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return coursBoursiers;
	}
}
