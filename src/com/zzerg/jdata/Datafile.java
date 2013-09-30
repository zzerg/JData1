package com.zzerg.jdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Admin
 * Date: 27.09.13
 * Time: 22:33
 */
public class Datafile {
    public static final int LINES = 100000;

    public int length;
    public String name;
    public double ev;
    public double[] freqs;
    public int[] tics;

    private static final String FILENAME_TEMPLATE = ".*?_(-?\\d+)(_(\\d+))?eV\\d*";
    private static Pattern filenamePattern = null;

    /**
     * Parses eV value from data filename
     *
     * @param name filename to parse
     * @return parsed value or 0.0 if parsing failed
     */
    public static double makeEv(String name) {
        if (filenamePattern == null) {
            filenamePattern = Pattern.compile(FILENAME_TEMPLATE);
        }
        Matcher filenameMatcher = filenamePattern.matcher(name);
        if (filenameMatcher.matches()) {
            StringBuilder sb = new StringBuilder();
            if (filenameMatcher.group(1) != null) {
                sb.append(filenameMatcher.group(1));
            } else {
                JDataMain.err("Cannot find eV in: [" + name + "]");
            }
            if (filenameMatcher.group(3) != null) {
                sb.append(".").append(filenameMatcher.group(3));
            }
            try {
                return Double.parseDouble(sb.toString());
            } catch (NumberFormatException ex) {
                JDataMain.err("Cannot parse eV in: [" + name + "]");
            }
        }
        return 0.0d;        // default value, even if failed!
    }



    public static Datafile loadData(String filename) throws IOException {
        JDataMain.log("Loading file: " + filename);
        Datafile df = new Datafile();
        df.length = LINES;
        if (filename.lastIndexOf(File.separator) >=0 ) {
            df.name = filename.substring(filename.lastIndexOf(File.separator) + 1);
        } else {
            df.name = filename;
        }
        if (df.name.contains(".")) {
            df.name = df.name.substring(0, df.name.indexOf("."));
        }
        df.ev = Datafile.makeEv(df.name);

        df.freqs = new double[LINES];
        df.tics = new int[LINES];

        int k = 0;
        String[]  sa;
        double d;
        int i;
        BufferedReader br = new BufferedReader(new FileReader(filename));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 3) {
                    sa = line.split("\t");
                    if (sa == null || sa.length != 2) {
                        JDataMain.err("Bad line: [" + line + "]");
                        continue;
                    }
                    try {
                        d = Double.parseDouble(sa[0]);
                        i = Integer.parseInt(sa[1]);
                        df.freqs[k] = d;
                        df.tics[k] = i;
                        k++;
                    } catch (NumberFormatException ex) {
                        JDataMain.err("Bad value: [" + line + "]");
                        continue;
                    }
                }
            } // while line
            JDataMain.log("Actually read lines: " + k);
        } finally {
            br.close();
        }
        return df;
    }

}
