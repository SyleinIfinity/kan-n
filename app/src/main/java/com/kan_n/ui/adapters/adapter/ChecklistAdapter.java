package com.kan_n.ui.adapters.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kan_n.R;
import com.kan_n.data.models.CheckItem;
import java.util.ArrayList;
import java.util.List;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ViewHolder> {

    private List<CheckItem> items = new ArrayList<>();
    private final OnCheckItemActionListener listener;

    // [QUAN TRỌNG] Định nghĩa Interface này để Fragment gọi
    public interface OnCheckItemActionListener {
        void onItemChecked(CheckItem item, int position, boolean isChecked);
        void onItemLongClicked(CheckItem item, int position); // Sự kiện nhấn giữ
    }

    // Constructor nhận Interface mới
    public ChecklistAdapter(OnCheckItemActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CheckItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checklist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cb;
        TextView tv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cb = itemView.findViewById(R.id.cb_check_item);
            tv = itemView.findViewById(R.id.tv_check_item_title);
        }

        void bind(CheckItem item, int position) {
            tv.setText(item.getTitle());

            cb.setOnCheckedChangeListener(null);
            cb.setChecked(item.isChecked());

            // Gạch ngang chữ nếu đã hoàn thành
            if (item.isChecked()) {
                tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tv.setTextColor(android.graphics.Color.GRAY);
            } else {
                tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tv.setTextColor(android.graphics.Color.BLACK);
            }

            // Sự kiện Check
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) listener.onItemChecked(item, position, isChecked);
            });

            // Sự kiện Nhấn giữ vào dòng chữ (để sửa tên)
            tv.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onItemLongClicked(item, position);
                }
                return true;
            });
        }
    }
}