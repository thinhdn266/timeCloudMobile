package com.CodeEngine.ThinhDinh.timecloud.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.CodeEngine.ThinhDinh.timecloud.Model.CategoryModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.TaskModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.TimeModel;
import com.google.android.gms.tasks.Task;

import java.util.Random;

public class LocalService extends Service {

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */

    public class LocalBinder extends Binder {
        public LocalService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocalService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * method for clients
     */
    public TimeModel getTimeModel() {
        return time;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public void startTimer() {
        updateTimerThread = new Runnable() {
            @Override
            public void run() {
                timerRunning = true;
                timeInMillisecond = System.currentTimeMillis() - startTime;
                time = new TimeModel((int) (timeInMillisecond / 1000));
                timeHandler.postDelayed(updateTimerThread, 1000);
            }
        };
        timeHandler.postDelayed(updateTimerThread, 1000);
    }

    public void stopTimer() {
        timeHandler.removeCallbacks(updateTimerThread);
        timerRunning = false;
    }

    public CategoryModel getCategory() {
        return category;
    }

    public void setCategory(CategoryModel category) {
        this.category = category;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Boolean getTimerRunning() {
        return timerRunning;
    }

    public void setTimerRunning(Boolean timerRunning) {
        this.timerRunning = timerRunning;
    }

    private final IBinder mBinder = new LocalBinder();
    private final Random mGenerator = new Random();
    private TimeModel time;
    private Handler timeHandler = new Handler();

    private long startTime = 0L, timeInMillisecond = 0L;
    private Runnable updateTimerThread;
    private Boolean timerRunning = false;
    private CategoryModel category;
}