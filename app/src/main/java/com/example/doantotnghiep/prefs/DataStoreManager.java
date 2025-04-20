package com.example.doantotnghiep.prefs;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.doantotnghiep.model.User;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.gson.Gson;

public class DataStoreManager {

    public static final String PREF_USER_INFO = "PREF_USER_INFO";

    private static DataStoreManager instance;
    private MySharedPreferences sharedPreferences;

    public static void init(Context context) {
        instance = new DataStoreManager();
        instance.sharedPreferences = new MySharedPreferences(context);
    }

    public static DataStoreManager getInstance() {
        if (instance != null) {
            return instance;
        } else {
            throw new IllegalStateException("Not initialized");
        }
    }

    public static void setUser(@Nullable User user) {
        String jsonUser = "";
        if (user != null) {
            jsonUser = user.toJSon();
        }
        DataStoreManager.getInstance().sharedPreferences
                .putStringValue(PREF_USER_INFO, jsonUser);
    }

    public static User getUser() {
        String jsonUser = DataStoreManager.getInstance()
                .sharedPreferences.getStringValue(PREF_USER_INFO);
        if (!StringUtil.isEmpty(jsonUser)) {
            return new Gson().fromJson(jsonUser, User.class);
        }
        return new User();
    }
}
