package com.kan_n.ui.adapters.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kan_n.R;
import com.kan_n.data.models.Board;

import java.util.List;
import java.util.Map;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {

    private List<Board> boardList;
    private final Context context;

    public BoardAdapter(Context context, List<Board> boardList) {
        this.context = context;
        this.boardList = boardList;
    }

    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_board, parent, false);
        return new BoardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardViewHolder holder, int position) {
        Board board = boardList.get(position);

        // 1. Đặt tiêu đề cho Bảng
        holder.tvBoardTitle.setText(board.getName());

        // 2. Xử lý biểu tượng truy cập (Visibility)
        String visibility = board.getVisibility();
        if ("private".equalsIgnoreCase(visibility)) {
            holder.ivBoardStarred.setImageResource(R.drawable.ic_riengtu);
        } else if ("workspace".equalsIgnoreCase(visibility) || "public".equalsIgnoreCase(visibility)) {
            holder.ivBoardStarred.setImageResource(R.drawable.ic_congkhai);
        } else {
            holder.ivBoardStarred.setImageDrawable(null);
        }

        // 3. Xử lý Ảnh/Màu nền (background)
        Map<String, String> background = board.getBackground();

        // Đặt mặc định về trạng thái ẩn
        holder.ivBoardBackground.setVisibility(View.GONE);
        holder.viewScrim.setVisibility(View.VISIBLE);

        if (background != null && background.containsKey("type")) {
            String type = background.get("type");
            String value = background.get("value");

            if ("color".equals(type) && value != null) {
                // Nếu là màu sắc
                holder.ivBoardBackground.setImageDrawable(null);
                try {
                    holder.viewScrim.setBackgroundColor(Color.parseColor(value));
                } catch (IllegalArgumentException e) {
                    holder.viewScrim.setBackgroundColor(Color.parseColor("#4D8DDB"));
                }
                holder.viewScrim.setVisibility(View.VISIBLE);

            } else if ("image".equals(type) && value != null) {
                // Nếu là URL ảnh
                holder.viewScrim.setBackgroundColor(Color.TRANSPARENT);
                holder.ivBoardBackground.setVisibility(View.VISIBLE);

                // Tải ảnh bằng Glide (YÊU CẦU THƯ VIỆN)
                Glide.with(context)
                        .load(value)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_huy)
                        .centerCrop()
                        .into(holder.ivBoardBackground);
            }
        }

        // 4. Sự kiện click
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Mở bảng: " + board.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Triển khai điều hướng tới màn hình chi tiết Board
        });
    }

    @Override
    public int getItemCount() {
        return boardList != null ? boardList.size() : 0;
    }

    public static class BoardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBoardBackground;
        TextView tvBoardTitle;
        ImageView ivBoardStarred;
        View viewScrim;

        public BoardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBoardBackground = itemView.findViewById(R.id.iv_board_background);
            tvBoardTitle = itemView.findViewById(R.id.tv_board_title);
            ivBoardStarred = itemView.findViewById(R.id.iv_board_starred);
            viewScrim = itemView.findViewById(R.id.view_scrim);
        }
    }

    public void updateData(List<Board> newBoardList) {
        this.boardList = newBoardList;
        notifyDataSetChanged();
    }
}