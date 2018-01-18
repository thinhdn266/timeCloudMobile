package com.example.administrator.timecloud;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 1/18/2018.
 */

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
    Button signupBtn;
    EditText email, password, confirm_password;
    TextView changeToLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.confirm_password);
        signupBtn = findViewById(R.id.signup_btn);
        changeToLoginBtn = findViewById(R.id.change_to_login_btn);
        signupBtn.setOnClickListener(this);
        changeToLoginBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup_btn:
                if(isInputValid()) {}
                break;
            case R.id.change_to_login_btn:
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
        }
    }

    public boolean isInputValid() {
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();
        String confirmPasswordText = confirm_password.getText().toString();
        if (TextUtils.isEmpty(emailText)) {
            email.setError(getString(R.string.error_email_required));
            return false;
        } else {
            if (TextUtils.isEmpty(passwordText)) {
                password.setError(getString(R.string.error_password_required));
                return false;
            } else if (TextUtils.isEmpty(confirmPasswordText)) {
                confirm_password.setError(getString(R.string.error_confirm_password_required));
                return false;
            }
            else if (passwordText.equals(confirmPasswordText)) return true;
            else {
                confirm_password.setError(getString(R.string.error_confirm_password_notmatch));
                return false;
            }
        }
    }

    private JSONObject getInputInfo() {
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
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
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}
