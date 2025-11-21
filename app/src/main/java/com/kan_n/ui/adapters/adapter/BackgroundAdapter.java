package com.kan_n.ui.adapters.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable; // ✨ 1. Thêm import này
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kan_n.R;
import com.kan_n.data.models.Background;

import java.util.List;

public class BackgroundAdapter extends RecyclerView.Adapter<BackgroundAdapter.BackgroundViewHolder> {

    // (Giữ nguyên các biến và interface)
    private final Context context;
    private List<Background> backgroundList;
    private final OnBackgroundClickListener listener;

    public interface OnBackgroundClickListener {
        void onBackgroundClick(Background background);
    }

    // (Giữ nguyên Constructor, onCreateViewHolder, getItemCount, updateData)

    public BackgroundAdapter(Context context, List<Background> backgroundList, OnBackgroundClickListener listener) {
        this.context = context;
        this.backgroundList = backgroundList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BackgroundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_background_option, parent, false);
        return new BackgroundViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return backgroundList != null ? backgroundList.size() : 0;
    }

    public void updateData(List<Background> newBackgroundList) {
        this.backgroundList = newBackgroundList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull BackgroundViewHolder holder, int position) {
        Background background = backgroundList.get(position);
        if (background == null) return;
        holder.bind(background, listener);
    }

    // --- ViewHolder (Nơi sửa lỗi) ---
    public static class BackgroundViewHolder extends RecyclerView.ViewHolder {

        ImageView ivBackgroundPreview;

        public BackgroundViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBackgroundPreview = itemView.findViewById(R.id.iv_background_preview);
        }

        public void bind(final Background background, final OnBackgroundClickListener listener) {
            ivBackgroundPreview.setImageDrawable(null);
            ivBackgroundPreview.setBackgroundColor(Color.TRANSPARENT);
            Context context = itemView.getContext();

            if ("color".equalsIgnoreCase(background.getType()) && background.getValue() != null) {
                try {
                    // Dòng này vẫn ĐÚNG
                    ivBackgroundPreview.setBackgroundColor(Color.parseColor(background.getValue()));
                } catch (IllegalArgumentException e) {
                    ivBackgroundPreview.setBackgroundColor(Color.LTGRAY);
                }
            } else if ("image".equalsIgnoreCase(background.getType()) && background.getValue() != null) {

                // Bọc mã màu bằng ColorDrawable
                Glide.with(context)
                        .load(background.getValue())
                        .centerCrop()
                        .placeholder(new ColorDrawable(Color.LTGRAY)) // Sửa
                        .error(new ColorDrawable(Color.GRAY))       // Sửa
                        .into(ivBackgroundPreview);
            } else {
                ivBackgroundPreview.setBackgroundColor(Color.LTGRAY);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBackgroundClick(background);
                }
            });
        }
    }
}