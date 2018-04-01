package com.CodeEngine.ThinhDinh.timecloud;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.CodeEngine.ThinhDinh.timecloud.Utils.Consts;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.CodeEngine.ThinhDinh.timecloud.Adapter.TaskInCategoryListViewAdapter;
import com.CodeEngine.ThinhDinh.timecloud.Model.CategoryModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.TaskModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.TimeModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 3/9/2018.
 */

public class TaskListActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        projectName = findViewById(R.id.project_name);
        categoryName = findViewById(R.id.category_name);
        taskList = findViewById(R.id.task_list);

        getTokenUserId();
        getIntentExtra();
        setTaskList();
    }

    private void getIntentExtra() {
        category = (CategoryModel) getIntent().getSerializableExtra("category");

        categoryName.setText("Category: " + category.getName());
        categoryName.setBackgroundColor(Integer.parseInt(category.getProject().getColor()));
        projectName.setText(category.getProject().getName());
        projectName.setTextColor(Integer.parseInt(category.getProject().getColor()));
        taskList.setBackgroundColor(Integer.parseInt(category.getProject().getColor()));
    }

    private ArrayList<TaskModel> getTaskListInCategory(JSONArray taskArray) throws JSONException {
        ArrayList<TaskModel> tasks = new ArrayList<>();
        if (taskArray.length() > 0) {
            for (int i = 0; i < taskArray.length(); i++) {
                JSONObject taskObject = taskArray.getJSONObject(i);
                TaskModel task = new TaskModel(taskObject.getInt("id"), taskObject.getString
                        ("name"), new TimeModel(taskObject.getInt("timeofwork")), category);
                tasks.add(task);
            }
        }
        return tasks;
    }

    private void getTokenUserId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        userId = prefs.getString("userId", Consts.DEFAULT_KEY);
        token = prefs.getString("token", Consts.DEFAULT_KEY);
        loginType = prefs.getString("loginType", Consts.DEFAULT_KEY);
    }

    private void setTaskList() {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String getProjectListUrl = Consts.URL + "cate/" + category.getId();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                getProjectListUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            tasks = getTaskListInCategory(response.getJSONArray("tasks"));
                            taskInCategoryListViewAdapter = new TaskInCategoryListViewAdapter
                                    (TaskListActivity.this, category, tasks);
                            taskList.setAdapter(taskInCategoryListViewAdapter);
                            taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view,
                                                        int position, long l) {
                                    TaskModel task = tasks.get(position);
                                }
                            });
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

    private TextView projectName, categoryName;
    private ListView taskList;
    private String token, userId, loginType;
    private CategoryModel category;
    private ArrayList<TaskModel> tasks;
    private TaskInCategoryListViewAdapter taskInCategoryListViewAdapter;
}
