package com.CodeEngine.ThinhDinh.timecloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.CodeEngine.ThinhDinh.timecloud.Utils.Consts;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.CodeEngine.ThinhDinh.timecloud.Model.CategoryModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.RecordModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.TimeModel;
import com.android.volley.toolbox.Volley;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 3/6/2018.
 */

public class TaskInfoActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.choose_project_view:
                Intent intent = new Intent(this, ProjectListActivity.class);
                intent.putExtra("update", true);
                startActivity(intent);
                break;
            case R.id.delete_btn:
                deleteTask();
                break;
            case R.id.start_date_time:
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(startTimeListener)
                        .setInitialDate(new Date(record.getStartTime()))
//                        .setMinDate(new Date(startTime - limitTime))
                        .setMaxDate(new Date(record.getEndTime()))
                        .setTheme(SlideDateTimePicker.HOLO_LIGHT)
                        .setIndicatorColor(getResources().getColor(R.color.colorAquaIcon))
                        .build()
                        .show();
                recordUpdated = true;
                break;
            case R.id.end_date_time:
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(endTimeListener)
                        .setInitialDate(new Date(record.getEndTime()))
                        .setMinDate(new Date(record.getStartTime()))
//                        .setMaxDate(new Date(endTime + limitTime))
                        .setTheme(SlideDateTimePicker.HOLO_LIGHT)
                        .setIndicatorColor(getResources().getColor(R.color.colorAquaIcon))
                        .build()
                        .show();
                recordUpdated = true;
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (recordUpdated) updateRecord();
        else if (taskCategoryUpdated || taskNameUpdated) updateTask();
        else {
            startActivity(new Intent(TaskInfoActivity.this, MainActivity.class));
            TaskInfoActivity.this.finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_info);

        deleteBtn = findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(this);

        timer = findViewById(R.id.timer_textview);
        startTimeTextView = findViewById(R.id.start_time_textview);
        startDateTextView = findViewById(R.id.start_date_textview);
        endTimeTextView = findViewById(R.id.end_time_textview);
        endDateTextView = findViewById(R.id.end_date_textview);
        chooseProjectTextView = findViewById(R.id.choose_project_category);
        taskName = findViewById(R.id.task_name);
        projectIcon = findViewById(R.id.project_icon);

        taskName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                taskNameUpdated = true;
            }
        });

        startDateTime = findViewById(R.id.start_date_time);
        startDateTime.setOnClickListener(this);
        endDateTime = findViewById(R.id.end_date_time);
        endDateTime.setOnClickListener(this);

        chooseProjectLayout = findViewById(R.id.choose_project_view);

        chooseProjectLayout.setOnClickListener(this);

        queue = Volley.newRequestQueue(this);

        getTokenUserId();
        getIntentExtras();
        setDateTimeListener();
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

    private JSONObject createUpdateRecordJson() {
        JSONObject jsRecord = new JSONObject();
        try {
            jsRecord.put("id", record.getId());
            jsRecord.put("starttime", record.getStartTime());
            jsRecord.put("endtime", record.getEndTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsRecord;
    }

    private JSONObject createUpdateTaskJosn() {
        JSONObject jsTask = new JSONObject();
        JSONObject jsCategory = new JSONObject();
        try {
            if (taskCategoryUpdated) {
                jsCategory.put("id", category.getId());
                jsTask.put("category", jsCategory);
            }
            if (taskNameUpdated) {
                jsTask.put("name", record.getTask().getName());
            }
            jsTask.put("id", record.getTask().getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsTask;
    }

    private void deleteTask() {
        final String deleteTaskURL = Consts.URL + "record/" + record.getId();
        showPDialog();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE,
                deleteTaskURL, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        hidePDialog();
                        Intent intent = new Intent(TaskInfoActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent
                                .FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        TaskInfoActivity.this.finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePDialog();
                displayMessage("Error");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", token);
                return params;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void displayMessage(String toastString) {
        Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show();
    }

    private void getIntentExtras() {
        if (getIntent().hasExtra("record")) {
            record = (RecordModel) getIntent().getSerializableExtra("record");

            startTime = record.getStartTime();
            Log.e("startTime", String.valueOf(startTime));
            endTime = record.getEndTime();

            dateFormat = new SimpleDateFormat("EEE, dd MMM");
            timeFormat = new SimpleDateFormat("KK:mm a");

            Calendar todayCalendar = Calendar.getInstance();
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTimeInMillis(record.getStartTime());
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTimeInMillis(record.getEndTime());

            timer.setText(record.getTime().toText());
            startDateTextView.setText(dateFormat.format(todayCalendar.getTime()).equals
                    (dateFormat.format(startCalendar.getTime())) ? "Today" : dateFormat.format
                    (startCalendar.getTime()));
            startTimeTextView.setText(timeFormat.format(startCalendar.getTime()));
            endDateTextView.setText(dateFormat.format(todayCalendar.getTime()).equals(dateFormat
                    .format(endCalendar.getTime())) ? "Today" : dateFormat.format(endCalendar
                    .getTime()));
            endTimeTextView.setText(timeFormat.format(endCalendar.getTime()));

            taskName.setText(record.getTask().getName());
            chooseProjectTextView.setText(record.getTask().getProjectCategory());
            if (record.getTask().getCategory() != null)
                projectIcon.setBackgroundColor(Integer.parseInt(record.getTask().getCategory()
                        .getProject().getColor()));
        } else if (getIntent().hasExtra("category")) {
            category = (CategoryModel) getIntent().getSerializableExtra("category");

            String projectCategory = category.getProject().getName() + " - " + category.getName();
            chooseProjectTextView.setText(projectCategory);
            chooseProjectTextView.setTextColor(getResources().getColor(R.color.colorBlackText));
            projectIcon.setBackgroundColor(Integer.parseInt(category.getProject().getColor()));

            taskCategoryUpdated = true;
        }
    }

    private void getTokenUserId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        userId = prefs.getString("userId", Consts.DEFAULT_KEY);
        token = prefs.getString("token", Consts.DEFAULT_KEY);
        loginType = prefs.getString("loginType", Consts.DEFAULT_KEY);
    }

    private void hidePDialog() {
        if (nDialog != null) {
            nDialog.dismiss();
            nDialog = null;
        }
    }

    private void setDateTimeListener() {
        final Date today = new Date();
        startTimeListener = new SlideDateTimeListener() {

            @Override
            public void onDateTimeSet(Date date) {
                if (date.getTime() < record.getEndTime()) {
                    if (dateFormat.format(date).equals(dateFormat.format(today)))
                        startDateTextView.setText("Today");
                    else
                        startDateTextView.setText(dateFormat.format(date));
                    startTimeTextView.setText(timeFormat.format(date));
                    record.setStartTime(date.getTime());
                    Log.e("startTimeAfter", String.valueOf(record.getStartTime()));
                    record.setTime(new TimeModel((int) (record.getEndTime() - record.getStartTime
                            ()) / 1000));
                    timer.setText(record.getTime().toText());
                } else displayMessage("Start time is unvalid, please set End time first");
            }

            @Override
            public void onDateTimeCancel() {
                // Overriding onDateTimeCancel() is optional.
            }
        };

        endTimeListener = new SlideDateTimeListener() {

            @Override
            public void onDateTimeSet(Date date) {
                if (date.getTime() > record.getStartTime()) {
                    if (dateFormat.format(date).equals(dateFormat.format(today)))
                        endDateTextView.setText("Today");
                    else
                        endDateTextView.setText(dateFormat.format(date));

                    endTimeTextView.setText(timeFormat.format(date));
                    record.setEndTime(date.getTime());

                    record.setTime(new TimeModel((int) (record.getEndTime() - record.getStartTime
                            ()) / 1000));
                    timer.setText(record.getTime().toText());
                } else displayMessage("End time is unvalid, please set Start time first");
            }

            @Override
            public void onDateTimeCancel() {
                // Overriding onDateTimeCancel() is optional.
            }
        };
    }

    private void showPDialog() {
        nDialog = new ProgressDialog(this, R.style.MyDialogTheme);
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
        nDialog.show();
    }

    private void updateRecord() {
        JSONObject jsRecord = createUpdateRecordJson();
        showPDialog();
        final String createRecordUrl = Consts.URL + "record";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                createRecordUrl, jsRecord,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        hidePDialog();
                        if (taskCategoryUpdated || taskNameUpdated) updateTask();
                        else {
                            Intent intent = new Intent(TaskInfoActivity.this, MainActivity.class);
                            intent.putExtra("taskListUpdate", true);
                            startActivity(intent);
                            TaskInfoActivity.this.finish();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", token);
                return params;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void updateTask() {
        JSONObject jsTask = createUpdateTaskJosn();
        Log.e("task", jsTask.toString());
        showPDialog();
        final String createRecordUrl = Consts.URL + "task";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                createRecordUrl, jsTask,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        hidePDialog();
                        Intent intent = new Intent(TaskInfoActivity.this, MainActivity.class);
                        intent.putExtra("taskListUpdate", true);
                        startActivity(intent);
                        TaskInfoActivity.this.finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", token);
                return params;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private TextView timer, startTimeTextView, startDateTextView, endTimeTextView,
            endDateTextView, chooseProjectTextView;
    private EditText taskName;
    private RelativeLayout chooseProjectLayout;
    private ImageButton deleteBtn;
    private ImageView projectIcon;
    private RecordModel record;
    private CategoryModel category;
    private ProgressDialog nDialog;
    private String token, userId, loginType;
    private SimpleDateFormat dateFormat, timeFormat;
    private LinearLayout startDateTime, endDateTime;
    private SlideDateTimeListener startTimeListener, endTimeListener;
    private long startTime, endTime;
    private Boolean recordUpdated = false, taskCategoryUpdated = false, taskNameUpdated = false;
    private RequestQueue queue;
    static final long limitTime = 3 * 24 * 3600 * 1000;                //limit time is 3 days
}
