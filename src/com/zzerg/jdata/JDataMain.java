package com.zzerg.jdata;

import java.io.*;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.text.DecimalFormat;

import static java.lang.System.*;

/**
 * User: Admin
 * Date: 27.09.13
 * Time: 22:15
 */
public class JDataMain {

    private static String dataDirName = "../data/";

    private static String printFilename = "result.csv";
    private static BufferedWriter bufferedWriter = null;

    private static final int MIN_TICKS_START_PEAK = 5;
    private static final int MAX_TICKS_END_PEAK = 3;
    private static final int MIN_PEAK_POWER = 10;
    private static final String CSV_SEPARATOR = ";";

    private static double mergeableFreqLevel = 0.05;

    private static NumberFormat doubleFormat_2 = new DecimalFormat("#.##", new DecimalFormatSymbols(new Locale("en")));

    public static void main(String[] args) throws Exception {

        /* Init stuff */
        log("JData v0.4");
        int mode = 42;
        double minFreq = 10;
        double maxFreq = 20;

        /* Read config file */
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("jtool.properties"));
            mode = (int) readDoubleProperty(prop, "mode");
        } catch (Exception ex) {
            ex.printStackTrace();
            err("Failed to load config: " + ex.getMessage());
        }

        /* 1=simple, 2=auto */
        if (mode == 1) {
            minFreq = readDoubleProperty(prop, "min.freq");
            maxFreq = readDoubleProperty(prop, "max.freq");
            log("Using fixed frequency range: " + minFreq + ", " + maxFreq);
        } else {
            log("Using auto-peak mode");
        }

        /* Output file */
        bufferedWriter = new BufferedWriter(new FileWriter(printFilename));
        PeakTool peakTool = new PeakTool(MIN_TICKS_START_PEAK, MAX_TICKS_END_PEAK);

        /* Read files */
        List<PeakRecord> globalPeaks = new ArrayList<PeakRecord>();
        List<FileResultRecord> fileResults = new ArrayList<FileResultRecord>();
        File dataDir = new File(dataDirName);
        for (final File fileEntry : dataDir.listFiles()) {
            if (!fileEntry.isDirectory()) {
                Datafile df = Datafile.loadData(fileEntry.getAbsolutePath());
                if (mode == 1) {
                    double power = peakTool.sumPower(df, minFreq, maxFreq);
                    fileResults.add(new FileResultRecord(df.ev, power));
                } else {
                    List<PeakRecord> peaks = peakTool.findPeaks(df);
                    peaks = peakTool.mergeClosePeaks(peaks);
                    FileResultRecord fr = new FileResultRecord(df.ev, 0.0);
                    fr.peaks = peaks;
                    fileResults.add(fr);
                    log(df.ev + " => " + peakTool.toString(peaks));
                    peakTool.totalizePeaks(peaks, globalPeaks);
                }

//                err.println("Bad data dir: " + fileEntry.getAbsolutePath());
            }
        }

        /* Total output */
        Collections.sort(fileResults);
        if (mode == 1) {
            for (FileResultRecord fr: fileResults) {
                println(fr.ev + CSV_SEPARATOR + doubleFormat_2.format(fr.rangePower));
            }
        } else {
            log(" TOTAL PEAKS: => " + peakTool.toString(globalPeaks));
            StringBuilder sb = new StringBuilder();

            /* Header */
            sb.append("file\\peak").append(CSV_SEPARATOR);
            for (PeakRecord globalPeak: globalPeaks) {
                sb.append(doubleFormat_2.format(globalPeak.freq)).append(CSV_SEPARATOR);
            }
            println(sb.toString());

            /* Rows */
            for (FileResultRecord fr: fileResults) {
                sb.setLength(0);
                sb.append(fr.ev).append(CSV_SEPARATOR);
                for (PeakRecord globalPeak: globalPeaks) {
                    double peakFreq = globalPeak.freq;
                    boolean found = false;
                    if (fr.peaks != null) {
                        for (PeakRecord pr: fr.peaks) {
                            if (Math.abs(peakFreq - pr.freq)/peakFreq < mergeableFreqLevel) {
                                sb.append(doubleFormat_2.format(pr.power)).append(CSV_SEPARATOR);
                                found = true;
                                break;
                            }
                        } // for peaks in result record
                    }
                    if (!found) {
                        sb.append("0.0").append(CSV_SEPARATOR);
                    }
                } // for global peaks
                println(sb.toString());
            }
        }


        bufferedWriter.close();
    }


//    private static double sum(Datafile df, double freqMin, double freqMax) {
//        println("Analyzing: " + df.name);
//        double prevFreq = df.freqs[0];
//        double curFreq;
//
//        double startPeakFreq = -1.0;
//        double peakPower = 0.0;
//
//        double d = 0.0;
//        for (int i = 1; i < df.length; i++) {
//            curFreq = df.freqs[i];
//            /* Sum in given interval */
//            if (curFreq >= freqMin && curFreq < freqMax) {
//                d += (curFreq - prevFreq) * df.tics[i];
//            }
//            prevFreq = curFreq;
//        }
//
//        return d;
//    }



    public static void log(String s) {
        out.println(s);
    }

    public static void println(String s) {
        if (bufferedWriter != null) {
            try {
                bufferedWriter.append(s).append("\n");
            } catch (IOException ex) {
                ex.printStackTrace();
                err("Failed to printf: " + s);
            }
        }
        log(s);
    }

    public static void err(String s) {
        err.print("ERROR: ");
        err.println(s);
    }


    public static double readDoubleProperty(Properties properties, String key) {
        String s = properties.getProperty(key);
        if (s != null) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ex) {
                err("Bad double: [" + s + "] for key " + key);
                // ignore
            }
        }
        return 0.0;
    }

}
