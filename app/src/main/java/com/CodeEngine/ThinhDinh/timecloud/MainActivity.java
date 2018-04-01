package com.CodeEngine.ThinhDinh.timecloud;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.CodeEngine.ThinhDinh.timecloud.Utils.Consts;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.CodeEngine.ThinhDinh.timecloud.Adapter.CategoryListViewAdapter;
import com.CodeEngine.ThinhDinh.timecloud.Adapter.TaskListViewAdapter;
import com.CodeEngine.ThinhDinh.timecloud.Model.CategoryModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.ProjectModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.RecordModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.TaskModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.TimeModel;
import com.CodeEngine.ThinhDinh.timecloud.Adapter.SwipeLeftRightMenuListView.SwipeMenu;
import com.CodeEngine.ThinhDinh.timecloud.Adapter.SwipeLeftRightMenuListView.SwipeMenuCreator;
import com.CodeEngine.ThinhDinh.timecloud.Adapter.SwipeLeftRightMenuListView.SwipeMenuItem;
import com.CodeEngine.ThinhDinh.timecloud.Adapter.SwipeLeftRightMenuListView.SwipeMenuListView;
import com.CodeEngine.ThinhDinh.timecloud.Adapter.SwipeLeftRightMenuListView.TaskListView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.parseInt;


/**
 * Created by Administrator on 1/17/2018.
 */

