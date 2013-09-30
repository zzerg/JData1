package com.zzerg.jdata;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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


    public static void main(String[] args) throws Exception {

        /* Init stuff */
        log("JData v0.4");
        double minFreq = 10;
        double maxFreq = 20;

        bufferedWriter = new BufferedWriter(new FileWriter(printFilename));
        PeakTool peakTool = new PeakTool(MIN_TICKS_START_PEAK, MAX_TICKS_END_PEAK);

        /* Read files */
        List<PeakRecord> globalPeaks = new ArrayList<PeakRecord>();
        File dataDir = new File(dataDirName);
        for (final File fileEntry : dataDir.listFiles()) {
            if (!fileEntry.isDirectory()) {
                Datafile df = Datafile.loadData(fileEntry.getAbsolutePath());
                List<PeakRecord> peaks = peakTool.findPeaks(df);
                peaks = peakTool.mergeClosePeaks(peaks);
                println(df.ev + " => " + peakTool.toString(peaks));
                peakTool.totalizePeaks(peaks, globalPeaks);


//                err.println("Bad data dir: " + fileEntry.getAbsolutePath());
            }
        }
        println(" TOTAL PEAKS: => " + peakTool.toString(globalPeaks));
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



}
