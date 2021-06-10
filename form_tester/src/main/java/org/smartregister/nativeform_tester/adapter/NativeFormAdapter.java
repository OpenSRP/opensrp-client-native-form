package org.smartregister.nativeform_tester.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.nativeform.R;
import org.smartregister.nativeform_tester.contract.FormTesterContract;

import java.util.ArrayList;
import java.util.List;

public class NativeFormAdapter extends RecyclerView.Adapter<NativeFormAdapter.MyViewHolder> {

    private List<FormTesterContract.NativeForm> forms = new ArrayList<>();
    private FormTesterContract.View hostView;

    public NativeFormAdapter(@NonNull List<FormTesterContract.NativeForm> forms, @NonNull FormTesterContract.View hostView) {
        this.forms.addAll(forms);
        this.hostView = hostView;
    }

    /**
     * update this adapters options where adapter is changed
     */
    public void refreshViewDataSource(@NonNull List<FormTesterContract.NativeForm> forms) {
        this.forms.clear();
        this.forms.addAll(forms);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.form_tester_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int position) {
        myViewHolder.resetHolder();
        FormTesterContract.NativeForm form = forms.get(position);
        if (form != null) {
            myViewHolder.tvFileName.setText(form.getFileName());
            myViewHolder.tvEventName.setText(form.getFormName());

            myViewHolder.myView.setOnClickListener(v -> hostView.onFormViewClicked(form, v, v.getId()));
        }
    }

    @Override
    public int getItemCount() {
        return forms.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFileName;
        private TextView tvEventName;
        private View myView;

        private MyViewHolder(View view) {
            super(view);
            myView = view;
            tvFileName = view.findViewById(R.id.tvFileName);
            tvEventName = view.findViewById(R.id.tvEventName);
        }

        public View getView() {
            return myView;
        }

        public void resetHolder() {
            tvFileName.setText("");
            tvEventName.setText("");
        }
    }
}
