package com.kan_n.ui.adapters.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kan_n.R;
import com.kan_n.data.models.Board;
import com.kan_n.data.models.Workspace;

import java.util.List;
import java.util.Objects;

public class WorkspaceAdapter extends RecyclerView.Adapter<WorkspaceAdapter.WorkspaceViewHolder> {

    private List<Workspace> workspaceList;
    private final Context context;

    public WorkspaceAdapter(Context context, List<Workspace> workspaceList) {
        this.context = context;
        this.workspaceList = workspaceList;
    }

    @NonNull
    @Override
    public WorkspaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tải layout cho mỗi Workspace
        View view = LayoutInflater.from(context).inflate(R.layout.item_workspace, parent, false);
        return new WorkspaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkspaceViewHolder holder, int position) {
        Workspace workspace = workspaceList.get(position);

        // 1. Đặt tên cho Không gian làm việc
        // SỬA: Sử dụng getName() theo định nghĩa trong mô hình Workspace.java
        holder.tvWorkspaceName.setText(workspace.getName());

        // 2. Chuẩn bị dữ liệu cho RecyclerView con (Board)
        List<Board> boardsInThisWorkspace = workspace.getBoards();

        // 3. Quản lý RecyclerView con: Tái sử dụng/Cập nhật BoardAdapter
        if (holder.rvBoards.getAdapter() == null) {
            // Lần đầu tiên: Khởi tạo BoardAdapter
            BoardAdapter boardAdapter = new BoardAdapter(context, boardsInThisWorkspace);
            holder.rvBoards.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            holder.rvBoards.setAdapter(boardAdapter);
        } else {
            ((BoardAdapter) Objects.requireNonNull(holder.rvBoards.getAdapter())).updateData(boardsInThisWorkspace);
        }
    }

    @Override
    public int getItemCount() {
        return workspaceList != null ? workspaceList.size() : 0;
    }

    /**
     * Phương thức cập nhật dữ liệu cho WorkspaceAdapter cha.
     * @param newWorkspaceList Danh sách Workspaces mới
     */
    public void updateData(List<Workspace> newWorkspaceList) {
        this.workspaceList = newWorkspaceList;
        notifyDataSetChanged();
    }

    // Lớp ViewHolder cho item_workspace.xml
    public static class WorkspaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkspaceName;
        RecyclerView rvBoards;

        public WorkspaceViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các View trong item_workspace.xml
            tvWorkspaceName = itemView.findViewById(R.id.tv_workspace_name);
            rvBoards = itemView.findViewById(R.id.rv_boards);
        }
    }
}