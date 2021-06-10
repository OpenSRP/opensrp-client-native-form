package org.smartregister.nativeform_tester;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.activities.JsonWizardFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.smartregister.nativeform.R;
import org.smartregister.nativeform_tester.adapter.NativeFormAdapter;
import org.smartregister.nativeform_tester.contract.AndroidPermissionHelper;
import org.smartregister.nativeform_tester.contract.FormTesterContract;
import org.smartregister.nativeform_tester.presenter.FormTesterPresenter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

public class FormTesterActivity extends AppCompatActivity implements FormTesterContract.View {

    private static final int REQUEST_CODE_GET_JSON = 1234;
    protected NativeFormAdapter mAdapter;
    protected ProgressBar progressBar;
    private FormTesterContract.Presenter presenter;
    private List<FormTesterContract.NativeForm> formList = new ArrayList<>();
    private AtomicInteger loadingRequests = new AtomicInteger(0);
    private Map<Integer, AndroidPermissionHelper.Requester> requesterMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_tester_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bindViews();
        registerPresenter();
    }

    @Override
    public void bindViews() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new NativeFormAdapter(formList, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    public void registerPresenter() {
        if (presenter == null) {
            presenter = new FormTesterPresenter()
                    .forView(this)
                    .initialize(getApplicationContext());
        }
    }

    @Override
    public void startForm(@NonNull JSONObject jsonObject, @Nullable Form form) {

        Class<?> cls = (jsonObject.has("step2")) ? JsonWizardFormActivity.class : JsonFormActivity.class;
        Intent intent = new Intent(this, cls);
        intent.putExtra("json", jsonObject.toString());
        intent.putExtra(JsonFormConstants.PERFORM_FORM_TRANSLATION, false);
        intent.putExtra(JsonFormConstants.FROM_DATA_SOURCE, JsonFormConstants.FileSource.DISK);
        if (form != null)
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

        Timber.d("form is " + jsonObject.toString());
        startActivityForResult(intent, REQUEST_CODE_GET_JSON);
    }

    @Override
    public void displayForms(List<FormTesterContract.NativeForm> nativeForms) {
        this.formList.clear();
        this.formList.addAll(nativeForms);
        mAdapter.refreshViewDataSource(nativeForms);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void isLoading(boolean isLoading) {
        int loadingRequest = isLoading ? loadingRequests.incrementAndGet() : loadingRequests.decrementAndGet();
        if (loadingRequest < 0)
            loadingRequests.set(0);

        progressBar.setVisibility(loadingRequest > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onFormViewClicked(FormTesterContract.NativeForm selectedObject, View hostView, int viewID) {
        if (selectedObject.isValid() && selectedObject.getJsonForm() != null) {
            startForm(selectedObject.getJsonForm(), selectedObject.getFormDetails());
        }
    }

    @Override
    public void displayMessage(int errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Context getContext() {
        return this;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.form_tester_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reset_app) {
            presenter.resetApp();
        } else if (id == R.id.action_refresh) {
            presenter.reloadFormsOnDevice();
        }

        return super.onOptionsItemSelected(item);
    }


    public void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Timber.v("Permission is granted1");
            } else {

                Timber.v("Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Timber.v("Permission is granted1");
        }
    }

    @Override
    public boolean checkPermissions(List<String> requiredPermissions) {
        if (Build.VERSION.SDK_INT < 23)
            return true;

        boolean isGranted = true;
        int x = 0;
        while (x < requiredPermissions.size()) {
            if (checkSelfPermission(requiredPermissions.get(x)) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            }
            x++;
        }

        return isGranted;
    }

    @Override
    public void checkOrRequestPermissions(List<String> requiredPermissions, AndroidPermissionHelper.Requester requester) {
        if (Build.VERSION.SDK_INT < 23) {
            requester.onHasPermissions();
            return;
        }

        List<String> missingPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (missingPermissions.size() == 0) {
            requester.onHasPermissions();
        } else {
            int request = Integer.parseInt(new SimpleDateFormat("HHmm", Locale.ENGLISH).format(new Date()));
            requesterMap.put(request, requester);

            ActivityCompat.requestPermissions(this,
                    missingPermissions.toArray(new String[missingPermissions.size()]),
                    request);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NotNull String[] permissions, @NotNull int[] grantResults) {

        AndroidPermissionHelper.Requester requester = requesterMap.get(requestCode);
        if (requester != null) {

            if (grantResults.length < 1) {
                requester.onMissingPermissions(Arrays.asList(permissions));
            } else {
                List<String> missingPermissions = new ArrayList<>();

                int position = 0;
                for (int state : grantResults) {
                    if (state != PackageManager.PERMISSION_GRANTED) {
                        missingPermissions.add(permissions[position]);
                    }
                    position++;
                }

                if (missingPermissions.size() > 0) {
                    requester.onMissingPermissions(missingPermissions);
                } else {
                    requester.onPermissionsGranted();
                }
            }
        }
    }
}
