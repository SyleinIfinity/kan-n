package com.kan_n.ui.adapters.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kan_n.R;
import com.kan_n.data.models.Activity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private List<Activity> activities = new ArrayList<>();

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout mới tên item_card_activity_log
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_activity_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(activities.get(position));
    }

    @Override
    public int getItemCount() {
        return activities != null ? activities.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        void bind(Activity activity) {
            tvContent.setText(activity.getContent());
            tvTime.setText(formatDate(activity.getTimestamp()));
        }

        private String formatDate(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}