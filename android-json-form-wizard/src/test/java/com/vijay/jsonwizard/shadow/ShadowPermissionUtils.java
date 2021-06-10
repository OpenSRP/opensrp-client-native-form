package com.vijay.jsonwizard.shadow;

import androidx.fragment.app.Fragment;

import com.vijay.jsonwizard.utils.PermissionUtils;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by samuelgithengi on 3/17/20.
 */
@Implements(PermissionUtils.class)
public class ShadowPermissionUtils {

    @Implementation
    public static boolean isPermissionGranted(Fragment fragment, String[] permissions, int requestCode) {
        return true;
    }
}
