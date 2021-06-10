package org.smartregister.nativeform_tester.contract;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;

import java.util.List;

public interface FormTesterContract {

    interface View extends AndroidPermissionHelper.Granter {

        /***
         * read XML files and initialize all necessary views
         */
        void bindViews();

        /**
         * adds a {@link Presenter} to the associated view
         * During initial construction/attachment to a presenter, the view executes  {@link Presenter#initialize() } method
         */
        void registerPresenter();

        /**
         * Renders form
         *
         * @param jsonObject
         */
        void startForm(@NonNull JSONObject jsonObject, @Nullable Form form);

        void displayForms(List<NativeForm> nativeForms);

        void isLoading(boolean isLoading);

        void onFormViewClicked(NativeForm selectedObject, android.view.View hostView, int viewID);

        void displayMessage(@StringRes int errorMessage);

        Context getContext();
    }

    interface Presenter {

        Presenter forView(@NonNull View view);

        Presenter usingModel(@NonNull Interactor interactor);

        Interactor getInteractor();

        Presenter initialize(Context context);

        /**
         * request all needed permissions and
         * re exports all the forms stored in the device
         *
         * @return
         */
        Presenter resetApp();

        void validateForm(Context context, String formName);

        void reloadFormsOnDevice();

        void onFormsRead(List<NativeForm> nativeForms);

        @Nullable
        View getView();

        void deregisterView();

    }

    interface Interactor {

        void exportDefaultForms(Context context, Presenter presenter);

        void validateForm(Context context, String formName);

        /**
         * return the root destination directory
         *
         * @return
         */
        String verifyOrCreateDiskDirectory();

        /**
         * return the root destination directory
         *
         * @return
         */
        boolean verifyFormsDirectoryExists();

        /**
         * Returns a list of all the forms in the disk directory
         *
         * @param context
         * @param presenter
         * @return
         */
        void readForms(@NonNull Context context, Presenter presenter);

        void executeOnMainThread(@NonNull Runnable runnable);

    }

    interface NativeForm {
        @Nullable
        String getFormName();

        String getFileName();

        boolean isValid();

        /**
         * Check for nullability and is the file is valid
         *
         * @return
         */
        @Nullable
        JSONObject getJsonForm();

        @Nullable
        /***
         * Returns a customizable form object if present
         */
        Form getFormDetails();
    }
}
