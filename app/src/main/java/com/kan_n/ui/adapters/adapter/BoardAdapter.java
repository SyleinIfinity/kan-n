package com.kan_n.ui.adapters.adapter;

import android.content.Context;
import android.graphics.Color; // Cần thiết cho việc xử lý mã màu
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.kan_n.R;
import com.kan_n.data.models.Board;
import com.kan_n.data.models.Background; // <--- SỬ DỤNG MODEL MỚI

import java.util.List;

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
        // Sử dụng layout item_board.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_board, parent, false);
        return new BoardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardViewHolder holder, int position) {
        Board board = boardList.get(position); // Lấy Board

        // 1. Đặt tiêu đề cho Bảng
        holder.tvBoardTitle.setText(board.getName());

        // 2. Xử lý logic hiển thị sao (yêu thích)
        // Nếu bạn dùng Visibility để làm trạng thái yêu thích:
        if (board.getVisibility() != null &&
                (board.getVisibility().equalsIgnoreCase("public") || board.getVisibility().equalsIgnoreCase("workspace"))) {
            holder.ivBoardStarred.setImageResource(R.drawable.ic_clicked_star);
        } else {
            holder.ivBoardStarred.setImageResource(R.drawable.ic_unclicked_star);
        }

        // 3. Xử lý hiển thị Background (Color hoặc Image)
        Background background = board.getBackground();
        // Màu mặc định, có thể là màu xanh từ view_scrim trong item_board.xml
        int defaultColor = Color.parseColor("#4D8DDB");

        if (background != null) {
            String type = background.getType();
            String value = background.getValue();

            if ("color".equalsIgnoreCase(type) && value != null && !value.isEmpty()) {
                // Trường hợp 1: Hiển thị màu nền
                holder.ivBoardBackground.setVisibility(View.GONE); // Ẩn ImageView
                try {
                    int color = Color.parseColor(value); // Parse mã màu
                    holder.viewScrim.setBackgroundColor(color);
                } catch (IllegalArgumentException e) {
                    holder.viewScrim.setBackgroundColor(defaultColor); // Màu mặc định nếu mã màu lỗi
                }

            } else if ("image".equalsIgnoreCase(type) && value != null && !value.isEmpty()) {
                // Trường hợp 2: Hiển thị ảnh nền
                holder.ivBoardBackground.setVisibility(View.VISIBLE);
//                holder.viewScrim.setBackgroundColor(Color.TRANSPARENT); // Đảm bảo scrim trong suốt

                Glide.with(context)
                        .load(value) // Tải URL ảnh
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_huy)
                        .centerCrop()
                        .into(holder.ivBoardBackground);

            } else {
                // Mặc định an toàn
                holder.ivBoardBackground.setVisibility(View.GONE);
                holder.viewScrim.setBackgroundColor(defaultColor);
            }
        } else {
            // Không có background, dùng màu mặc định
            holder.ivBoardBackground.setVisibility(View.GONE);
            holder.viewScrim.setBackgroundColor(defaultColor);
        }

        System.out.println(defaultColor);
    }

    @Override
    public int getItemCount() {
        return boardList != null ? boardList.size() : 0;
    }

    /**
     * PHƯƠNG THỨC CẦN THIẾT: Cập nhật dữ liệu cho Adapter
     * Phương thức này giải quyết lỗi "Cannot resolve method 'updateData'" trong WorkspaceAdapter.
     * @param newBoardList Danh sách Board mới
     */
    public void updateData(List<Board> newBoardList) {
        this.boardList = newBoardList;
        notifyDataSetChanged();
    }


    // Lớp ViewHolder để giữ các tham chiếu đến View
    public static class BoardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBoardBackground; // ImageView cho ảnh nền (có bo góc)
        TextView tvBoardTitle;
        ImageView ivBoardStarred;
        View viewScrim; // View cho lớp phủ/màu nền

        public BoardViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID từ item_board.xml
            ivBoardBackground = itemView.findViewById(R.id.iv_board_background);
            tvBoardTitle = itemView.findViewById(R.id.tv_board_title);
            ivBoardStarred = itemView.findViewById(R.id.iv_board_starred);
            viewScrim = itemView.findViewById(R.id.view_scrim);
        }
    }
}