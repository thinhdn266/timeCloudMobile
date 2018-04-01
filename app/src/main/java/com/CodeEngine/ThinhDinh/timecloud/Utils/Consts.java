package com.CodeEngine.ThinhDinh.timecloud.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.regex.Pattern;

/**
 * Created by Administrator on 3/28/2018.
 */

public final class Consts {
    private Consts() {
        throw new AssertionError();
    }

    public static void displayMessage(Context context, String toastString) {
        Toast.makeText(context, toastString, Toast.LENGTH_SHORT).show();
    }

    public static final String URL = "https://timecloud-mobile.herokuapp.com/";
    public static final String NOTI_URL = "https://android.googleapis.com/gcm/notification";
    public static final String SENDER_ID = "306494298185";
    public static final String API_KEY = "AIzaSyCqGe3I1cRkGFML7afc0hxwDe9FBCkMWV0";
    public static final String DEFAULT_KEY = "Default";
    public static final int TIME_DELAY = 2000;
    public static final Pattern EMAIL_PATTERN = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\" +
            ".[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"" +
            "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])" +
            "" + "*\")@" + "(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:" +
            "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
            "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:" +
            "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)" +
            "\\])");
}
