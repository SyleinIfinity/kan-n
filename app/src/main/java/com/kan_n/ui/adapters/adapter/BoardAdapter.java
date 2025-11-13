package com.kan_n.ui.adapters.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
// import com.google.android.material.imageview.ShapeableImageView; // Khong can neu da cast
import com.kan_n.R;
import com.kan_n.data.models.Board;
import com.kan_n.data.models.Background;

import java.util.List;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {

    private List<Board> boardList;
    private final Context context;
    private final OnBoardClickListener boardClickListener; // ✨ 1. Them bien listener

    // ✨ 2. Dinh nghia Interface
    public interface OnBoardClickListener {
        void onBoardClick(Board board);
    }

    // ✨ 3. Cap nhat constructor
    public BoardAdapter(Context context, List<Board> boardList, OnBoardClickListener listener) {
        this.context = context;
        this.boardList = boardList;
        this.boardClickListener = listener; // Gan listener
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
        if (board == null) return;

        // 1. Đặt tiêu đề cho Bảng
        holder.tvBoardTitle.setText(board.getName());

        // 2. Xử lý logic hiển thị sao (yêu thích)
        // (Logic cua ban)
        if (board.getVisibility() != null &&
                (board.getVisibility().equalsIgnoreCase("public") || board.getVisibility().equalsIgnoreCase("workspace"))) {
            holder.ivBoardStarred.setImageResource(R.drawable.ic_clicked_star);
        } else {
            holder.ivBoardStarred.setImageResource(R.drawable.ic_unclicked_star);
        }

        // 3. Xử lý hiển thị Background
        Background background = board.getBackground();
        int defaultColor = Color.parseColor("#4D8DDB");

        if (background != null) {
            String type = background.getType();
            String value = background.getValue();

            if ("color".equalsIgnoreCase(type) && value != null && !value.isEmpty()) {
                holder.ivBoardBackground.setVisibility(View.GONE);
                try {
                    holder.viewScrim.setBackgroundColor(Color.parseColor(value));
                } catch (IllegalArgumentException e) {
                    holder.viewScrim.setBackgroundColor(defaultColor);
                }
            } else if ("image".equalsIgnoreCase(type) && value != null && !value.isEmpty()) {
                holder.ivBoardBackground.setVisibility(View.VISIBLE);
                // holder.viewScrim.setBackgroundColor(Color.TRANSPARENT); // Dam bao scrim trong suot
                Glide.with(context)
                        .load(value)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_huy)
                        .centerCrop()
                        .into(holder.ivBoardBackground);
            } else {
                holder.ivBoardBackground.setVisibility(View.GONE);
                holder.viewScrim.setBackgroundColor(defaultColor);
            }
        } else {
            holder.ivBoardBackground.setVisibility(View.GONE);
            holder.viewScrim.setBackgroundColor(defaultColor);
        }

        // ✨ 4. Gan su kien click vao toan bo item
        holder.itemView.setOnClickListener(v -> {
            if (boardClickListener != null) {
                boardClickListener.onBoardClick(board);
            }
        });
    }

    @Override
    public int getItemCount() {
        return boardList != null ? boardList.size() : 0;
    }

    public void updateData(List<Board> newBoardList) {
        this.boardList = newBoardList;
        notifyDataSetChanged();
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
}