package com.CodeEngine.ThinhDinh.timecloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            signinGoogle(result);
        }
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
        if (!isNetworkAvailable()) {
            Consts.displayMessage(LoginActivity.this, "No Internet Connection");
        } else
            switch (view.getId()) {
                case R.id.login_btn:
                    if (isInputValid()) loginAction();
                    break;
                case R.id.change_to_signup_btn:
                    intent = new Intent(LoginActivity.this, SignupActivity.class);
                    startActivity(intent);
                    (LoginActivity.this).finish();
                    break;
                case R.id.google_btn:
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                    break;
                case R.id.forgot_password:
                    intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                    startActivity(intent);
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
        setContentView(R.layout.activity_login);
        if (isUserSignedIn()) changeToMainScreen();

        googleBtn = findViewById(R.id.google_btn);
        emailView = findViewById(R.id.email);
        passwordView = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login_btn);
        changeToSignupBtn = findViewById(R.id.change_to_signup_btn);
        forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setPaintFlags(forgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        changeToSignupBtn.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        googleBtn.setOnClickListener(this);

        queue = Volley.newRequestQueue(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions
                .DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void addDeviceToGroup() {
        JSONObject addDeviceToGroup = addDeviceToGroupJson();
        showPDialog();
        JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, Consts.NOTI_URL,
                addDeviceToGroup, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    hidePDialog();
                    notiKey = response.getString("notification_key");
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

    private JSONObject addDeviceToGroupJson() {
        String username = email.split("@")[0];
        notiKeyName = "app" + username;
        JSONObject js = new JSONObject();
        ArrayList<String> regisArray = new ArrayList<>();
        regisArray.add(FirebaseInstanceId.getInstance().getToken());
        try {
            js.put("operation", "add");
            js.put("notification_key_name", notiKeyName);
            js.put("notification_key", notiKey);
            js.put("registration_ids", new JSONArray(regisArray));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return js;
    }

    private void changeToMainScreen() {
        intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        (LoginActivity.this).finish();
    }

    private void createDeviceGroup() {
        JSONObject createDeviceGroup = createDeviceGroupIson();
        Log.e("json", createDeviceGroup.toString());
        showPDialog();
        JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, Consts.NOTI_URL, createDeviceGroup,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            hidePDialog();
                            notiKey = response.getString("notification_key");
                            updateNotificationKeyForUser();
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
        String username = email.split("@")[0];
        notiKeyName = "app" + username;
        JSONObject js = new JSONObject();
        ArrayList<String> regisArray = new ArrayList<>();
        regisArray.add(FirebaseInstanceId.getInstance().getToken());
        try {
            js.put("operation", "create");
            js.put("notification_key_name", notiKeyName);
            js.put("registration_ids", new JSONArray(regisArray));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return js;
    }

    private void displayErrorMessage(VolleyError error) {
        if (error.networkResponse != null) {
            switch (error.networkResponse.statusCode) {
                case 401:
                    Consts.displayMessage(LoginActivity.this, "Invalid email or password");
                    break;
                case 404:
                    Consts.displayMessage(LoginActivity.this, "Sorry, we couldn't find an account with that email");
                default:
                    Consts.displayMessage(LoginActivity.this, "Error");
                    break;
            }
        } else Consts.displayMessage(LoginActivity.this, "Cannot connect to server");
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

    private Boolean isUserSignedIn() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        token = prefs.getString("token", Consts.DEFAULT_KEY);
        if (token.equals(Consts.DEFAULT_KEY)) {
            return false;
        } else return true;
    }

    private boolean isInputValid() {
        String emailText = emailView.getText().toString();
        String passwordText = passwordView.getText().toString();
        Matcher m = Consts.EMAIL_PATTERN.matcher(emailText);
        if (TextUtils.isEmpty(emailText)) {
            emailView.setError(getString(R.string.error_email_required));
            return false;
        } else if (TextUtils.isEmpty(passwordText)) {
            passwordView.setError(getString(R.string.error_confirm_password_required));
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void loginAction() {
        userInput = getInputInfo();
        showPDialog();
        String loginUrl = Consts.URL + "/signin";
        JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, loginUrl, userInput,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            hidePDialog();
                            loginSuccess(response, Consts.DEFAULT_KEY);
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
        });
        queue.add(jsonObject);
    }

    private void loginSuccess(JSONObject response, String type) throws JSONException {
        token = response.getString("token");
        JSONObject account = response.getJSONObject("account");
        userId = account.getString("id");
        email = account.getString("email");
        loginType = type;
        saveTokenEmail();

        if (account.has("notificationid")) {
            notiKey = account.getString("notificationid");
            addDeviceToGroup();
        } else {
            createDeviceGroup();
        }
        changeToMainScreen();
    }

    private JSONObject parseEmailToJson(String emailUser) {
        JSONObject js = new JSONObject();
        try {
            js.put("email", emailUser);
        } catch (JSONException e) {
            e.printStackTrace();

        }
        return js;
    }

    private void showPDialog() {
        nDialog = new ProgressDialog(LoginActivity.this, R.style.MyDialogTheme);
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
        nDialog.show();
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

    private void signinGoogle(GoogleSignInResult result) {
        if (result.isSuccess()) {
            acct = result.getSignInAccount();
            email = acct.getEmail();
            userEmail = parseEmailToJson(email);
            showPDialog();
            String loginUrl = Consts.URL + "/signin";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, loginUrl,
                    userEmail,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                hidePDialog();
                                loginSuccess(response, "Google");
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
            });
            queue.add(jsonObjectRequest);
        } else {
            Consts.displayMessage(LoginActivity.this, "There is something wrong with this Google Account");
        }
    }

    private void updateNotificationKeyForUser() {
        JSONObject jsUser = new JSONObject();
        try {
            jsUser.put("id", userId);
            jsUser.put("notificationid", notiKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        showPDialog();
        final String createRecordUrl = Consts.URL + "user";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, createRecordUrl, jsUser,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
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

    private int RC_SIGN_IN = 0;
    private String token, email, loginType, userId, notiKey, notiKeyName;
    private TextView changeToSignupBtn, forgotPassword;
    private Button loginBtn;
    private EditText emailView, passwordView;
    private JSONObject userInput, userEmail;
    private GoogleSignInAccount acct;
    private Intent intent;
    private ProgressDialog nDialog;
    private LinearLayout googleBtn;
    private GoogleApiClient mGoogleApiClient;
    private RequestQueue queue;
    private static long back_pressed;
}
