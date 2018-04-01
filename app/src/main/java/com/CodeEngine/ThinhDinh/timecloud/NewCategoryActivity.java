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
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.CodeEngine.ThinhDinh.timecloud.Utils.Consts;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.CodeEngine.ThinhDinh.timecloud.Model.ProjectModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2/28/2018.
 */

public class NewCategoryActivity extends AppCompatActivity implements View.OnClickListener {

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
            case R.id.accept_btn:
                if (String.valueOf(categoryName.getText()).equals(""))
                    Consts.displayMessage(NewCategoryActivity.this, "Category name is required");
                else
                    addNewCategory();
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_category);

        projectNameTextView = findViewById(R.id.project_name);
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        getTokenUserId();
        getProjectInfo();

        acceptBtn = findViewById(R.id.accept_btn);
        categoryName = findViewById(R.id.category_name);
        categoryDes = findViewById(R.id.category_des);
        acceptBtn.setOnClickListener(this);
    }

    private void addNewCategory() {
        JSONObject projectInput = getInputInfo();
        String newProjectUrl = Consts.URL + "/cate";
        RequestQueue queue = Volley.newRequestQueue(this);
        showPDialog();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, newProjectUrl, projectInput,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        hidePDialog();
                        changeToNewTaskScreen();
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

    private void changeToNewTaskScreen() {
        Intent intent = new Intent(this, NewTaskActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        this.finish();
    }

    private JSONObject getInputInfo() {
        JSONObject js = new JSONObject();
        JSONObject jsProject = new JSONObject();
        try {
            js.put("name", categoryName.getText().toString());
            js.put("description", categoryDes.getText().toString());
            jsProject.put("id", project.getId());
            js.put("project", jsProject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return js;
    }

    private void getProjectInfo() {
        Intent intent = getIntent();
        project = (ProjectModel) intent.getSerializableExtra("project");

        projectNameTextView.setText(project.getName());
        projectNameTextView.setBackgroundColor(Integer.parseInt(project.getColor()));

        line1.setBackgroundColor(Integer.parseInt(project.getColor()));
        line2.setBackgroundColor(Integer.parseInt(project.getColor()));
    }

    private void getTokenUserId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        userId = prefs.getString("userId", Consts.DEFAULT_KEY);
        token = prefs.getString("token", Consts.DEFAULT_KEY);
    }

    private void hidePDialog() {
        if (nDialog != null) {
            nDialog.dismiss();
            nDialog = null;
        }
    }

    private void showPDialog() {
        nDialog = new ProgressDialog(this, R.style.MyDialogTheme);
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
        nDialog.show();
    }

    private TextView projectNameTextView;
    private TextView categoryName, categoryDes;
    private View line1, line2;
    private ProjectModel project;
    private ImageButton acceptBtn;
    private String userId, token;
    private ProgressDialog nDialog;
}
