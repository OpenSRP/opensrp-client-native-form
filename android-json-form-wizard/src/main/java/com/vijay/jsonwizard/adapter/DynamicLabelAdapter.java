package com.vijay.jsonwizard.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.model.DynamicLabelInfo;
import com.vijay.jsonwizard.utils.FormUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;

import timber.log.Timber;

public class DynamicLabelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final ArrayList<DynamicLabelInfo> dynamicLabelInfoList;

    public DynamicLabelAdapter(Context context, ArrayList<DynamicLabelInfo> dynamicLabelInfoList) {
        this.context = context;
        this.dynamicLabelInfoList = dynamicLabelInfoList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dynamic_dialog_row_layout, parent, false);
        return new RecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final RecyclerViewHolder recyclerViewHolder = (RecyclerViewHolder) holder;
        DynamicLabelInfo dynamicLabelInfo = dynamicLabelInfoList.get(position);
        String dynamicLabelTitle = dynamicLabelInfo.getDynamicLabelTitle();
        if (StringUtils.isNotBlank(dynamicLabelTitle)) {
            recyclerViewHolder.tileTextView.setText(dynamicLabelTitle);
            recyclerViewHolder.tileTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerViewHolder.tileTextView.setVisibility(View.GONE);
        }

        String dynamicLabelText = dynamicLabelInfo.getDynamicLabelText();
        if (StringUtils.isNotBlank(dynamicLabelText)) {
            recyclerViewHolder.descriptionTextView.setText(dynamicLabelText);
            recyclerViewHolder.descriptionTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerViewHolder.descriptionTextView.setVisibility(View.GONE);
        }

        String dynamicLabelImageSrc = dynamicLabelInfo.getDynamicLabelImageSrc();
        if (StringUtils.isNotBlank(dynamicLabelImageSrc)) {
            try {
                recyclerViewHolder.imageViewLabel.setImageDrawable(FormUtils.readImageFromAsset(context, dynamicLabelImageSrc));
                recyclerViewHolder.imageViewLabel.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                Timber.e(e);
                recyclerViewHolder.imageViewLabel.setVisibility(View.GONE);
            }
        } else {
            recyclerViewHolder.imageViewLabel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return dynamicLabelInfoList.size();
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private final TextView descriptionTextView;
        private final TextView tileTextView;
        private final ImageView imageViewLabel;

        private RecyclerViewHolder(View view) {
            super(view);
            descriptionTextView = view.findViewById(R.id.descriptionText);
            tileTextView = view.findViewById(R.id.labelTitle);
            imageViewLabel = view.findViewById(R.id.imageViewLabel);
        }
    }
}
