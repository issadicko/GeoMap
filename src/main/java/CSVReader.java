import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class CSVReader {

    private static final int DIVIDER_VALUE = 100000;

    public static ArrayList<LPoint> parse(String filename){

        System.out.println("Chargement du fichier : "+filename+" ...");

        ArrayList<LPoint> liste = new ArrayList<>();
        DecimalFormat format = new DecimalFormat();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(symbols);

        try {

            FileReader reader = new FileReader(new File(filename));
            BufferedReader bufferedReader  = new BufferedReader(reader);
            String line;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (i++ == 0) continue;

                String[] tokens = line.split(",");

                double
                        lng      = Double.parseDouble(tokens[0]) / DIVIDER_VALUE,
                        lat      = Double.parseDouble(tokens[1]) / DIVIDER_VALUE,
                        prof     = Double.parseDouble(tokens[2]);

                LPoint point = new LPoint();
                point.setLatitude(lat);
                point.setLongitude(lng);
                point.setProfondeur(prof);

                liste.add(point);

            }

        } catch (IOException e ) {
            e.printStackTrace();
        }

        return liste;
    }


    public static ArrayList<LPoint> parse2(String filename){

        System.out.println("Chargement du fichier : "+filename+" ...");

        ArrayList<LPoint> liste = new ArrayList<>();
        DecimalFormat format = new DecimalFormat();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(symbols);

        try {

            FileReader reader = new FileReader(new File(filename));
            BufferedReader bufferedReader  = new BufferedReader(reader);
            String line;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (i++ == 0) continue;

                String[] tokens = line.split(",");

                String nom       = tokens[0];

                double
                        lng      = Double.parseDouble(tokens[1]) / DIVIDER_VALUE,
                        lat      = Double.parseDouble(tokens[2]) / DIVIDER_VALUE,
                        prof     = Double.parseDouble(tokens[3]);

                LPoint point = new LPoint();
                point.setLatitude(lat);
                point.setLongitude(lng);
                point.setProfondeur(prof);
                point.setNom(nom);

                liste.add(point);

            }

        } catch (IOException e ) {
            e.printStackTrace();
        }

        return liste;
    }

}
