package com.esri.rttest.mon;

public class Sample {

    long cnt;
    long ts;

    public Sample(long cnt, long ts) {
        this.cnt = cnt;
        this.ts = ts;
    }

    public long getCnt() {
        return cnt;
    }

    public void setCnt(long cnt) {
        this.cnt = cnt;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }
}
