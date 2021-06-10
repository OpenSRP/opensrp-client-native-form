package com.vijay.jsonwizard.shadow;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.File;

/**
 * Created by samuelgithengi on 9/15/20.
 */
@Implements(FileProvider.class)
public class ShadowFileProvider {

    @Implementation
    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority, @NonNull File file) {
        return Uri.fromFile(new File("profile.jpg"));
    }
}
