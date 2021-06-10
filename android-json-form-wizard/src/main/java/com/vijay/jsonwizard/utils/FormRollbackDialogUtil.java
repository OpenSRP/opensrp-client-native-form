package com.vijay.jsonwizard.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interfaces.RollbackDialogCallback;

import org.smartregister.client.utils.contract.ClientFormContract;

import java.util.List;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 22-05-2020.
 */
public class FormRollbackDialogUtil {

    public static AlertDialog showAvailableRollbackFormsDialog(@NonNull final Context context
            , @NonNull final ClientFormContract.Dao clientFormRepository
            , @NonNull final List<ClientFormContract.Model> clientFormList
            , @NonNull final ClientFormContract.Model currentClientForm
            , final @NonNull RollbackDialogCallback rollbackDialogCallback) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setIcon(R.drawable.ic_icon_danger);
        builderSingle.setTitle(R.string.rollback_dialog_title);
        int selectedItem = -1;
        //builderSingle.setMessage("Due to an error on the current form, the form cannot be openned. Kindly select another rollback form to use for the time being.");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_singlechoice);

        int counter = 0;
        for (ClientFormContract.Model clientForm : clientFormList) {
            if (clientForm.getVersion().equals(currentClientForm.getVersion())) {
                selectedItem = counter;
                arrayAdapter.add("v" + clientForm.getVersion() + context.getString(R.string.current_corrupted_form));
            } else {
                arrayAdapter.add("v" + clientForm.getVersion());
            }

            counter++;
        }

        arrayAdapter.add(JsonFormConstants.CLIENT_FORM_ASSET_VERSION);

        builderSingle.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                rollbackDialogCallback.onCancelClicked();
            }
        });

        builderSingle.setSingleChoiceItems(arrayAdapter, selectedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String formVersion = arrayAdapter.getItem(which);

                if (formVersion != null) {
                    boolean wasClickHandled = selectForm(clientFormRepository, which, formVersion, context, clientFormList, currentClientForm, rollbackDialogCallback);

                    if (wasClickHandled) {
                        dialog.dismiss();
                    }
                }
            }
        });
        builderSingle.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (context instanceof ClientFormContract.View) {
                    ((ClientFormContract.View) context).setVisibleFormErrorAndRollbackDialog(false);
                }
            }
        });


        if (context instanceof ClientFormContract.View) {
            ((ClientFormContract.View) context).setVisibleFormErrorAndRollbackDialog(true);
        }
        return builderSingle.show();
    }

    @VisibleForTesting
    protected static boolean selectForm(@NonNull ClientFormContract.Dao clientFormRepository, int pos, @NonNull String formVersion, @NonNull Context context, @NonNull List<ClientFormContract.Model> clientFormList, @NonNull ClientFormContract.Model currentClientForm, @NonNull RollbackDialogCallback rollbackDialogCallback) {
        if (formVersion.contains(context.getString(R.string.current_corrupted_form))) {
            Toast.makeText(context, R.string.cannot_select_corrupted_form_rollback, Toast.LENGTH_LONG).show();
            return false;
        } else {
            ClientFormContract.Model selectedClientForm;
            if (formVersion.equals(JsonFormConstants.CLIENT_FORM_ASSET_VERSION)) {
                selectedClientForm = clientFormRepository.createNewClientFormModel();
                selectedClientForm.setVersion(JsonFormConstants.CLIENT_FORM_ASSET_VERSION);
            } else {
                if (pos >= clientFormList.size()) {
                    return false;
                }

                selectedClientForm = clientFormList.get(pos);

                if (selectedClientForm == null) {
                    return false;
                } else {
                    selectedClientForm.setActive(true);
                    clientFormRepository.addOrUpdate(selectedClientForm);
                }
            }

            currentClientForm.setActive(false);
            clientFormRepository.addOrUpdate(currentClientForm);
            rollbackDialogCallback.onFormSelected(selectedClientForm);
            return true;
        }
    }
}
