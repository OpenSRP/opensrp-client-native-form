package com.vijay.jsonwizard.interfaces;

import androidx.annotation.NonNull;

import org.smartregister.client.utils.contract.ClientFormContract;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 21-05-2020.
 */
public interface RollbackDialogCallback {

    void onFormSelected(@NonNull ClientFormContract.Model selectedForm);

    void onCancelClicked();
}
