package com.vijay.jsonwizard.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.MultiSelectItem;
import com.vijay.jsonwizard.widgets.MultiSelectListFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import timber.log.Timber;

public class MultiSelectListSelectedAdapter extends RecyclerView.Adapter<MultiSelectListSelectedAdapter.MyViewHolder> {
    private List<MultiSelectItem> data;
    private static ClickListener clickListener;
    private MultiSelectListFactory multiSelectListFactory;

    public MultiSelectListSelectedAdapter(List<MultiSelectItem> data, MultiSelectListFactory multiSelectListFactory) {
        this.data = data;
        this.multiSelectListFactory = multiSelectListFactory;
    }

    public List<MultiSelectItem> getData() {
        return data;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.multiselectlistselecteditem, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        MultiSelectItem multiSelectItem = data.get(position);
        holder.itemTextView().setText(multiSelectItem.getText());
        holder.deleteImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.remove(position);
                notifyDataSetChanged();

                multiSelectListFactory.writeToForm();
            }
        });
        String value = multiSelectItem.getValue();
        try {
            JSONObject valueJsonObject = new JSONObject(value);
            if (valueJsonObject.has(JsonFormConstants.MultiSelectUtils.META)) {
                holder.additionalInfoView().setVisibility(View.VISIBLE);
                holder.additionalInfoView().setTypeface(Typeface.DEFAULT);
                holder.additionalInfoView().setText(valueJsonObject.optJSONObject(JsonFormConstants.MultiSelectUtils.META).optString(JsonFormConstants.MultiSelectUtils.INFO));
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView multiSelectListTextView;
        private TextView multiSelectListTextViewAdditionalInfo;
        private ImageView imgDelete;

        private MyViewHolder(View view) {
            super(view);
            multiSelectListTextView = view.findViewById(R.id.multiSelectListTextView);
            multiSelectListTextViewAdditionalInfo = view.findViewById(R.id.multiSelectListTextViewAdditionalInfo);
            imgDelete = view.findViewById(R.id.multiSelectListDelete);
            view.setOnClickListener(this);
        }

        public TextView itemTextView() {
            return multiSelectListTextView;
        }

        public ImageView deleteImageView() {
            return imgDelete;
        }

        public TextView additionalInfoView() {
            return multiSelectListTextViewAdditionalInfo;
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemClick(view);
            }
        }
    }

    public interface ClickListener {
        void onItemClick(View view);
    }

    public void setOnClickListener(ClickListener onClickListener) {
        MultiSelectListSelectedAdapter.clickListener = onClickListener;
    }
}
