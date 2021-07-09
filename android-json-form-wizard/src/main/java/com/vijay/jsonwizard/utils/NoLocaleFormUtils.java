package com.vijay.jsonwizard.utils;

import android.content.Context;

public class NoLocaleFormUtils extends FormUtils{
    @Override
    protected String getLocaleFormIdentity(Context context, String formIdentity) {
        return formIdentity;
    }
}
