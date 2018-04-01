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
import android.widget.ToggleButton;

import com.CodeEngine.ThinhDinh.timecloud.Utils.Consts;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2/8/2018.
 */

public class NewProjectActivity extends AppCompatActivity implements View.OnClickListener {

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
                if (String.valueOf(projectName.getText()).equals(""))
                    Consts.displayMessage(NewProjectActivity.this,"Project name is required");
                else addNewProject();
                break;
            case R.id.red_btn:
                isPressed.setChecked(false);
                redBtn.setChecked(true);
                isPressed = redBtn;
                colorPressed = color.get("red");
                break;
            case R.id.blue_btn:
                isPressed.setChecked(false);
                blueBtn.setChecked(true);
                isPressed = blueBtn;
                colorPressed = color.get("blue");
                break;
            case R.id.brown_btn:
                isPressed.setChecked(false);
                brownBtn.setChecked(true);
                isPressed = brownBtn;
                colorPressed = color.get("blue");
                break;
            case R.id.lime_btn:
                isPressed.setChecked(false);
                limeBtn.setChecked(true);
                isPressed = limeBtn;
                colorPressed = color.get("lime");
                break;
            case R.id.orange_btn:
                isPressed.setChecked(false);
                orangeBtn.setChecked(true);
                isPressed = orangeBtn;
                colorPressed = color.get("orange");
                break;
            case R.id.pink_btn:
                isPressed.setChecked(false);
                pinkBtn.setChecked(true);
                isPressed = pinkBtn;
                colorPressed = color.get("pink");
                break;
            case R.id.purple_btn:
                isPressed.setChecked(false);
                purpleBtn.setChecked(true);
                isPressed = purpleBtn;
                colorPressed = color.get("purple");
                break;
            case R.id.seafoam_btn:
                isPressed.setChecked(false);
                seafoamBtn.setChecked(true);
                isPressed = seafoamBtn;
                colorPressed = color.get("seafoam");
                break;
            case R.id.yellow_btn:
                isPressed.setChecked(false);
                yellowBtn.setChecked(true);
                isPressed = yellowBtn;
                colorPressed = color.get("yellow");
                break;
            case R.id.aqua_btn:
                isPressed.setChecked(false);
                aquaBtn.setChecked(true);
                isPressed = aquaBtn;
                colorPressed = color.get("aqua");
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_project);

        setColorAttr();
        setColorBtn();
        getTokenUserId();

        acceptBtn = findViewById(R.id.accept_btn);
        projectName = findViewById(R.id.task_name);
        projectDes = findViewById(R.id.project_des);
        acceptBtn.setOnClickListener(this);

    }

    private void addNewProject() {
        JSONObject projectInput = getInputInfo();
        String newProjectUrl = Consts.URL + "/pj";
        RequestQueue queue = Volley.newRequestQueue(this);
        showPDialog();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                newProjectUrl, projectInput,
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
        Intent intent = new Intent(NewProjectActivity.this, NewTaskActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        NewProjectActivity.this.finish();
    }

    private JSONObject getInputInfo() {
        JSONObject js = new JSONObject();
        try {
            js.put("name", projectName.getText().toString());
            js.put("description", projectDes.getText().toString());
            js.put("backgroundcolor", String.valueOf(colorPressed));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return js;
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

    private void setColorBtn() {
        redBtn = findViewById(R.id.red_btn);
        blueBtn = findViewById(R.id.blue_btn);
        orangeBtn = findViewById(R.id.orange_btn);
        limeBtn = findViewById(R.id.lime_btn);
        purpleBtn = findViewById(R.id.purple_btn);
        pinkBtn = findViewById(R.id.pink_btn);
        brownBtn = findViewById(R.id.brown_btn);
        seafoamBtn = findViewById(R.id.seafoam_btn);
        aquaBtn = findViewById(R.id.aqua_btn);
        yellowBtn = findViewById(R.id.yellow_btn);

        redBtn.setOnClickListener(this);
        blueBtn.setOnClickListener(this);
        orangeBtn.setOnClickListener(this);
        limeBtn.setOnClickListener(this);
        pinkBtn.setOnClickListener(this);
        purpleBtn.setOnClickListener(this);
        brownBtn.setOnClickListener(this);
        seafoamBtn.setOnClickListener(this);
        aquaBtn.setOnClickListener(this);
        yellowBtn.setOnClickListener(this);

        redBtn.setChecked(true);
        colorPressed = color.get("red");
        isPressed = redBtn;
    }

    private void showPDialog() {
        nDialog = new ProgressDialog(this, R.style.MyDialogTheme);
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
        nDialog.show();
    }

    private ImageButton acceptBtn;
    private EditText projectName, projectDes;
    private String userId, token;
    private int colorPressed;
    private ProgressDialog nDialog;
    private ToggleButton isPressed, redBtn, blueBtn, brownBtn, yellowBtn, orangeBtn, aquaBtn,
            purpleBtn, pinkBtn, seafoamBtn, limeBtn;
    private HashMap<String, Integer> color;
}
