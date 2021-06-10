package com.vijay.jsonwizard.activities;

import androidx.appcompat.app.AppCompatActivity;

import com.vijay.jsonwizard.utils.NativeFormLangUtils;

public class MultiLanguageActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context base) {
        // get language from prefs
        String lang = NativeFormLangUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(NativeFormLangUtils.setAppLocale(base, lang));
    }
}