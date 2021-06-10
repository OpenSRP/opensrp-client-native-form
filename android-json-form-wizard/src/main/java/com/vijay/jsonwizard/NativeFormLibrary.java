package com.vijay.jsonwizard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.smartregister.client.utils.contract.ClientFormContract;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 29-06-2020.
 */
public class NativeFormLibrary {

    private ClientFormContract.Dao clientFormDao;
    private static NativeFormLibrary instance;
    private boolean performFormTranslation = false;

    @NonNull
    public static NativeFormLibrary getInstance() {
        if (instance == null) {
            instance = new NativeFormLibrary();
        }

        return instance;
    }

    @Nullable
    public ClientFormContract.Dao getClientFormDao() {
        return clientFormDao;
    }

    public void setClientFormDao(@NonNull ClientFormContract.Dao clientFormDao) {
        this.clientFormDao = clientFormDao;
    }

    public boolean isPerformFormTranslation() {
        return performFormTranslation;
    }

    public void setPerformFormTranslation(boolean performFormTranslation) {
        this.performFormTranslation = performFormTranslation;
    }
}