public class MainActivity extends AppCompatActivity implements NavigationView
        .OnNavigationItemSelectedListener, View.OnClickListener {

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.monday_textview:
                highlightClickedDay(MONDAY);
                break;
            case R.id.tuesday_textview:
                highlightClickedDay(TUESDAY);
                break;
            case R.id.wednesday_textview:
                highlightClickedDay(WEDNESDAY);
                break;
            case R.id.thursday_textview:
                highlightClickedDay(THURSDAY);
                break;
            case R.id.friday_textview:
                highlightClickedDay(FRIDAY);
                break;
            case R.id.saturday_textview:
                highlightClickedDay(SATURDAY);
                break;
            case R.id.sunday_textview:
                highlightClickedDay(SUNDAY);
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setColorAttr();
        initView();

        if (!isUserSignedIn()) changeToSigninScreen();
        setProjectList();

        if (taskIsRunning()) runTracktimeBar();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_timer:
                drawer.closeDrawer(Gravity.END);
                break;
            case R.id.menu_logout:
                signoutAction();
                break;
            case R.id.menu_team:
                startActivity(new Intent(this, TeamActivity.class));
                drawer.closeDrawer(Gravity.END);
                break;
            case R.id.menu_time_off:
                startActivity(new Intent(this, TimeoffActivity.class));
                drawer.closeDrawer(Gravity.END);
                break;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        timeHandler.removeCallbacks(updateTimerThread);
        hidePDialog();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        if (taskIsRunning()) runTracktimeBar();
        setUpdateRecordList();
        super.onResume();
    }

    //Add play and delete buttons to task list item
    private void addButtonsToTaskList() {
        SwipeMenuCreator creatorLeft = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                openItem.setBackground(new ColorDrawable(getResources().getColor(R.color.colorBlue)));
                openItem.setWidth(pxToDp(90));
                openItem.setIcon(R.drawable.ic_play);
                menu.addMenuItem(openItem);
            }
        };
        SwipeMenuCreator creatorRight = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(getResources().getDrawable(R.drawable.delete_item));
                deleteItem.setWidth(pxToDp(90));
                deleteItem.setIcon(R.drawable.ic_delete);
                menu.addMenuItem(deleteItem);
            }
        };
        recordList.setLeftMenuCreator(creatorLeft);
        recordList.setRightMenuCreator(creatorRight);
    }

    //Create and Add category list view to each project
    @SuppressLint("ClickableViewAccessibility")
    private ListView addCategoryList(final ArrayList<CategoryModel> categories) {
        ListView categoryList = new ListView(this);
        ListView.LayoutParams params = new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                .LayoutParams.WRAP_CONTENT);
        categoryList.setLayoutParams(params);
        CategoryListViewAdapter categoryListViewAdapter = new CategoryListViewAdapter(this, categories);
        categoryList.setAdapter(categoryListViewAdapter);
        categoryList.setDivider(new ColorDrawable(getResources().getColor(R.color.colorGrayLineTransparent)));
        categoryList.setDividerHeight(1);

        setOnTouchListener(categoryList);
        return categoryList;
    }

    //Change to sigin screen
    private void changeToSigninScreen() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        (MainActivity.this).finish();
    }

    //Create new record via API without taskname or category id
    private void createNewRecordWithoutTask() {
        JSONObject jsRecord = createRecordJsonWithoutTask();

        final String createRecordUrl = Consts.URL + "record";
        showPDialog();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, createRecordUrl, jsRecord,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        setRunningRecordInfo(response);
                        welcomeTextIsShown = false;
                        toggleWelcomeText();
                        setUpdateRecordList();
                        hidePDialog();
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

    // Create new task via API with taskname or category id
    private void createNewTask() {
        JSONObject jsTask = createTaskJson();
        Log.e("task", jsTask.toString());
        final String createTaskUrl = Consts.URL + "task";
        showPDialog();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, createTaskUrl, jsTask,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        try {
                            runningRecord.getTask().setId(response.getInt("id"));
                            runningRecord.getTask().setTime(new TimeModel(response.getInt("timeofwork")));
                            createNewRecordWithTask();
                        } catch (JSONException e) {
                            e.printStackTrace();
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

    //Create new record via API after create task
    private void createNewRecordWithTask() {
        JSONObject jsRecord = createRecordJsonWithTask();

        final String createRecordUrl = Consts.URL + "record";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                createRecordUrl, jsRecord,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        try {
                            runningRecord.setId(response.getInt("id"));
                            runningRecord.setTime(new TimeModel(response.getInt("timeofwork")));
                            runningRecord.setStartTime(response.getLong("starttime"));
                            runningRecord.setEndTime(response.getLong("endtime"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        welcomeTextIsShown = false;
                        toggleWelcomeText();
                        setUpdateRecordList();
                        hidePDialog();
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

    //Create Project Background
    private CardView createProjectBackground(int bgColor) {
        CardView taskListBackground = new CardView(this);
        CardView.LayoutParams params = new CardView.LayoutParams(pxToDp(projectBackgroundSize - 15), pxToDp
                (projectBackgroundSize));
        taskListBackground.setClipToPadding(false);
        taskListBackground.setUseCompatPadding(true);
        taskListBackground.setRadius(pxToDp(10));
        taskListBackground.setCardElevation(pxToDp(8));
        taskListBackground.setMaxCardElevation(pxToDp(8));
        taskListBackground.setLayoutParams(params);
        taskListBackground.setCardBackgroundColor(bgColor);
        return taskListBackground;
    }

    //Create Project client name textview
    private TextView createProjectClientTextView() {
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView projectClient = new TextView(this);
        param.setMargins(pxToDp(10), 0, 0, 0);
        projectClient.setText("Client");
        projectClient.setEllipsize(TextUtils.TruncateAt.END);
        projectClient.setMaxEms(23);
        projectClient.setMaxLines(1);
        projectClient.setTextColor(getResources().getColor(R.color.colorGoogle));
        projectClient.setLayoutParams(param);
        return projectClient;
    }

    //Create Project name textview
    private TextView createProjectNameTextView(String name) {
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView projectName = new TextView(this);
        param.setMargins(pxToDp(10), 0, 0, 0);
        projectName.setLayoutParams(param);
        projectName.setText(name);
        projectName.setEllipsize(TextUtils.TruncateAt.END);
        projectName.setMaxEms(5);
        projectName.setSingleLine(true);
        projectName.setMaxLines(1);

        projectName.setTextColor(getResources().getColor(R.color.colorBlackText));
        projectName.setTypeface(Typeface.DEFAULT_BOLD);
        projectName.setTextSize(22);
        return projectName;
    }

    //Create Project Layout for each project model
    private LinearLayout createProjectView(ProjectModel project) {
        LinearLayout projectLayout = new LinearLayout(this);
        projectLayout.setClipToPadding(false);
        projectLayout.setOrientation(LinearLayout.VERTICAL);

        TextView projectClient = createProjectClientTextView();
        TextView projectName = createProjectNameTextView(project.getName());
        CardView taskListBackground = createProjectBackground(parseInt(project.getColor()));

        LinearLayout paddingList = new LinearLayout(this);
        paddingList.setPadding(pxToDp(5), pxToDp(5), pxToDp(5), pxToDp(5));
        paddingList.addView(addCategoryList(project.getCategories()));

        taskListBackground.addView(paddingList);
        projectLayout.addView(projectClient);
        projectLayout.addView(projectName);
        projectLayout.addView(taskListBackground);
        return projectLayout;
    }

    //Create Record via API with or without task
    private void createRecord() {
        if (!startNewTask) createNewRecordWithTask();
        else {
            if (runningRecord.getTask().getName() != "No name" || runningRecord.getTask().getCategory() != null) {
                createNewTask();
            } else {
                createNewRecordWithoutTask();
            }
        }
    }

    private JSONObject createTaskJson() {
        JSONObject jsTask = new JSONObject();
        JSONObject jsCategory = new JSONObject();
        try {
            jsTask.put("name", runningRecord.getTask().getName());
            if (runningRecord.getTask().getCategory() != null) {
                jsCategory.put("id", runningRecord.getTask().getCategory().getId());
                jsTask.put("category", jsCategory);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsTask;
    }

    private JSONObject createRecordJsonWithTask() {
        JSONObject jsRecord = new JSONObject();
        JSONObject jsTask = new JSONObject();
        try {
            jsTask.put("id", runningRecord.getTask().getId());
            jsRecord.put("starttime", startTime);
            jsRecord.put("endtime", System.currentTimeMillis());
            jsRecord.put("task", jsTask);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsRecord;
    }

    private JSONObject createRecordJsonWithoutTask() {
        JSONObject jsRecord = new JSONObject();
        try {
            jsRecord.put("starttime", startTime);
            jsRecord.put("endtime", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsRecord;
    }

    //Delete Task via API
    private void deleteRecord(RecordModel tmpRecord, final int position) {
        String deleteRecordURL = Consts.URL + "record/" + tmpRecord.getId();
        showPDialog();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, deleteRecordURL, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        records.remove(position);
                        recordListViewAdapter = new TaskListViewAdapter(MainActivity.this, records);
                        recordList.setAdapter(recordListViewAdapter);
                        recordList.invalidateViews();
                        hidePDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePDialog();
                Consts.displayMessage(MainActivity.this, "Error");
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

    //Get and return project model from a project json object
    private ProjectModel getProjectFromJsonObject(JSONObject projectObject) throws JSONException {
        int projectId = projectObject.getInt("id");
        String projectName = projectObject.getString("name");
        String projectColor = projectObject.getString("backgroundcolor");
        JSONArray categoryArray = projectObject.getJSONArray("categories");
        ArrayList<CategoryModel> categories = new ArrayList<>();
        ProjectModel tmpProject = new ProjectModel(projectId, projectName, projectColor, categories);
        for (int i = 0; i < categoryArray.length(); i++) {
            JSONObject categoryObject = categoryArray.getJSONObject(i);
            CategoryModel tmpCategory = new CategoryModel(categoryObject.getInt("id"), categoryObject.getString
                    ("name"), tmpProject);
            tmpProject.getCategories().add(tmpCategory);
        }
        return tmpProject;
    }

    //Get projects from response json
    private void getProjectListFromResponse(JSONArray response, LinearLayout projectListLayout) throws JSONException {
        int length = response.length();
        if (length > 0) {
            ProjectModel tmpProject;
            for (int i = 0; i < length; i++) {
                JSONObject projectObject = response.getJSONObject(i);
                tmpProject = getProjectFromJsonObject(projectObject);
                projectListLayout.addView(createProjectView(tmpProject));
                projects.add(tmpProject);
            }
            welcomeTextIsShown = false;
        }
    }

    //get record info with given id via API
    private void getRecordInfo(int recordId) {
        final String getRecordByIdUrl = Consts.URL + "record/" + recordId;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getRecordByIdUrl, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        try {
                            getRunningRecordFromResponse(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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

    //Get running record from response json
    private void getRunningRecordFromResponse(JSONObject response) throws JSONException {
        int recordId = response.getInt("id");
        TimeModel recordTime = new TimeModel(response.getInt("timeofwork"));
        long recordStartTime = response.getLong("starttime");
        long recordEndTime = response.getLong("endtime");

        JSONObject taskObject = response.getJSONObject("task");
        int taskId = taskObject.getInt("id");
        String taskName = taskObject.getString("name");
        TimeModel taskTime = new TimeModel(taskObject.getInt("timeofwork"));

        JSONObject cateObject = taskObject.getJSONObject("category");
        int categoryId = cateObject.getInt("id");
        String categoryName = cateObject.getString("name");

        JSONObject projectObject = cateObject.getJSONObject("project");
        int projectId = projectObject.getInt("id");
        String projectName = projectObject.getString("name");
        String projectColor = projectObject.getString("backgroundcolor");

        ProjectModel projectModel = new ProjectModel(projectId, projectName, projectColor, null);
        CategoryModel categoryModel = new CategoryModel(categoryId, categoryName, projectModel);
        TaskModel taskModel = new TaskModel(taskId, taskName, taskTime, categoryModel);
        runningRecord = new RecordModel(recordId, recordTime, recordStartTime, recordEndTime, taskModel);
    }

    //Create a task list got from response
    private ArrayList<RecordModel> getTaskListfromResponse(JSONArray response) throws JSONException {
        ArrayList<RecordModel> recordList = new ArrayList<>();
        int length = response.length();
        if (length > 0) {
            RecordModel record;
            TaskModel task;
            TimeModel taskTime, recordTime;
            CategoryModel category;
            ProjectModel project;
            JSONObject categoryObject, projectObject;
            for (int i = 0; i < length; i++) {
                JSONObject recordObject = response.getJSONObject(i);
                recordTime = new TimeModel(recordObject.getInt("timeofwork"));
                JSONObject taskObject = recordObject.getJSONObject("task");
                taskTime = new TimeModel(taskObject.getInt("timeofwork"));
                if (taskObject.has("category")) {
                    categoryObject = taskObject.getJSONObject("category");
                    projectObject = categoryObject.getJSONObject("project");
                    project = new ProjectModel(projectObject.getInt("id"), projectObject.getString("name"),
                            projectObject.getString("backgroundcolor"), null);
                    category = new CategoryModel(categoryObject.getInt("id"), categoryObject.getString("name"),
                            project);
                } else {
                    category = null;
                }
                task = new TaskModel(taskObject.getInt("id"), taskObject.has("name") ? taskObject.getString("name") :
                        "No name", taskTime, category);
                record = new RecordModel(recordObject.getInt("id"), recordTime, recordObject.getLong("starttime"),
                        recordObject.getLong("endtime"), task);
                recordList.add(record);
            }
        }
        return recordList;
    }

    //Get token and user id from local
    public void getTokenUserId() {
        userId = prefs.getString("userId", Consts.DEFAULT_KEY);
        token = prefs.getString("token", Consts.DEFAULT_KEY);
        loginType = prefs.getString("loginType", Consts.DEFAULT_KEY);
    }

    //Get working time in week from response
    private void getWorkingTimeFromResponse(JSONArray timeOfWorkInWeek) throws JSONException, ParseException {
        long totalWeekTime = 0;
        for (int i = MONDAY; i <= SUNDAY; i++) {
            JSONObject day = timeOfWorkInWeek.getJSONObject(i - 1);
            totalWeekTime += day.getInt("timeofwork");
            workingTimeInWeek.put(i, new TimeModel(day.getInt("timeofwork")));
            timeTotal = new TimeModel(totalWeekTime);
            setTotalTimeTextView();
        }
    }

    //Get working time in a week via API
    private void getWorkingTimeInWeek() {
        Calendar mondayOfWeek = Calendar.getInstance();
        mondayOfWeek.setTimeInMillis(pickedDay.getTimeInMillis());
        mondayOfWeek.add(Calendar.DATE, 1 - dateNumberInWeek);
        String getMondayOfWeek = new SimpleDateFormat("yyyy-MM-dd").format(mondayOfWeek.getTime());
        Log.e("monday", getMondayOfWeek);
        String getWorkTimeByWeek = Consts.URL + "report/week?date=" + getMondayOfWeek;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                getWorkTimeByWeek, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        try {
                            getWorkingTimeFromResponse(response.getJSONArray("timeofworkinaweek"));
                            setWorkingTimeOfDay();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener()

        {
            @Override
            public void onErrorResponse(VolleyError error) {
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

    private void hidePDialog() {
        if (nDialog != null) {
            nDialog.dismiss();
            nDialog = null;
        }
    }

    //Change color of clicked date button
    private void highlightClickedDay(int day) {
        if (dateNumberInWeek != day) {
            pickedDay.add(Calendar.DAY_OF_WEEK, day - dateNumberInWeek);

            datePickerDialog = new DatePickerDialog(MainActivity.this, callback, pickedDay.get(Calendar.YEAR),
                    pickedDay.get(Calendar.MONTH), pickedDay.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMaxDate(currentDay.getTimeInMillis());

            pickedDayText = new SimpleDateFormat("EEE, dd MMM").format(pickedDay.getTime());
            if (pickedDayText.equals(currentDayText)) {
                dateView.setText(currentDayText + " - Today");
            } else dateView.setText(pickedDayText);

            removeDateHighLight();
            dateNumberInWeek = (pickedDay.get(Calendar.DAY_OF_WEEK) != 1) ? pickedDay.get(Calendar.DAY_OF_WEEK) - 1 : 7;
            setDateHighlight();

            setUpdateRecordList();
        }
    }

    //Init Date picker dialog's listener
    private void initDatePickerDialogListener() {
        callback = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                pickedDay.set(year, month, day);
                pickedDayText = new SimpleDateFormat("EEE, dd MMM").format(pickedDay.getTime());
                if (pickedDayText.equals(currentDayText)) {
                    dateView.setText(currentDayText + " - Today");
                } else dateView.setText(pickedDayText);

                removeDateHighLight();
                dateNumberInWeek = (pickedDay.get(Calendar.DAY_OF_WEEK) != 1) ? pickedDay.get(Calendar.DAY_OF_WEEK) -
                        1 : 7;
                setDateHighlight();
                getWorkingTimeInWeek();
                setUpdateRecordList();
            }
        };
    }

    //Init Google Signin
    private void initGoogleSignin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    //Init Toolbar and Navigation
    private void initToolbarAndNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        drawer = findViewById(R.id.drawer_layout);
        drawerToggle = new EndDrawerToggle(this, drawer, toolbar, R.string
                .navigation_drawer_open, R.string
                .navigation_drawer_close);
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(Gravity.END)) {
                    drawer.closeDrawer(Gravity.END);
                } else {
                    drawer.openDrawer(Gravity.END);
                }
            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    //init Tracktime Bar
    private void initTracktimeBar() {
        tracktimeBar = findViewById(R.id.tracktimebar);
        tracktimeBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewTaskActivity.class);
                if (taskIsRunning) {
                    intent.putExtra("runningRecord", runningRecord);
                    intent.putExtra("startTime", startTime);
                }
                startActivity(intent);
            }
        });
        stateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (taskIsRunning) {
                    stopTrackTimeBar();
                } else {
                    Intent intent = new Intent(MainActivity.this, NewTaskActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    //Init weekday's textview on WorkingTimeBar
    private void initWeekDayViews() {
        monView = findViewById(R.id.monday_textview);
        monWorkingTime = findViewById(R.id.monday_working_time);
        monView.setOnClickListener(this);
        tueView = findViewById(R.id.tuesday_textview);
        tueWorkingTime = findViewById(R.id.tuesday_working_time);
        tueView.setOnClickListener(this);
        wedView = findViewById(R.id.wednesday_textview);
        wedWorkingTime = findViewById(R.id.wednesday_working_time);
        wedView.setOnClickListener(this);
        thuView = findViewById(R.id.thursday_textview);
        thuWorkingTime = findViewById(R.id.thursday_working_time);
        thuView.setOnClickListener(this);
        friView = findViewById(R.id.friday_textview);
        friWorkingTime = findViewById(R.id.friday_working_time);
        friView.setOnClickListener(this);
        satView = findViewById(R.id.saturday_textview);
        satWorkingTime = findViewById(R.id.saturday_working_time);
        satView.setOnClickListener(this);
        sunView = findViewById(R.id.sunday_textview);
        sunWorkingTime = findViewById(R.id.sunday_working_time);
        sunView.setOnClickListener(this);
    }

    //Init TextViews, Buttons and Layouts
    private void initView() {
        taskName = findViewById(R.id.task_name);
        projectCategory = findViewById(R.id.project_category_name);
        projectRunningTaskIcon = findViewById(R.id.project_icon);

        todayView = findViewById(R.id.today_view);
        recordList = new TaskListView(this);
        recordList.setPadding(0, 0, 0, pxToDp(80));

        secView = findViewById(R.id.sec_textview);
        hourMinView = findViewById(R.id.hour_min_textview);
        stateBtn = findViewById(R.id.state_btn);
        mainContent = findViewById(R.id.main_content);
        horizontalScrollView = findViewById(R.id.horizontal_scrollview);

        welcomeTitle = findViewById(R.id.welcome_title);
        welcomeContent = findViewById(R.id.welcome_content);

        currentDay = Calendar.getInstance();
        pickedDay = Calendar.getInstance();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ed = prefs.edit();

        queue = Volley.newRequestQueue(this);

        initWeekDayViews();
        initDatePickerDialogListener();
        initToolbarAndNavigation();
        initTracktimeBar();
        initGoogleSignin();
    }

    //Check if background service is running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //Check if User has signed in alr
    private Boolean isUserSignedIn() {
        getTokenUserId();
        return !token.equals("Default");
    }

    //Scale px to dp
    private int pxToDp(int pixel) {
        final float scale = this.getResources().getDisplayMetrics().density;
        return (int) (pixel * scale + 0.5f);
    }

    //Set colors
    private void setColorAttr() {
        color = new HashMap<>();
        color.put("red", getResources().getColor(R.color.colorRedIcon));
        color.put("blue", getResources().getColor(R.color.colorBlueIcon));
        color.put("lime", getResources().getColor(R.color.colorLimeIcon));
        color.put("aqua", getResources().getColor(R.color.colorAquaIcon));
        color.put("yellow", getResources().getColor(R.color.colorYellowIcon));
        color.put("brown", getResources().getColor(R.color.colorBrownIcon));
        color.put("purple", getResources().getColor(R.color.colorPurpleIcon));
        color.put("pink", getResources().getColor(R.color.colorPinkIcon));
        color.put("orange", getResources().getColor(R.color.colorOrangeIcon));
        color.put("seafoam", getResources().getColor(R.color.colorSeafoamIcon));
    }

    //Highlight chosen day in week
    private void setDateHighlight() {
        switch (dateNumberInWeek) {
            case MONDAY:
                setDayBackground(monView, monWorkingTime);
                break;
            case TUESDAY:
                setDayBackground(tueView, tueWorkingTime);
                break;
            case WEDNESDAY:
                setDayBackground(wedView, wedWorkingTime);
                break;
            case THURSDAY:
                setDayBackground(thuView, thuWorkingTime);
                break;
            case FRIDAY:
                setDayBackground(friView, friWorkingTime);
                break;
            case SATURDAY:
                setDayBackground(satView, satWorkingTime);
                break;
            case SUNDAY:
                setDayBackground(sunView, sunWorkingTime);
                break;

        }
    }

    //Change background color of chosen day
    private void setDayBackground(TextView dayTextView, TextView dayWorkingTimeTextView) {
        dayTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        dayTextView.setBackground(getResources().getDrawable(R.drawable.background_date_blue));
        dayWorkingTimeTextView.setTextColor(getResources().getColor(R.color.colorBlue));
    }

    //Set onTouchListener for Category list inside project (Swipe up down left right)
    private void setOnTouchListener(ListView categoryList) {
        categoryList.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                touchActionMoveStatus = false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchActionDownX = (int) event.getX();
                        touchActionDownY = (int) event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        touchActionMoveStatus = true;
                        if (touchActionMoveStatus) {
                            touchActionMoveX = (int) event.getX();
                            touchActionMoveY = (int) event.getY();

                            if (touchActionMoveX < (touchActionDownX - threshold) &&
                                    (touchActionMoveY >
                                            (touchActionDownY - threshold)) && (touchActionMoveY <
                                    (touchActionDownY +
                                            threshold))) {
                                //Move left: If the move left was greater than the threshold and
                                // not greater than the
                                // threshold up or down
                                touchActionMoveStatus = false;
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                            } else if (touchActionMoveX > (touchActionDownX + threshold) &&
                                    (touchActionMoveY >
                                            (touchActionDownY - threshold)) && (touchActionMoveY <
                                    (touchActionDownY +
                                            threshold))) {
                                //Move right: If the move right was greater than the threshold
                                // and not greater than
                                // the threshold up or down
                                touchActionMoveStatus = false;
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                            } else if (touchActionMoveY < (touchActionDownY - threshold) &&
                                    (touchActionMoveX >
                                            (touchActionDownX - threshold)) && (touchActionMoveX <
                                    (touchActionDownX +
                                            threshold))) {
                                //Move up: If the move up was greater than the threshold and not
                                // greater than the
                                // threshold left or right
                                touchActionMoveStatus = false;
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                            } else if (touchActionMoveY > (touchActionDownY + threshold) &&
                                    (touchActionMoveX >
                                            (touchActionDownX - threshold)) && (touchActionMoveX <
                                    (touchActionDownX +
                                            threshold))) {
                                //Move down: If the move down was greater than the threshold and
                                // not greater than the
                                // threshold left or right
                                touchActionMoveStatus = false;
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                            }
                        }
                        break;
                }
                return false;
            }
        });
    }

    public void setProjectList() {

        projects = new ArrayList<>();
        final LinearLayout projectListLayout = new LinearLayout(this);
        projectListLayout.setOrientation(LinearLayout.HORIZONTAL);
        projectListLayout.setClipToPadding(false);

        showPDialog();
        final String getProjectListUrl = Consts.URL + "pj/listall";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getProjectListUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hidePDialog();
                        try {
                            getProjectListFromResponse(response.getJSONArray("projects"), projectListLayout);
                            setUpdateRecordList();
                        } catch (JSONException e) {
                            e.printStackTrace();
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
        horizontalScrollView.addView(projectListLayout);

    }

    //Get and Set running record attributes from response
    private void setRunningRecordInfo(JSONObject response) {
        try {
            JSONObject taskObject = response.getJSONObject("task");
            runningRecord.getTask().setId(taskObject.getInt("id"));
            runningRecord.getTask().setTime(new TimeModel(taskObject.getInt("timeofwork")));
            runningRecord.setId(response.getInt("id"));
            runningRecord.setTime(new TimeModel(response.getInt("timeofwork")));
            runningRecord.setStartTime(response.getLong("starttime"));
            runningRecord.setEndTime(response.getLong("endtime"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setRecordList() {
        records = new ArrayList<>();
        String getPickedDay = new SimpleDateFormat("yyyy-MM-dd").format(pickedDay.getTime());
        final String getTaskByDayUrl = Consts.URL + "record/date?date=" + getPickedDay;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getTaskByDayUrl, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getJSONArray("records").length() != 0) {
                                mainContent.removeView(recordList);
                                records = getTaskListfromResponse(response.getJSONArray("records"));
                                recordListViewAdapter = new TaskListViewAdapter(MainActivity.this, records);
                                recordList.setAdapter(recordListViewAdapter);
                                mainContent.addView(recordList);
                                setWorkingTimeBar();
                                welcomeTextIsShown = false;
                                toggleWelcomeText();
                            }
                            hidePDialog();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
        addButtonsToTaskList();
        setTaskMenuListener();
    }

    //Add OnClickListener for buttons on task list item
    private void setTaskMenuListener() {
        recordList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (menu.getViewType()) {
                    case 0:
                        switch (index) {
                            case 0:
                                if (runningRecord != null) {
                                    RecordModel chosenRecord = (RecordModel) recordListViewAdapter.getItem(position);
                                    if (!(runningRecord == chosenRecord)) {
                                        stopTrackTimeBar();
                                        runningRecord = (RecordModel) recordListViewAdapter.getItem(position);
                                        startTime = System.currentTimeMillis();
                                        taskIsRunning = true;
                                        runTracktimeBar();
                                    }
                                } else {
                                    runningRecord = (RecordModel) recordListViewAdapter.getItem(position);
                                    startTime = System.currentTimeMillis();
                                    taskIsRunning = true;
                                    runTracktimeBar();
                                }
                                break;
                        }
                        break;
                    case 1:
                        switch (index) {
                            case 0:
                                RecordModel tmpRecord = (RecordModel) recordListViewAdapter.getItem
                                        (position);
                                deleteRecord(tmpRecord, position);
                                break;
                        }
                        break;
                }
            }
        });
    }

    //Set Total time textview
    private void setTotalTimeTextView() {
        totalTimeView = findViewById(R.id.total_time_textview);
        String totalTime = timeTotal.hourToText() + ":" + timeTotal.minToText() + "h";
        totalTimeView.setText(totalTime);
    }

    //Check if recordList is available and set or update it
    private void setUpdateRecordList() {
        if (recordList.getParent() != null) {
            updateRecordList();
        } else {
            setRecordList();
        }
    }

    //Set Working time Bar
    private void setWorkingTimeBar() {
        if (todayView.getVisibility() == View.GONE) {
            currentDayText = new SimpleDateFormat("EEE, dd MMM").format(currentDay.getTime());

            dateLayout = findViewById(R.id.date_layout);

            dateView = findViewById(R.id.date_textview);
            dateView.setText(currentDayText + " - Today");

            dateLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datePickerDialog = new DatePickerDialog(MainActivity.this, callback,
                            pickedDay.get(Calendar.YEAR), pickedDay.get(Calendar.MONTH),
                            pickedDay.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.getDatePicker().setMaxDate(currentDay.getTimeInMillis());
                    datePickerDialog.show();
                }
            });

            currentDateText = new SimpleDateFormat("yyyy-MM-dd").format(currentDay.getTime());
            int dayInWeek = currentDay.get(Calendar.DAY_OF_WEEK);
            dateNumberInWeek = (dayInWeek != 1) ? dayInWeek - 1 : 7;

            setDateHighlight();
            getWorkingTimeInWeek();
            todayView.setVisibility(View.VISIBLE);
        }
    }

    //Set Working time Textview for each day in week
    private void setWorkingTimeOfDay() {
        TimeModel time;
        for (int i = MONDAY; i <= SUNDAY; i++) {
            time = workingTimeInWeek.get(i);
            switch (i) {
                case MONDAY:
                    monWorkingTime.setText(time.hourToText() + ":" + time.minToText());
                    break;
                case TUESDAY:
                    tueWorkingTime.setText(time.hourToText() + ":" + time.minToText());
                    break;
                case WEDNESDAY:
                    wedWorkingTime.setText(time.hourToText() + ":" + time.minToText());
                    break;
                case THURSDAY:
                    thuWorkingTime.setText(time.hourToText() + ":" + time.minToText());
                    break;
                case FRIDAY:
                    friWorkingTime.setText(time.hourToText() + ":" + time.minToText());
                    break;
                case SATURDAY:
                    satWorkingTime.setText(time.hourToText() + ":" + time.minToText());
                    break;
                case SUNDAY:
                    sunWorkingTime.setText(time.hourToText() + ":" + time.minToText());
                    break;
            }
        }
    }

    private void showPDialog() {
        nDialog = new ProgressDialog(MainActivity.this, R.style.MyDialogTheme);
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
        nDialog.show();
    }

    //Signout via API
    private void signoutAction() {
        showPDialog();
        String signoutURL = Consts.URL + "/signout";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, signoutURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hidePDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePDialog();
                Consts.displayMessage(MainActivity.this, "error");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", token);
                return params;
            }
        };
        signoutSuccess();
        queue.add(stringRequest);
    }

    //Remove user on local and change to signin screen after signout sucessfully
    public void signoutSuccess() {
        switch (loginType) {
            case "Google":
                mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        removeTokenUsername();
                        changeToSigninScreen();
                    }
                });
                break;
            case "Facebook":
                //sign out facebook
                break;
            default:
                removeTokenUsername();
                changeToSigninScreen();
                break;
        }
    }

    //Update tracktime bar after pressing stop button
    private void stopTrackTimeBar() {
        taskName.setText("des. . .");
        projectCategory.setText("Project . Category");
        projectRunningTaskIcon.setVisibility(View.GONE);

        secView.setText("");
        hourMinView.setText("");
        stateBtn.setBackground(getResources().getDrawable(R.mipmap.playbtn));

        timeHandler.removeCallbacks(updateTimerThread);

        createRecord();
        setUpdateRecordList();

        taskIsRunning = false;
        startNewTask = true;
    }

    //Remove background for highlighted day after choosing another day
    private void removeDateHighLight() {
        switch (dateNumberInWeek) {
            case MONDAY:
                removeDayBackground(monView, monWorkingTime);
                break;
            case TUESDAY:
                removeDayBackground(tueView, tueWorkingTime);
                break;
            case WEDNESDAY:
                removeDayBackground(wedView, wedWorkingTime);
                break;
            case THURSDAY:
                removeDayBackground(thuView, thuWorkingTime);
                break;
            case FRIDAY:
                removeDayBackground(friView, friWorkingTime);
                break;
            case SATURDAY:
                removeDayBackground(satView, satWorkingTime);
                break;
            case SUNDAY:
                removeDayBackground(sunView, sunWorkingTime);
                break;
        }
    }

    //Remove day background
    private void removeDayBackground(TextView dayTextView, TextView dayWorkingTimeTextView) {
        dayTextView.setTextColor(getResources().getColor(R.color.colorBlackText));
        dayTextView.setBackground(null);
        dayWorkingTimeTextView.setTextColor(getResources().getColor(R.color.colorBlackText));
    }

    //Remove user on local
    private void removeTokenUsername() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor ed = prefs.edit();
        ed.clear();
        ed.apply();
    }

    //Update tracktime bar when play button is pressed
    private void runTracktimeBar() {
        taskName.setText(runningRecord.getTask().getName() == null ? "No name" : runningRecord.getTask().getName());
        projectCategory.setText(runningRecord.getTask().getProjectCategory());
        if (runningRecord.getTask().getCategory() != null)
            projectRunningTaskIcon.setBackgroundColor(Integer.parseInt(runningRecord.getTask().getCategory()
                    .getProject().getColor()));
        else {
            projectRunningTaskIcon.setBackgroundColor(getResources().getColor(R.color.colorGrayText));
        }
        projectRunningTaskIcon.setVisibility(View.VISIBLE);
        stateBtn.setBackground(getResources().getDrawable(R.mipmap.stopbtn));
        updateTimerThread = new Runnable() {
            @Override
            public void run() {
                timeInMillisecond = System.currentTimeMillis() - startTime;
                time = new TimeModel((int) (timeInMillisecond / 1000));
                secView.setText(time.secToText() + "s");
                hourMinView.setText(time.hourToText() + ":" + time.minToText());
                timeHandler.postDelayed(updateTimerThread, 1000);
            }
        };
        timeHandler.postDelayed(updateTimerThread, 1000);
    }

    //Check if task is running base on intent extra
    private Boolean taskIsRunning() {
        //Get intent from newtask screen
        if (getIntent().hasExtra("startTime")) {
            String runningRecordName = null;
            if (getIntent().hasExtra("category")) {
                runningRecordCategory = (CategoryModel) getIntent().getSerializableExtra("category");
            }
            if (getIntent().hasExtra("name")) {
                runningRecordName = getIntent().getStringExtra("name");
            } else runningRecordName = "No name";
            startTime = getIntent().getLongExtra("startTime", 0L);
            runningRecord = new RecordModel(0, null, 0, 0, new TaskModel(0, runningRecordName, null,
                    runningRecordCategory));
            taskIsRunning = true;
            getIntent().removeExtra("startTime");
            runTracktimeBar();
//            startBackgroundServiceTimer(runningRecordCategory);

            //Get intent from tasklist screen
        } else if (getIntent().hasExtra("task")) {
            TaskModel task = (TaskModel) getIntent().getSerializableExtra("task");
            startTime = System.currentTimeMillis();
            runningRecord = new RecordModel(0, null, 0, 0, task);
            taskIsRunning = true;
            startNewTask = false;
            getIntent().removeExtra("task");
            runTracktimeBar();
//            startBackgroundServiceTimer(task.getCategory());

            //Get intent from play button in category list and last task is available
        } else if (getIntent().hasExtra("lastTaskInCategory")) {
            TaskModel task = (TaskModel) getIntent().getSerializableExtra("lastTaskInCategory");
            startTime = System.currentTimeMillis();
            runningRecord = new RecordModel(0, null, 0, 0, task);
            taskIsRunning = true;
            startNewTask = false;
            getIntent().removeExtra("lastTaskInCategory");
            runTracktimeBar();
//            startBackgroundServiceTimer(task.getCategory());

            //Get intent from play button in category list and no task is available
        } else if (getIntent().hasExtra("noTaskInCategory")) {
            runningRecordCategory = (CategoryModel) getIntent().getSerializableExtra("noTaskInCategory");
            startTime = System.currentTimeMillis();
            String runningRecordName = "null";
            runningRecord = new RecordModel(0, null, 0, 0, new TaskModel(0, runningRecordName, null,
                    runningRecordCategory));
            taskIsRunning = true;
            getIntent().removeExtra("noTaskInCategory");
            runTracktimeBar();
//            startBackgroundServiceTimer(runningRecordCategory);
        }
        else if(getIntent().hasExtra("taskListUpdate")){
            setUpdateRecordList();
            setWorkingTimeBar();
        }
        return taskIsRunning;
    }

    //Show welcome Textview if there is no project or record
    private void toggleWelcomeText() {
        if (welcomeTextIsShown) {
            welcomeTitle.setVisibility(View.VISIBLE);
            welcomeContent.setVisibility(View.VISIBLE);
        } else {
            welcomeTitle.setVisibility(View.GONE);
            welcomeContent.setVisibility(View.GONE);
        }
    }

    private void updateRecordList() {
        records = new ArrayList<>();
        String getPickedDay = new SimpleDateFormat("yyyy-MM-dd").format(pickedDay.getTime());
        final String getTaskByDayUrl = Consts.URL + "record/date?date=" + getPickedDay;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getTaskByDayUrl, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getJSONArray("records").length() != 0) {
                                records = getTaskListfromResponse(response.getJSONArray("records"));
                                recordListViewAdapter = new TaskListViewAdapter(MainActivity.this, records);
                                recordList.setAdapter(recordListViewAdapter);
                                recordList.invalidateViews();
                                setWorkingTimeBar();
                                welcomeTextIsShown = false;
                                toggleWelcomeText();
                            } else {
                                recordListViewAdapter = new TaskListViewAdapter(MainActivity.this, records);
                                recordList.setAdapter(recordListViewAdapter);
                                recordList.invalidateViews();
                            }
                            hidePDialog();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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


    private TextView monView, tueView, thuView, wedView, friView, satView, sunView,
            monWorkingTime, tueWorkingTime,
            wedWorkingTime, thuWorkingTime, friWorkingTime, satWorkingTime, sunWorkingTime;
    private TextView secView, hourMinView, dateView, totalTimeView;
    private TextView welcomeTitle, welcomeContent;
    private TextView taskName, projectCategory;
    private String token, userId, loginType;
    private String currentDateText, currentDayText, pickedDayText;
    private Boolean touchActionMoveStatus;
    private Boolean taskIsRunning = false;
    private Boolean startNewTask = true;
    private Boolean welcomeTextIsShown = true;
    private TimeModel time;
    private int touchActionDownX = 0, touchActionDownY = 0, touchActionMoveX, touchActionMoveY;
    private int dateNumberInWeek;
    private ArrayList<ProjectModel> projects;
    private ArrayList<RecordModel> records;
    private ProgressDialog nDialog;
    private TimeModel timeTotal;
    private RecordModel runningRecord;
    private CategoryModel runningRecordCategory;
    private Handler timeHandler = new Handler();
    private long startTime = 0L, timeInMillisecond = 0L;
    private Runnable updateTimerThread;
    private ImageButton stateBtn;
    private ImageView projectRunningTaskIcon;
    private LinearLayout mainContent, dateLayout;
    private RelativeLayout tracktimeBar, todayView;
    private HashMap<Integer, TimeModel> workingTimeInWeek = new HashMap<>();
    private HashMap<String, Integer> color;
    private TaskListView recordList;
    private TaskListViewAdapter recordListViewAdapter;
    private Calendar currentDay, pickedDay;
    private DrawerLayout drawer;
    private EndDrawerToggle drawerToggle;
    private HorizontalScrollView horizontalScrollView;
    private GoogleSignInClient mGoogleSignInClient;
    private DatePickerDialog datePickerDialog;
    private DatePickerDialog.OnDateSetListener callback;
    private SharedPreferences prefs;
    private SharedPreferences.Editor ed;
    private RequestQueue queue;
    static final int MONDAY = 1;
    static final int TUESDAY = 2;
    static final int WEDNESDAY = 3;
    static final int THURSDAY = 4;
    static final int FRIDAY = 5;
    static final int SATURDAY = 6;
    static final int SUNDAY = 7;
    static final int projectBackgroundSize = 215;
    static final int threshold = 5;
}
