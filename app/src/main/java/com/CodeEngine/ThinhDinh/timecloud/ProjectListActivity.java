package com.CodeEngine.ThinhDinh.timecloud;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.CodeEngine.ThinhDinh.timecloud.Utils.Consts;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.CodeEngine.ThinhDinh.timecloud.Adapter.ProjectListViewAdapter;
import com.CodeEngine.ThinhDinh.timecloud.Model.CategoryModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.ProjectModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Administrator on 2/8/2018.
 */

public class ProjectListActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_project_button:
                startActivity(new Intent(ProjectListActivity.this, NewProjectActivity.class));
                break;
            case R.id.close_button:
                Intent intent;
                if (gotIntentFromTaskInfo) intent = new Intent(this, TaskInfoActivity.class);
                else
                    intent = new Intent(this, NewTaskActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        closeBtn = findViewById(R.id.close_button);
        addBtn = findViewById(R.id.add_project_button);
        closeBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);

        getIntentExtras();
        getTokenUserId();
        setProjectList();

        projectSearch = findViewById(R.id.project_search);
        projectSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String search = projectSearch.getText().toString()
                        .toLowerCase();
                chooseProjectlistAdapter.getFilter().filter(search);
            }
        });
    }

    @Override
    protected void onResume() {
        getProjectList();
        super.onResume();
    }

    private void getIntentExtras() {
        if (getIntent().hasExtra("update"))
            gotIntentFromTaskInfo = true;
    }

    private void getProjectList() {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String getProjectListUrl = Consts.URL + "pj/listall";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getProjectListUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            projectList = getProjectListFromResponse(response.getJSONArray("projects"));
                            chooseProjectlistAdapter = new ProjectListViewAdapter(ProjectListActivity.this, projectList);
                            chooseProjectlist.setAdapter(chooseProjectlistAdapter);
                            chooseProjectlist.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                                @Override
                                public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
                                    CategoryModel category = (CategoryModel) chooseProjectlistAdapter.getChild(groupPosition, childPosition);
                                    Intent intent;
                                    if (gotIntentFromTaskInfo) {
                                        intent = new Intent(ProjectListActivity.this, TaskInfoActivity.class);
                                        intent.putExtra("category", category);
                                        startActivity(intent);
                                    } else {
                                        intent = new Intent(ProjectListActivity.this, NewTaskActivity.class);
                                        intent.putExtra("category", category);
                                        startActivityIfNeeded(intent, 0);
                                    }
                                    ProjectListActivity.this.finish();
                                    return false;
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

    private ArrayList<ProjectModel> getProjectListFromResponse(JSONArray response) throws JSONException {
        ArrayList<ProjectModel> tmpProjectList = new ArrayList<>();
        int length = response.length();
        if (length == 0) {
            setTextIfNoProjectExist();
        } else {
            ProjectModel tmpProject;
            for (int i = 0; i < length; i++) {
                JSONObject tmpObject = response.getJSONObject(i);
                tmpProject = getProjectFromJson(tmpObject);
                tmpProjectList.add(tmpProject);
            }
        }
        return tmpProjectList;
    }

    private ProjectModel getProjectFromJson(JSONObject tmpObject) throws JSONException {
        String projectName = tmpObject.getString("name");
        String projectColor = tmpObject.getString("backgroundcolor");
        int projectId = tmpObject.getInt("id");
        JSONArray categoryArray = tmpObject.getJSONArray("categories");
        ArrayList<CategoryModel> categories = new ArrayList<>();
        ProjectModel tmpProject = new ProjectModel(projectId, projectName, projectColor, categories);
        for (int i = 0; i < categoryArray.length(); i++) {
            JSONObject categoryObject = categoryArray.getJSONObject(i);
            CategoryModel tmpCategory = new CategoryModel(categoryObject.getInt("id"), categoryObject.getString("name"), tmpProject);
            tmpProject.getCategories().add(tmpCategory);
        }
        return tmpProject;
    }

    public void getTokenUserId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        userId = prefs.getString("userId", Consts.DEFAULT_KEY);
        token = prefs.getString("token", Consts.DEFAULT_KEY);
        loginType = prefs.getString("loginType", Consts.DEFAULT_KEY);
    }

    private void setProjectList() {
        chooseProjectlist = findViewById(R.id.choose_projectlist);
        getProjectList();
    }

    private void setTextIfNoProjectExist() {
        TextView noProjectNotificationTitle = findViewById(R.id.no_project_title);
        TextView noProjectNotificationContent = findViewById(R.id.no_project_content);
        noProjectNotificationTitle.setVisibility(View.VISIBLE);
        noProjectNotificationContent.setVisibility(View.VISIBLE);
    }

    private ImageButton closeBtn;
    private FloatingActionButton addBtn;
    private EditText projectSearch;
    private String token, userId, loginType;
    private ExpandableListView chooseProjectlist;
    private ProjectListViewAdapter chooseProjectlistAdapter;
    private ArrayList<ProjectModel> projectList;
    private Boolean gotIntentFromTaskInfo = false;
}
