package com.jsonwizard.application;

import android.app.Application;

import com.vijay.jsonwizard.R;

public class TestApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.Theme_AppCompat);
    }
}