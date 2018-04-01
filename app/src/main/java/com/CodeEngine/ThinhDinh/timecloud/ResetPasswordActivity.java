package com.CodeEngine.ThinhDinh.timecloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.CodeEngine.ThinhDinh.timecloud.Utils.Consts;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 1/31/2018.
 */

public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reset_password_btn:
                changePasswordRequest();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        queue = Volley.newRequestQueue(this);
        resetPasswordBtn = findViewById(R.id.reset_password_btn);
        passwordView = findViewById(R.id.password_);
        confirmPasswordView = findViewById(R.id.confirm_password_);
        resetPasswordBtn.setOnClickListener(this);
    }

    private void changePasswordRequest() {
        showPDialog();
        userInput = getInputInfo();
        String resetPassUrl = Consts.URL + "/changepassword";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, resetPassUrl, userInput,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hidePDialog();
                        changeToLoginScreen();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePDialog();
                Consts.displayMessage(ResetPasswordActivity.this, error.toString());
            }
        }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));

                    JSONObject result = null;

                    if (jsonString != null && jsonString.length() > 0)
                        result = new JSONObject(jsonString);

                    return Response.success(result,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void changeToLoginScreen() {
        intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        startActivity(intent);
        (ResetPasswordActivity.this).finish();
    }

    private JSONObject getInputInfo() {
        JSONObject js = new JSONObject();
        try {
            js.put("email", getIntent().getStringExtra("email"));
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
        String passwordText = passwordView.getText().toString();
        String confirmPasswordText = confirmPasswordView.getText().toString();
        if (TextUtils.isEmpty(passwordText)) {
            passwordView.setError(getString(R.string.error_password_required));
            return false;
        } else if (!passwordText.equals(confirmPasswordText)) {
            confirmPasswordView.setError(getString(R.string.error_confirm_password_notmatch));
            return false;
        } else if (passwordText.length() < 6) {
            passwordView.setError(getString(R.string.error_password_too_short));
            return false;
        } else return true;
    }

    private void showPDialog() {
        nDialog = new ProgressDialog(ResetPasswordActivity.this, R.style.MyDialogTheme);
        nDialog.setMessage("Loading..");
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
        nDialog.show();
    }

    private Button resetPasswordBtn;
    private EditText passwordView, confirmPasswordView;
    private Intent intent;
    private JSONObject userInput;
    private ProgressDialog nDialog;
    private RequestQueue queue;
}
