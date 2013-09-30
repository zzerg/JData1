package com.zzerg.jdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: Admin
 * Date: 29.09.13
 * Time: 10:56
 */
public class PeakTool {

    private int minValueStartPeak;
    private int maxValueEndPeak;
    private double mergeableFreqLevel = 0.05;


    public PeakTool(int min, int max) {
        minValueStartPeak = min;
        maxValueEndPeak = max;
    }

    public List<PeakRecord> findPeaks(Datafile df) {
        JDataMain.log("Analyzing: " + df.name);
        double prevFreq = df.freqs[0];
        double curFreq;

        double startPeakFreq = -1.0;
        double peakPower = 0.0;

        List<PeakRecord> peakList = new ArrayList<PeakRecord>();

        for (int i = 1; i < df.length; i++) {
            curFreq = df.freqs[i];

            if (df.tics[i] > minValueStartPeak && startPeakFreq < 0.0) {
                /* Begin peak */
                startPeakFreq = curFreq;
            } else if (startPeakFreq > 0.0 && df.tics[i] < maxValueEndPeak) {
                /* End peak */
                // TODO: weighted peak center calculation!
                PeakRecord pr = new PeakRecord(0.5 * (startPeakFreq + curFreq), peakPower);
                peakList.add(pr);
                if (peakPower > 10) {
                    JDataMain.log("Peak detected around: " + pr.freq + ", power: " + peakPower);
                }
                startPeakFreq = -1.0;
                peakPower = 0.0;
            }

            /* Inside peak */
            if (startPeakFreq > 0.0) {
                peakPower += (curFreq - prevFreq) * df.tics[i];
            }
            prevFreq = curFreq;
        } // for freq

        return peakList;
    }


    public List<PeakRecord> mergeClosePeaks(List<PeakRecord> peakList) {
        if (peakList == null) {
            return null;
        }

        List<PeakRecord> res = new ArrayList<PeakRecord>();
        for (PeakRecord p: peakList) {
            boolean merged = false;
            for (PeakRecord r: res) {
                if (Math.abs(p.freq - r.freq)/r.freq < mergeableFreqLevel) {
                    JDataMain.log("Merging peaks: " + p.freq + " and " + r.freq);
                    r.freq = (r.freq * r.power + p.freq * p.power) / (r.power + p.power);
                    r.power += p.power;
                    JDataMain.log("Merged peak: " + r.freq + ", power=" + r.power);
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                JDataMain.log("New peak: " + p.freq);
                res.add(p);
            }
        }
        return res;
    }

    public String toString(List<PeakRecord> peakList) {
        StringBuilder sb = new StringBuilder();
        if (peakList != null) {
            for (PeakRecord pr: peakList) {
                sb.append(pr.freq).append("->").append(pr.power).append(", ");
            }
        }
        return sb.toString();
    }


    public void totalizePeaks(List<PeakRecord> peakList, List<PeakRecord> totalPeaksList) {
        if (peakList == null) {
            return;
        }

        for (PeakRecord p: peakList) {
            boolean matched = false;
            for (PeakRecord r: totalPeaksList) {
                if (Math.abs(p.freq - r.freq)/r.freq < mergeableFreqLevel) {
                    JDataMain.log("Matching peaks: " + p.freq + " and " + r.freq);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                JDataMain.log("New peak: " + p.freq);
                totalPeaksList.add(new PeakRecord(p.freq, 0.0));
            }
        }
        Collections.sort(totalPeaksList);
    }

    public int getMinValueStartPeak() {
        return minValueStartPeak;
    }

    public void setMinValueStartPeak(int minValueStartPeak) {
        this.minValueStartPeak = minValueStartPeak;
    }

    public int getMaxValueEndPeak() {
        return maxValueEndPeak;
    }

    public void setMaxValueEndPeak(int maxValueEndPeak) {
        this.maxValueEndPeak = maxValueEndPeak;
    }
}
