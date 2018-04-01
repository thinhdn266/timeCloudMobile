package com.CodeEngine.ThinhDinh.timecloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.CodeEngine.ThinhDinh.timecloud.Utils.Consts;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by Administrator on 1/18/2018.
 */

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
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
    public void onBackPressed() {
        if (back_pressed + Consts.TIME_DELAY > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), "Press once again to exit!",
                    Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup_btn:
                if (isNetworkAvailable()) {
                    if (isInputValid()) signupAction();
                } else {
                    Toast.makeText(SignupActivity.this, "No internet connection", Toast
                            .LENGTH_LONG).show();
                }
                break;
            case R.id.change_to_login_btn:
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                (SignupActivity.this).finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailView = findViewById(R.id.email);
        passwordView = findViewById(R.id.password);
        confirmPasswordView = findViewById(R.id.confirm_password);
        signupBtn = findViewById(R.id.signup_btn);
        changeToLoginBtn = findViewById(R.id.change_to_login_btn);
        signupBtn.setOnClickListener(this);
        changeToLoginBtn.setOnClickListener(this);
        queue = Volley.newRequestQueue(this);

    }

    private void changeToMainScreen() {
        intent = new Intent(SignupActivity.this, MainActivity.class);
        startActivity(intent);
        (SignupActivity.this).finish();
    }

    private void createDeviceGroup() {
        JSONObject createDeviceGroup = createDeviceGroupIson();
        showPDialog();
        JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, Consts.NOTI_URL,
                createDeviceGroup, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    hidePDialog();
                    notiKey = response.getString("notification_key");
                    Log.e("devicesKey", notiKey);
//                    sendNotiKeyToServer();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePDialog();
                displayErrorMessage(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("project_id", Consts.SENDER_ID);
                params.put("Content-Type", "application/json");
                params.put("Authorization", "key=" + Consts.API_KEY);
                return params;
            }
        };
        queue.add(jsonObject);
    }

    private JSONObject createDeviceGroupIson() {
        String email = emailView.getText().toString();
        String username = email.split("@")[0];
        notiKeyName = "app" + username;
        JSONObject js = new JSONObject();
        ArrayList<String> regisArray = new ArrayList<>();
        regisArray.add(FirebaseInstanceId.getInstance().getToken());
        try {
            js.put("operation", "create");
            js.put("notification_key_name", notiKeyName);
            js.put("registration_ids", regisArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return js;
    }

    private void displayErrorMessage(VolleyError error) {
        switch (error.networkResponse.statusCode) {
            case 409:
                Consts.displayMessage(SignupActivity.this, "This email already exists");
                break;
        }
    }

    private JSONObject getInputInfo() {
        JSONObject js = new JSONObject();
        try {
            js.put("email", emailView.getText().toString());
            js.put("password", passwordView.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return js;
    }

    private void hidePDialog() {
        if (nDialog != null) {
            nDialog.dismiss();
            nDialog = null;
        }
    }

    private boolean isInputValid() {
        String emailText = emailView.getText().toString();
        String passwordText = passwordView.getText().toString();
        String confirmPasswordText = confirmPasswordView.getText().toString();
        Matcher m = Consts.EMAIL_PATTERN.matcher(emailText);
        if (TextUtils.isEmpty(emailText)) {
            emailView.setError(getString(R.string.error_email_required));
            return false;
        } else if (TextUtils.isEmpty(passwordText)) {
            passwordView.setError(getString(R.string.error_password_required));
            return false;
        } else if (!passwordText.equals(confirmPasswordText)) {
            confirmPasswordView.setError(getString(R.string.error_confirm_password_notmatch));
            return false;
        } else if (!m.matches()) {
            emailView.setError(getString(R.string.error_email_valid));
            return false;
        } else if (passwordText.length() < 6) {
            passwordView.setError(getString(R.string.error_password_too_short));
            return false;
        } else return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void saveTokenEmail() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString("userId", userId);
        ed.putString("email", email);
        ed.putString("token", token);
        ed.putString("loginType", loginType);
        ed.apply();
    }

    private void signupAction() {
        showPDialog();

        userInput = getInputInfo();
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Consts.URL,
                userInput,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            hidePDialog();
                            signupSuccess(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePDialog();
                displayErrorMessage(error);
//                mTextView.setText((String.valueOf(error.networkResponse.statusCode)));
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void signupSuccess(JSONObject response) throws JSONException {
        token = response.getString("token");
        JSONObject account = response.getJSONObject("account");
        userId = account.getString("id");
        email = account.getString("email");
        loginType = Consts.DEFAULT_KEY;
        saveTokenEmail();

        createDeviceGroup();
//        changeToMainScreen();
    }

    private void showPDialog() {
        nDialog = new ProgressDialog(SignupActivity.this, R.style.MyDialogTheme);
        nDialog.setMessage("Loading..");
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
        nDialog.show();
    }

    private Button signupBtn;
    private EditText emailView, passwordView, confirmPasswordView;
    private TextView changeToLoginBtn;
    private JSONObject userInput;
    private String token, email, loginType, userId;
    private String notiKey, notiKeyName;
    private Intent intent;
    private ProgressDialog nDialog;
    private RequestQueue queue;
    private static long back_pressed;
}
