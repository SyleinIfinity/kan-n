package com.kan_n.ui.adapters.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kan_n.R;
import com.kan_n.data.models.Board;

// Thêm thư viện Glide để tải ảnh. Bạn cần thêm thư viện này vào build.gradle.kts (app)
// implementation("com.github.bumptech.glide:glide:4.16.0")

import java.util.List;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {

    private List<Board> boardList;
    private Context context;

    public BoardAdapter(Context context, List<Board> boardList) {
        this.context = context;
        this.boardList = boardList;
    }

    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item_board.xml đã thiết kế ở bước trước
        View view = LayoutInflater.from(context).inflate(R.layout.item_board, parent, false);
        return new BoardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardViewHolder holder, int position) {
        Board board = boardList.get(position);

        // 1. Đặt tiêu đề cho Bảng
        // Sử dụng getName() thay vì getTitle()
        holder.tvBoardTitle.setText(board.getName()); //

        // 2. Xử lý logic hiển thị sao (yêu thích)
        // Sử dụng getVisibility() thay vì isStarred()
        // Cần kiểm tra null vì getVisibility() trả về Boolean
        if (board.getVisibility() != null && board.getVisibility()) {
            holder.ivBoardStarred.setImageResource(R.drawable.ic_clicked_star);
        } else {
            holder.ivBoardStarred.setImageResource(R.drawable.ic_unclicked_star);
        }

        // 3. Tải ảnh nền bằng Glide
        // Sử dụng getDescription() vì bạn đang lưu URL ảnh trong đó
        Glide.with(context)
                .load(board.getDescription()) // Lấy URL từ trường description
                .placeholder(R.drawable.ic_launcher_background) // Ảnh chờ
                .error(R.drawable.ic_huy) // Ảnh khi lỗi
                .centerCrop() // Cắt ảnh cho vừa
                .into(holder.ivBoardBackground);

        // 4. (Tùy chọn) Thêm sự kiện click cho mỗi bảng
        holder.itemView.setOnClickListener(v -> {
            // Xử lý khi người dùng nhấn vào một bảng
            // Ví dụ: Chuyển sang một Activity/Fragment chi tiết của bảng
            // Intent intent = new Intent(context, BoardDetailActivity.class);
            // intent.putExtra("BOARD_ID", board.getBoardId());
            // context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return boardList != null ? boardList.size() : 0;
    }

    // Lớp ViewHolder để giữ các tham chiếu đến View
    public static class BoardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBoardBackground;
        TextView tvBoardTitle;
        ImageView ivBoardStarred;

        public BoardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBoardBackground = itemView.findViewById(R.id.iv_board_background);
            tvBoardTitle = itemView.findViewById(R.id.tv_board_title);
            ivBoardStarred = itemView.findViewById(R.id.iv_board_starred);
        }
    }
}