package com.CodeEngine.ThinhDinh.timecloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.CodeEngine.ThinhDinh.timecloud.Model.CategoryModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.RecordModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.TimeModel;
import com.CodeEngine.ThinhDinh.timecloud.Utils.Consts;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 1/31/2018.
 */

public class NewTaskActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.choose_project_view:
                Intent intent = new Intent(NewTaskActivity.this, ProjectListActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (record != null) {
            Intent intent = new Intent(NewTaskActivity.this, MainActivity.class);
            startActivity(intent);
            NewTaskActivity.this.finish();
        } else {
            Intent intent = new Intent(NewTaskActivity.this, MainActivity.class);
            intent.putExtra("startTime", startTime);
            if (category != null) {
                intent.putExtra("category", category);
            }
            if (!taskName.getText().toString().equals(""))
                intent.putExtra("name", taskName.getText().toString());
            startActivity(intent);
            NewTaskActivity.this.finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        timer = findViewById(R.id.timer_textview);
        date = findViewById(R.id.date_textview);

        chooseProjectLayout = findViewById(R.id.choose_project_view);
        taskName = findViewById(R.id.task_name);
        projectIcon = findViewById(R.id.project_icon);
        chooseProjectTextView = findViewById(R.id.choose_project_category);
        rightArrow = findViewById(R.id.right_arrow);

        chooseProjectLayout.setOnClickListener(this);

        startTime = System.currentTimeMillis();
        getIntentExtras();
        runTrackTimer();
        getTokenUserId();
        setTodayTextView();

        timeHandler.postDelayed(updateTimerThread, 1000);
    }

    @Override
    protected void onDestroy() {
        timeHandler.removeCallbacks(updateTimerThread);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        getIntentExtras();
        super.onResume();
    }

    private void getIntentExtras() {
        if (getIntent().hasExtra("category")) {
            category = (CategoryModel) getIntent().getSerializableExtra("category");

            String projectCategory = category.getProject().getName() + " - " + category.getName();
            chooseProjectTextView.setText(projectCategory);
            chooseProjectTextView.setTextColor(getResources().getColor(R.color.colorBlackText));
            projectIcon.setVisibility(View.VISIBLE);
            projectIcon.setBackgroundColor(Integer.parseInt(category.getProject().getColor()));
            rightArrow.setVisibility(View.GONE);
        } else if (getIntent().hasExtra("runningRecord")) {
            startTime = getIntent().getLongExtra("startTime", 0);
            record = (RecordModel) getIntent().getSerializableExtra("runningRecord");

            taskName.setText(record.getTask().getName().equals("null") ? "No name" : record.getTask().getName());
            chooseProjectTextView.setText(record.getTask().getProjectCategory());
            chooseProjectTextView.setTextColor(getResources().getColor(R.color.colorBlackText));
            if (record.getTask().getCategory() == null) {
                projectIcon.setBackgroundColor(getResources().getColor(R.color.colorGrayText));
            } else {
                projectIcon.setBackgroundColor(Integer.parseInt(record.getTask().getCategory().getProject().getColor
                        ()));
            }
            projectIcon.setVisibility(View.VISIBLE);
            rightArrow.setVisibility(View.GONE);
        }
    }

    private void getTokenUserId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        userId = prefs.getString("userId", Consts.DEFAULT_KEY);
        token = prefs.getString("token", Consts.DEFAULT_KEY);
        loginType = prefs.getString("loginType", Consts.DEFAULT_KEY);
    }

    private void setTodayTextView() {
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm a");
        String currentTime = sdf.format(new Date());
        date.setText("Today, " + currentTime);
    }

    private void runTrackTimer() {
        updateTimerThread = new Runnable() {
            @Override
            public void run() {
                timeInMillisecond = System.currentTimeMillis() - startTime;
                time = new TimeModel((int) (timeInMillisecond / 1000));
                timer.setText(time.toText());
                timeHandler.postDelayed(updateTimerThread, 1000);
            }
        };
    }

    private ImageButton rightArrow;
    private ImageView projectIcon;
    private TextView timer, date, chooseProjectTextView;
    private EditText taskName;
    private RelativeLayout chooseProjectLayout;
    private String token, userId, loginType;
    private ProgressDialog nDialog;
    private Handler timeHandler = new Handler();
    private long startTime = 0L, timeInMillisecond = 0L;
    private Runnable updateTimerThread;
    private TimeModel time;
    private CategoryModel category;
    private RecordModel record;
}
