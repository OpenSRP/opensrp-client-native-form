package com.vijay.jsonwizard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import timber.log.Timber;

import static com.vijay.jsonwizard.utils.Utils.convertStreamToString;

/**
 * Created by Vincent Karuri on 16/03/2020
 */
public class TestUtils {

    public String getResourceFileContentsAsString(String filePath) {
        return convertStreamToString(getTestResource(filePath));
    }

    public InputStream getTestResource(String filePath) {
        return getClass().getClassLoader().getResourceAsStream(filePath);
    }

    public void copyFilesIntoResourcesFolder(String sourcePath) {
        String[] filePath = sourcePath.split(File.separator);
        String fileName = filePath[filePath.length - 1];
        copyFile(sourcePath, getResourcesFilePath() + File.separator + fileName);
    }

    public void copyFile(String sourcePath, String destPath) {
        try {
            deleteFile(destPath);
            Files.copy(Paths.get(sourcePath), Paths.get(destPath));
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public String getResourcesFilePath() {
        return getBasePackageFilePath() + "/src/test/resources";
    }

    public void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    public String getBasePackageFilePath() {
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }
}
