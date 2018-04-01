package com.CodeEngine.ThinhDinh.timecloud.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.CodeEngine.ThinhDinh.timecloud.MainActivity;
import com.CodeEngine.ThinhDinh.timecloud.Model.ProjectModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.TaskModel;
import com.CodeEngine.ThinhDinh.timecloud.Model.TimeModel;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.CodeEngine.ThinhDinh.timecloud.Model.CategoryModel;
import com.CodeEngine.ThinhDinh.timecloud.R;
import com.CodeEngine.ThinhDinh.timecloud.TaskListActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2/22/2018.
 */

public class CategoryListViewAdapter extends BaseAdapter {

    public CategoryListViewAdapter(Context context, ArrayList<CategoryModel> categories) {
        this.context = context;
        this.categories = categories;
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        final CategoryModel category = (CategoryModel) getItem(position);
        if (view == null) {
            LayoutInflater li = (LayoutInflater) this.context.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.categorylist_item, null);
        }
        getTokenUserId();

        TextView categoryTextView = view.findViewById(R.id.category_textview);
        categoryTextView.setText(category.getName());

        categoryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, TaskListActivity.class);
                intent.putExtra("category", category);
                context.startActivity(intent);
            }
        });

        setPlayBtnListener(view, category);
        return view;
    }

    public void getTokenUserId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        userId = prefs.getString("userId", DEFAULT_KEY);
        token = prefs.getString("token", DEFAULT_KEY);
        loginType = prefs.getString("loginType", DEFAULT_KEY);
    }

    private void hidePDialog() {
        if (nDialog != null) {
            nDialog.dismiss();
            nDialog = null;
        }
    }

    private void showPDialog() {
        nDialog = new ProgressDialog(context, R.style.MyDialogTheme);
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
        nDialog.show();
    }

    private void setPlayBtnListener(View view, final CategoryModel category) {
        ImageButton playBtn = view.findViewById(R.id.play_btn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPDialog();
                RequestQueue queue = Volley.newRequestQueue(context);
                final String getLastTaskInCategory = URL + "/task/cate?categoryid=" + category.getId();
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                        getLastTaskInCategory, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                TaskModel task = null;
                                try {
                                    task = getTaskFromResponse(response);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.putExtra("lastTaskInCategory", task);
                                context.startActivity(intent);
                                hidePDialog();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse.statusCode == 404) {
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("noTaskInCategory", category);
                            context.startActivity(intent);
                        }
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
        });
    }

    private TaskModel getTaskFromResponse(JSONObject taskObject) throws JSONException {
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
        return taskModel;
    }

    private Context context;
    private ArrayList<CategoryModel> categories;
    private String token, userId, loginType;
    private ProgressDialog nDialog;
    static final String URL = "https://timecloud-mobile.herokuapp.com/";
    static final String DEFAULT_KEY = "Default";
}
