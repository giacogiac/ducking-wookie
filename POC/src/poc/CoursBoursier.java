package poc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CoursBoursier implements Serializable, Comparable<CoursBoursier> {

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

    public static List<CoursBoursier> parseCSV(String file) {

        ArrayList<CoursBoursier> coursBoursiers = new ArrayList<CoursBoursier>();

        FileReader fileReader;
        try {
            fileReader = new FileReader(new File(file));

            BufferedReader buffer = new BufferedReader(fileReader);
            String line = null;
            do {
                try {
                    line = buffer.readLine();

                    if (null != line) {
                        String[] data = line.split(";");

                        coursBoursiers.add(new CoursBoursier(System
                                .currentTimeMillis(), data[0].trim(), data[1]
                                .trim(), Double.parseDouble(data[2])));
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

    /**
     * Natural ordering des cours boursiers
     * @param cours Autre cours à comparer
     * @return -1;0;1 si less;equ;grt
     */
    @Override
    public int compareTo(CoursBoursier cours) {
        assert ISIN.equals(cours.ISIN);
        
        if(time == cours.time) {
            return 0;
        } else if(time < cours.time) {
            return -1;
        }
        return 1;
    }

}
