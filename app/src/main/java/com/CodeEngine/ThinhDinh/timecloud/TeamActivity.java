package com.CodeEngine.ThinhDinh.timecloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.CodeEngine.ThinhDinh.timecloud.Utils.Consts;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.CodeEngine.ThinhDinh.timecloud.Adapter.MemberListViewAdapter;
import com.CodeEngine.ThinhDinh.timecloud.Model.AccountModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by Administrator on 3/15/2018.
 */

public class TeamActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View
        .OnClickListener {

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
            case R.id.invite_btn:
                if (isInputValid()) addMember();
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_timer:
                startActivity(new Intent(this, MainActivity.class));
                this.finish();
                break;
            case R.id.menu_logout:
                signoutAction();
                break;
            case R.id.menu_team:
                drawer.closeDrawer(Gravity.END);
                break;
            case R.id.menu_time_off:
                startActivity(new Intent(this, TimeoffActivity.class));
                drawer.closeDrawer(Gravity.END);
                break;
        }
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);

        memberList = findViewById(R.id.member_list);
        emailView = findViewById(R.id.email_textview);
        inviteBtn = findViewById(R.id.invite_btn);
        inviteBtn.setOnClickListener(this);

        queue = Volley.newRequestQueue(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Team");

        drawer = findViewById(R.id.drawer_layout);
        drawerToggle = new EndDrawerToggle(this, drawer, toolbar, R.string
                .navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(Gravity.END)) {
                    drawer.closeDrawer(Gravity.END);
                } else {
                    drawer.openDrawer(Gravity.END);
                }
            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.menu_team);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions
                .DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        getTokenUserId();
        setMemberList();
    }

    private void addMember() {
        String addMemberUrl = Consts.URL + "/rela/add?email=" + emailView.getText();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, addMemberUrl, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        Consts.displayMessage(TeamActivity.this,"Invite successfully");
                        updateMemberList();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 404){
                    Consts.displayMessage(TeamActivity.this,"This email is unavailable");
                }
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

    private void changeToSigninScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        (this).finish();
    }

    private ArrayList<AccountModel> getMemberListFromResponse(JSONArray members){
        ArrayList<AccountModel> accountModels = new ArrayList<>();
        accountModels.add(new AccountModel("thinhdn266@gmail.com", "Admin"));
        return accountModels;
    };

    private void getTokenUserId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        userId = prefs.getString("userId", Consts.DEFAULT_KEY);
        token = prefs.getString("token", Consts.DEFAULT_KEY);
        loginType = prefs.getString("loginType", Consts.DEFAULT_KEY);
    }

    private void hidePDialog() {
        if (nDialog != null) {
            nDialog.dismiss();
            nDialog = null;
        }
    }

    private boolean isInputValid() {
        String emailText = emailView.getText().toString();
        Matcher m = Consts.EMAIL_PATTERN.matcher(emailText);
        if (TextUtils.isEmpty(emailText)) {
            emailView.setError(getString(R.string.error_email_required));
            return false;
        } else if (!m.matches()) {
            emailView.setError(getString(R.string.error_email_valid));
            return false;
        } else return true;
    }

    private void setMemberList() {
        accounts = new ArrayList<>();
        String addMemberUrl = Consts.URL + "/rela/colleagues";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, addMemberUrl, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        try {
                            accounts = getMemberListFromResponse(response.getJSONArray("colleagues"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        memberListViewAdapter = new MemberListViewAdapter(TeamActivity.this, accounts);
                        memberList.setAdapter(memberListViewAdapter);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 404){
                    Consts.displayMessage(TeamActivity.this,"This email is unavailable");
                }
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

    private void showPDialog() {
        nDialog = new ProgressDialog(this, R.style.MyDialogTheme);
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
        nDialog.show();
    }

    private void signoutAction() {
        showPDialog();
        RequestQueue queue = Volley.newRequestQueue(this);
        String signoutURL = Consts.URL + "signout";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, signoutURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hidePDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePDialog();
                Consts.displayMessage(TeamActivity.this, "error");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", token);
                return params;
            }
        };
        signoutSuccess();
        queue.add(stringRequest);
    }

    private void signoutSuccess() {
        switch (loginType) {
            case "Google":
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                removeTokenUsername();
                                changeToSigninScreen();
                            }
                        });
                break;
            case "Facebook":
                //sign out facebook
                break;
            default:
                removeTokenUsername();
                changeToSigninScreen();
                break;
        }
    }

    private void removeTokenUsername() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor ed = prefs.edit();
        ed.clear();
        ed.apply();
    }

    private void updateMemberList(){
        accounts = new ArrayList<>();
        String addMemberUrl = Consts.URL + "/rela/colleagues";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, addMemberUrl, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        try {
                            accounts = getMemberListFromResponse(response.getJSONArray("colleagues"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        memberListViewAdapter = new MemberListViewAdapter(TeamActivity.this, accounts);
                        memberList.setAdapter(memberListViewAdapter);
                        memberList.invalidateViews();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 404){
                    Consts.displayMessage(TeamActivity.this,"This email is unavailable");
                }
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

    private EditText emailView;
    private Button inviteBtn;
    private DrawerLayout drawer;
    private EndDrawerToggle drawerToggle;
    private String token, userId, loginType;
    private ProgressDialog nDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private ListView memberList;
    private MemberListViewAdapter memberListViewAdapter;
    private ArrayList<AccountModel> accounts;
    private RequestQueue queue;
}
