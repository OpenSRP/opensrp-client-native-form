package com.vijay.jsonwizard.factory;

import androidx.annotation.Nullable;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.filesource.AssetsFileSource;
import com.vijay.jsonwizard.filesource.DiskFileSource;
import com.vijay.jsonwizard.interfaces.FormFileSource;

public class FileSourceFactoryHelper {

    public static FormFileSource getFileSource(@Nullable String fileSource) {
        if (fileSource != null && fileSource.equalsIgnoreCase(JsonFormConstants.FileSource.DISK))
            return DiskFileSource.INSTANCE;

        return AssetsFileSource.INSTANCE;
    }
}
