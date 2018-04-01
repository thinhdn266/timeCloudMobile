package com.CodeEngine.ThinhDinh.timecloud.Model;

import java.io.Serializable;

/**
 * Created by Administrator on 2/12/2018.
 */

public class TimeModel implements Serializable {
    private int hour, min, sec;

    public TimeModel(int hour, int min, int sec) {
        this.hour = hour;
        this.min = min;
        this.sec = sec;
    }

    public TimeModel(long sec) {
        this.sec = (int) (sec % 60);
        min = (int) (sec / 60);
        hour = min / 60;
        min = min % 60;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getSec() {
        return sec;
    }

    public void setSec(int sec) {
        this.sec = sec;
    }

    public String toText() {
        return (String.format("%3d", hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec));
    }

    public String secToText() {
        return String.format("%02d", sec);
    }

    public String minToText() {
        return String.format("%02d", min);
    }

    public String hourToText() {
        return String.format("%3d", hour);
    }
}
