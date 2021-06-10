/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vijay.jsonwizard.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.interfaces.OnActivityRequestPermissionResultListener;

import java.util.HashMap;
import java.util.Locale;

/**
 * Used to return JavaRosa type device properties
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Jason Rogena (jrogena@ona.io)
 */

public class PropertyManager {

    private HashMap<String, String> mProperties;
    private TelephonyManager mTelephonyManager;
    private Context mContext;

    public final static String DEVICE_ID_PROPERTY = "deviceid"; // imei
    public final static String SUBSCRIBER_ID_PROPERTY = "subscriberid"; // imsi
    public final static String SIM_SERIAL_PROPERTY = "simserial";
    public final static String PHONE_NUMBER_PROPERTY = "phonenumber";
    public static final String ANDROID6_FAKE_MAC = "02:00:00:00:00:00";


    public PropertyManager(Context context) {
        mContext = context;
        mProperties = new HashMap<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            grantPhoneStatePermission();
            handleOnRequestPermissionResults();
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                addPhoneProperties();
            }
        }
    }

    public void grantPhoneStatePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{
                        Manifest.permission.READ_PHONE_STATE}, PermissionUtils.PHONE_STATE_PERMISSION);
            } else {
                mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            }
        } else {
            mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        }
    }

    @SuppressLint("MissingPermission")
    private String getDeviceId() {
        String deviceId = null;
        if (mTelephonyManager != null) {
            if (mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) { //For tablet
                deviceId = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            } else { //for normal phones
                deviceId = mTelephonyManager.getDeviceId();
            }
        }
        return deviceId;
    }

    public String getSingularProperty(String propertyName) {
        // for now, all property names are in english...
        return mProperties.get(propertyName.toLowerCase(Locale.ENGLISH));
    }

    private void handleOnRequestPermissionResults() {
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            if (activity instanceof JsonApi) {
                final JsonApi jsonApi = (JsonApi) activity;
                jsonApi.addOnActivityRequestPermissionResultListener(PermissionUtils.PHONE_STATE_PERMISSION,
                        new OnActivityRequestPermissionResultListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
                                if (PermissionUtils.verifyPermissionGranted(permissions, grantResults, Manifest.permission.READ_PHONE_STATE)) {
                                    addPhoneProperties();
                                } else {
                                    boolean isUserInformed = ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                                            Manifest.permission.READ_PHONE_STATE);
                                    if (isUserInformed) {
                                        jsonApi.showPermissionDeniedDialog();
                                    }
                                }
                            }
                        });
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void addPhoneProperties() {
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = getDeviceId();
        if (deviceId == null) {
            // no SIM -- WiFi only Retrieve WiFiManager
            WifiManager wifi = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            // Get WiFi status
            WifiInfo info = wifi.getConnectionInfo();
            if (info != null && !ANDROID6_FAKE_MAC.equals(info.getMacAddress())) {
                deviceId = info.getMacAddress();
            }
        }
        if (deviceId != null) {
            mProperties.put(DEVICE_ID_PROPERTY, deviceId);
        }
        String value;
        value = mTelephonyManager.getSubscriberId();
        if (value != null) {
            mProperties.put(SUBSCRIBER_ID_PROPERTY, value);
        }
        value = mTelephonyManager.getSimSerialNumber();
        if (value != null) {
            mProperties.put(SIM_SERIAL_PROPERTY, value);
        }
        value = mTelephonyManager.getLine1Number();
        if (value != null) {
            mProperties.put(PHONE_NUMBER_PROPERTY, value);
        }
    }

}
