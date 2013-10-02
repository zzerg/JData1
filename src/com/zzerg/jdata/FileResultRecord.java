package com.zzerg.jdata;

import java.util.List;

/**
 * User: Admin
 * Date: 02.10.13
 * Time: 23:09
 */
public class FileResultRecord implements Comparable<FileResultRecord> {

    public double ev;
    public double rangePower;
    public List<PeakRecord> peaks;

    public FileResultRecord (double ev, double p) {
        this.ev = ev;
        this.rangePower = p;
        this.peaks = null;
    }


    @Override
    public int compareTo(FileResultRecord other){
        if (other == null) {
            return 1;
        }
        if (this.ev == other.ev) {
            return 0;
        }
        return (this.ev > other.ev)?1:-1;
    }

}
