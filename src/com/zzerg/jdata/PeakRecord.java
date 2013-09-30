package com.zzerg.jdata;

/**
 * User: Admin
 * Date: 29.09.13
 * Time: 10:55
 */
public class PeakRecord implements Comparable<PeakRecord>{
    public double freq;
    public double power;


    public PeakRecord() {}

    public PeakRecord(double f, double p) {
        this.freq = f;
        this.power = p;
    }


    @Override
    public int compareTo(PeakRecord other){
        if (other == null) {
            return 1;
        }
        if (this.freq == other.freq) {
            return 0;
        }
        return (this.freq > other.freq)?1:-1;
    }
}
