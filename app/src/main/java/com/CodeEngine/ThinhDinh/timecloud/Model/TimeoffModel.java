package com.CodeEngine.ThinhDinh.timecloud.Model;

/**
 * Created by Administrator on 3/23/2018.
 */

public class TimeoffModel {
    private long startTime,endTime;
    private long replyTime;
    private String state;

    public TimeoffModel(long startTime, long endTime, long replyTime, String state) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.replyTime = replyTime;
        this.state = state;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getReplyTime() {
        return replyTime;
    }

    public void setReplyTime(long replyTime) {
        this.replyTime = replyTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
