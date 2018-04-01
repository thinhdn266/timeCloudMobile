package com.CodeEngine.ThinhDinh.timecloud;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.CodeEngine.ThinhDinh.timecloud.Utils.Consts;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;

/**
 * Created by Administrator on 1/31/2018.
 */

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.forgot_password_btn:
                if (isInputValid()) sendEmailToServer();
                break;
            case R.id.back_text:
                super.onBackPressed();
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        queue = Volley.newRequestQueue(this);
        emailReset = findViewById(R.id.email__reset);
        forgotPasswordBtn = findViewById(R.id.forgot_password_btn);
        backText = findViewById(R.id.back_text);
        backText.setOnClickListener(this);
        forgotPasswordBtn.setOnClickListener(this);
    }

    private void changeToResetPasswordScreen() {
        Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    private void hidePDialog() {
        if (nDialog != null) {
            nDialog.dismiss();
            nDialog = null;
        }
    }

    private boolean isInputValid() {
        String emailText = emailReset.getText().toString();
        Matcher m = Consts.EMAIL_PATTERN.matcher(emailText);
        if (TextUtils.isEmpty(emailText)) {
            emailReset.setError(getString(R.string.error_email_required));
            return false;
        } else if (!m.matches()) {
            emailReset.setError(getString(R.string.error_email_valid));
            return false;
        } else return true;
    }

    private void sendEmailToServer() {
        showPDialog();
        email = emailReset.getText().toString();
        String uri = Consts.URL + "/request-resetpassword?email=" + email;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hidePDialog();
                        try {
                            verifyNumber = response.getString("Verification number");
                            setupVerificationDialog();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePDialog();
                Consts.displayMessage(ForgotPasswordActivity.this,"Unvalid Email");
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void setupVerificationDialog() {
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Verification number");
        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputNumber = input.getText().toString();
                if (inputNumber.equals(verifyNumber)) {
                    changeToResetPasswordScreen();
                } else {
                    Consts.displayMessage(ForgotPasswordActivity.this,"Verification Number is invalid");
                }
            }
        });
        builder.show();
    }

    private void showPDialog() {
        nDialog = new ProgressDialog(ForgotPasswordActivity.this, R.style.MyDialogTheme);
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
        nDialog.show();
    }

    private AlertDialog.Builder builder;
    private Button forgotPasswordBtn;
    private ProgressDialog nDialog;
    private EditText emailReset;
    private TextView backText;
    private String email, verifyNumber;
    private RequestQueue queue;
    private JsonObjectRequest jsonObjectRequest;
}
