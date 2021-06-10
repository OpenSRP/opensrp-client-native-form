package org.smartregister.nativeform_tester.interactor;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.smartregister.nativeform_tester.contract.FormTesterContract;
import org.smartregister.nativeform_tester.domain.ConfigForm;
import org.smartregister.nativeform_tester.domain.JsonForm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FormTesterInteractor implements FormTesterContract.Interactor {
    private Gson gson = null;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    public void exportDefaultForms(Context context, FormTesterContract.Presenter presenter) {
        executorService.execute(() -> {
            AssetManager assetManager = context.getAssets();
            try {
                String root = verifyOrCreateDiskDirectory();

                String[] imageSources = {JsonFormConstants.JSON_FORM_DIRECTORY, "json.form.config", "img", "image", "rule"};
                for (String sourceDir : imageSources) {
                    exportDirectory(assetManager, sourceDir, context, root, true);
                }

                readForms(context, presenter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void exportDirectory(AssetManager assetManager, String sourceDir, Context context, String root, boolean createRoot) throws IOException {
        String[] localAssets = assetManager.list(sourceDir);
        if (localAssets != null && localAssets.length > 0) {
            for (String assetName : localAssets) {
                // export all in
                try {

                    // if the assetName is a folder extract this
                    String[] childAssets = assetManager.list(sourceDir + "/" + assetName);
                    if (childAssets != null && childAssets.length > 0) {
                        String destinationRoot = root + "/" + assetName;
                        File myDir = new File(destinationRoot);
                        if (!myDir.exists())
                            myDir.mkdirs();

                        exportDirectory(assetManager, sourceDir + "/" + assetName, context, destinationRoot, false);

                    } else {
                        String fileRoot = createRoot ? root + "/" + sourceDir : root;
                        if (createRoot) {
                            File myDir = new File(root + "/" + sourceDir);
                            if (!myDir.exists())
                                myDir.mkdirs();
                        }

                        exportBinaryAsset(assetName, context, fileRoot, sourceDir);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void exportBinaryAsset(String assetName, Context context, String root, String rootSource) throws IOException {
        InputStream inputStream = context.getAssets()
                .open(rootSource + "/" + assetName);

        byte[] targetArray = new byte[inputStream.available()];
        inputStream.read(targetArray);

        File file = new File(root, assetName);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(targetArray);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void validateForm(Context context, String formName) {
        // TODO
    }

    @Override
    public String verifyOrCreateDiskDirectory() {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File myDir = new File(root + "/" + JsonFormConstants.DEFAULT_FORMS_DIRECTORY);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        return myDir.toString();
    }

    @Override
    public boolean verifyFormsDirectoryExists() {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File myDir = new File(root + "/" + JsonFormConstants.DEFAULT_FORMS_DIRECTORY + "/" + JsonFormConstants.JSON_FORM_DIRECTORY + "/");
        return myDir.exists();
    }

    @Override
    public void readForms(@NotNull Context context, FormTesterContract.Presenter presenter) {
        executorService.execute(() -> {
            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            File myDir = new File(root + "/" + JsonFormConstants.DEFAULT_FORMS_DIRECTORY + "/" + JsonFormConstants.JSON_FORM_DIRECTORY + "/");

            if (myDir.exists()) {
                List<FormTesterContract.NativeForm> nativeForms = new ArrayList<>();
                for (File file : myDir.listFiles()) {
                    String type = getFileExtension(file.getName());
                    if (type.equalsIgnoreCase("json"))
                        nativeForms.add(new JsonForm(file, readFileForm(context, file.getName())));
                }

                Collections.sort(nativeForms, (o1, o2) -> o1.getFileName().compareTo(o2.getFileName()));

                executeOnMainThread(() -> presenter.onFormsRead(nativeForms));
            }
        });
    }

    @Override
    public void executeOnMainThread(@NotNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    @Nullable
    private Form readFileForm(Context context, String formName) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File formFile = new File(root + "/" + JsonFormConstants.DEFAULT_FORMS_DIRECTORY + "/json.form.config/" + formName);
        if (formFile.exists()) {
            // parse and create a form object
            if (gson == null)
                gson = new Gson();

            try {
                String content = Utils.getFileContentsAsString(formFile);
                ConfigForm configForm = gson.fromJson(content, ConfigForm.class);
                return configForm.toForm(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private String getFileExtension(String fullName) {
        String fileName = new File(fullName).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

}
