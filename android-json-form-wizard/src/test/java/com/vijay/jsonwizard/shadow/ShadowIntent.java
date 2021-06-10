package com.vijay.jsonwizard.shadow;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import org.robolectric.annotation.Implements;

import static org.mockito.Mockito.mock;

/**
 * Created by samuelgithengi on 9/15/20.
 */
@Implements(Intent.class)
public class ShadowIntent {

    public ComponentName resolveActivity(@NonNull PackageManager pm) {
        return mock(ComponentName.class);
    }
}
