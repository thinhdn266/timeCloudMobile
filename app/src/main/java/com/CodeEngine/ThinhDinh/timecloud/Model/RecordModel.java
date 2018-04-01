package com.CodeEngine.ThinhDinh.timecloud.Model;

import java.io.Serializable;

/**
 * Created by Administrator on 3/8/2018.
 */

public class RecordModel implements Serializable {
    private int id;
    private TimeModel time;
    private long startTime;
    private long endTime;
    private TaskModel task;


    public RecordModel(int id, TimeModel time, long startTime, long endTime, TaskModel task) {
        this.id = id;
        this.time = time;
        this.startTime = startTime;
        this.endTime = endTime;
        this.task = task;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TimeModel getTime() {
        return time;
    }

    public void setTime(TimeModel time) {
        this.time = time;
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

    public TaskModel getTask() {
        return task;
    }

    public void setTask(TaskModel task) {
        this.task = task;
    }
}
