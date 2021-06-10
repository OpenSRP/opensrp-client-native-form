package com.vijay.jsonwizard.utils;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionUtils {

    public static final int CAMERA_PERMISSION_REQUEST_CODE = 15141;
    public static final int PHONE_STATE_PERMISSION_REQUEST_CODE = 14151;
    public static final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 15142;
    public static final int PHONE_STATE_PERMISSION = 15143;


    public static boolean isPermissionGranted(Fragment fragment, String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(fragment.getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed; request the permission
            fragment.requestPermissions(new String[]{permission}, requestCode);
            return false;
        } else {
            return true;
        }
    }

    public static boolean isPermissionGranted(Activity activity, String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            return false;
        } else {
            return true;
        }
    }

    public static boolean isPermissionGranted(Fragment fragment, String[] permissions, int requestCode) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }

        List<String> notGranted = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(fragment.getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                notGranted.add(permission);
            }
        }

        if (notGranted.isEmpty()) {
            return true;
        }

        fragment.requestPermissions(notGranted.toArray(new String[notGranted.size()]), requestCode);
        return false;
    }

    public static boolean verifyPermissionGranted(String permissions[], int[] grantResults, String... permissionsToVerify) {
        Map<String, Integer> perms = new HashMap<>();
        // Initialize the map with both permissions
        for (String permission : permissionsToVerify) {
            perms.put(permission, PackageManager.PERMISSION_GRANTED);
        }
        // Fill with actual results from user
        for (int i = 0; i < permissions.length; i++) {
            perms.put(permissions[i], grantResults[i]);
        }

        for (String permission : permissionsToVerify) {
            if (perms.get(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}
