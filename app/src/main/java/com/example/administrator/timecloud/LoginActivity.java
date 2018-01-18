package com.example.administrator.timecloud;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = LoginActivity.class.getSimpleName();
    TextView changeToSignupBtn;
    Button loginBtn;
    EditText email, password;
    JSONObject userInput;
    String token;
    int userId;
    Intent intent;
    static final String URL = "https://timxengon.herokuapp.com/signin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login_btn);
        changeToSignupBtn = findViewById(R.id.change_to_signup_btn);
        changeToSignupBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_btn:
                if (isNetworkAvailable()) {
                    if (isInputValid()) loginAction();
                } else {
                    Toast.makeText(LoginActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.change_to_signup_btn:
                intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
        }

    }

    public boolean isInputValid() {
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();
        if (TextUtils.isEmpty(emailText)) {
            email.setError(getString(R.string.error_email_required));
            return false;
        } else {
            if (TextUtils.isEmpty(passwordText)) {
                password.setError(getString(R.string.error_confirm_password_required));
                return false;
//            } else {
//                if (!Pattern.matches("^[a-zA-Z0-9]*$", email.getText())) {
//                    email.setError("Email only contains letters and numbers");
//                    return false;
            } else return true;
        }
    }

    public void loginAction() {
        userInput = getInputInfo();
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, userInput,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            loginSuccess(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayErrorMessage(error);
//                mTextView.setText((String.valueOf(error.networkResponse.statusCode)));
            }
        });
        queue.add(jsonObjectRequest);
    }

    public void displayErrorMessage(VolleyError error) {
        String json = null;
        NetworkResponse response = error.networkResponse;
        if (response != null && response.data != null) {
            json = new String(response.data);
            json = trimMessage(json, "errors");
            if (json != null) displayMessage(json);
//                switch (response.statusCode) {
//                    case 400:
//                        json = new String(response.data);
//                        json = trimMessage(json, "message");
//                        if (json != null) displayMessage(json);
//                        break;
//                    case 401:
//                        json = new String(response.data);
//                        json = trimMessage(json, "message");
//                        if (json != null) displayMessage(json);
//                        break;
        }
    }

    public void displayMessage(String toastString) {
        Toast.makeText(LoginActivity.this, toastString, Toast.LENGTH_LONG).show();
    }

    private String trimMessage(String json, String key) {
        String trimmedString = null;
        try {
            JSONObject obj = new JSONObject(json);
            trimmedString = obj.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return trimmedString;
    }

    private JSONObject getInputInfo() {
        JSONObject js = new JSONObject();
        try {
            js.put("username", email.getText().toString());
            js.put("password", password.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return js;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void loginSuccess(JSONObject response) throws JSONException {
        token = response.getString("authentoken");
        userId = response.getInt("id");
        Bundle infoUser = new Bundle();
        infoUser.putString("token", token);
        infoUser.putInt("userId", userId);
        intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtras(infoUser);
        startActivity(intent);
    }
}

//            get Google Account infomation
//            final TextView mTextView = (TextView) findViewById(R.id.test);
//            AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
//            Account[] list = manager.getAccounts();
//            String gmail = null;
//
//            for(Account account: list)
//            {
//                if(account.type.equalsIgnoreCase("com.google"))
//                {
//                    gmail = account.name;
//                    break;
//                }
//            }
//            mTextView.setText(gmail);