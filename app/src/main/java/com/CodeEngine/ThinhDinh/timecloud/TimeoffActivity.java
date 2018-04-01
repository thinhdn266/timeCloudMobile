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
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.CodeEngine.ThinhDinh.timecloud.Adapter.TimeoffListViewAdapter;
import com.CodeEngine.ThinhDinh.timecloud.Model.TimeoffModel;
import com.CodeEngine.ThinhDinh.timecloud.Utils.Consts;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 3/23/2018.
 */

public class TimeoffActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

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
                startActivity(new Intent(this, TeamActivity.class));
                drawer.closeDrawer(Gravity.END);
                break;
            case R.id.menu_time_off:
                drawer.closeDrawer(Gravity.END);
                break;
        }
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeoff);

        timeoffList = findViewById(R.id.timeoff_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Time Off");

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
        setTimeoffList();
    }

    private void changeToSigninScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        (this).finish();
    }

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

    private void setTimeoffList() {
        long startTime = System.currentTimeMillis();
        timeoffs = new ArrayList<>();
        timeoffs.add(new TimeoffModel(startTime,startTime-1600000,startTime,"Approved"));
        timeoffs.add(new TimeoffModel(startTime-10000000,startTime-2000000,startTime,"Rejected"));
        timeoffs.add(new TimeoffModel(startTime-14000000,startTime-900000,startTime,"Approved"));
        timeoffListViewAdapter = new TimeoffListViewAdapter(this,timeoffs);
        timeoffList.setAdapter(timeoffListViewAdapter);
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
                Consts.displayMessage(TimeoffActivity.this, "error");
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

    private DrawerLayout drawer;
    private EndDrawerToggle drawerToggle;
    private String token, userId, loginType;
    private ProgressDialog nDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private ListView timeoffList;
    private TimeoffListViewAdapter timeoffListViewAdapter;
    private ArrayList<TimeoffModel> timeoffs;
}
