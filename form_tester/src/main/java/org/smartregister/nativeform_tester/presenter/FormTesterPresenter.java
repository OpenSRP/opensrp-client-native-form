package org.smartregister.nativeform_tester.presenter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.smartregister.nativeform.R;
import org.smartregister.nativeform_tester.contract.AndroidPermissionHelper;
import org.smartregister.nativeform_tester.contract.FormTesterContract;
import org.smartregister.nativeform_tester.interactor.FormTesterInteractor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class FormTesterPresenter implements FormTesterContract.Presenter {

    private WeakReference<FormTesterContract.View> view;
    private FormTesterContract.Interactor interactor;

    @Override
    public FormTesterContract.Presenter forView(@NonNull FormTesterContract.View view) {
        this.view = new WeakReference<>(view);
        return this;
    }

    @Override
    public FormTesterContract.Presenter usingModel(@NonNull FormTesterContract.Interactor interactor) {
        this.interactor = interactor;
        return this;
    }

    @Override
    public FormTesterContract.Interactor getInteractor() {
        if (interactor == null)
            interactor = new FormTesterInteractor();

        return interactor;
    }

    @Override
    public FormTesterContract.Presenter initialize(Context context) {
        resetApp();
        return this;
    }

    @Override
    public FormTesterContract.Presenter resetApp() {
        FormTesterContract.View view = getView();
        if (view != null) {
            view.isLoading(true);

            List<String> permissions = new ArrayList<>();
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

            view.checkOrRequestPermissions(permissions,
                    new AndroidPermissionHelper.Requester() {
                        @Override
                        public void onHasPermissions() {
                            if (!getInteractor().verifyFormsDirectoryExists()) {
                                getInteractor().exportDefaultForms(view.getContext(), FormTesterPresenter.this);
                            } else {
                                getInteractor().readForms(view.getContext(), FormTesterPresenter.this);
                            }
                        }

                        @Override
                        public void onMissingPermissions(List<String> missingPermissions) {
                            view.displayMessage(R.string.missing_permissions);
                            view.isLoading(false);
                        }

                        @Override
                        public void onPermissionsGranted() {
                            getInteractor().exportDefaultForms(view.getContext(), FormTesterPresenter.this);
                        }
                    });
        }

        return this;
    }

    @Override
    public void validateForm(Context context, String formName) {
        Timber.v("validateForm");
    }

    @Override
    public void reloadFormsOnDevice() {
        FormTesterContract.View view = getView();
        if (view != null) {
            view.isLoading(true);

            List<String> permissions = new ArrayList<>();
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

            view.checkOrRequestPermissions(permissions,
                    new AndroidPermissionHelper.Requester() {
                        @Override
                        public void onHasPermissions() {
                            getInteractor().readForms(view.getContext(), FormTesterPresenter.this);
                        }

                        @Override
                        public void onMissingPermissions(List<String> missingPermissions) {
                            view.displayMessage(R.string.missing_permissions);
                            view.isLoading(false);
                        }

                        @Override
                        public void onPermissionsGranted() {
                            getInteractor().readForms(view.getContext(), FormTesterPresenter.this);
                        }
                    });
        }
    }

    @Override
    public void onFormsRead(List<FormTesterContract.NativeForm> nativeForms) {
        FormTesterContract.View view = getView();
        if (view == null) return;

        view.displayForms(nativeForms);
        view.isLoading(false);
    }

    @Nullable
    @Override
    public FormTesterContract.View getView() {
        if (view != null)
            return view.get();

        return null;
    }

    @Override
    public void deregisterView() {
        Timber.v("deregisterView");
    }
}
