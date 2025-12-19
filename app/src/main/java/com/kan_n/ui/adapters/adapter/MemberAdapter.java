// File: com/kan_n/ui/adapters/adapter/MemberAdapter.java
package com.kan_n.ui.adapters.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.kan_n.R;
import com.kan_n.data.models.User;
import java.util.List;
import android.util.Pair; // Dùng Pair để chứa User + Role

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
    private List<Pair<User, String>> mListMembers;

    // Sửa tên hàm setDate -> setData
    public void setData(List<Pair<User, String>> list) {
        this.mListMembers = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_bang_thanhvien, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Pair<User, String> item = mListMembers.get(position);
        User user = item.first;
        String role = item.second;

        if (user == null) return;

        // 1. Hiển thị Tên
        holder.tvName.setText(user.getUsername()); // Hoặc user.getDisplayName()

        // 2. Hiển thị Quyền (Convert từ tiếng Anh sang tiếng Việt nếu cần)
        if ("owner".equals(role)) {
            holder.tvRole.setText("Quản trị viên");
        } else {
            holder.tvRole.setText("Thành viên");
        }

        // 3. Hiển thị Avatar (SỬA LỖI: dùng getAvatarUrl)
        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_nguoi_dung)
                    .error(R.drawable.ic_nguoi_dung)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_nguoi_dung);
        }
    }

    @Override
    public int getItemCount() {
        return mListMembers != null ? mListMembers.size() : 0;
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvRole;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_member_avatar);
            tvName = itemView.findViewById(R.id.tv_member_name);
            tvRole = itemView.findViewById(R.id.tv_member_role);
        }
    }
}