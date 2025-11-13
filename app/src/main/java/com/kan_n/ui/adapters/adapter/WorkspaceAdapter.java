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
        View view = LayoutInflater.from(context).inflate(R.layout.item_workspace, parent, false);
        return new WorkspaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkspaceViewHolder holder, int position) {
        Workspace workspace = workspaceList.get(position);

        // 1. Đặt tên cho Không gian làm việc
        holder.tvWorkspaceName.setText(workspace.getName());

        // 2. Chuẩn bị dữ liệu cho RecyclerView con (Board)
        List<Board> boardsInThisWorkspace = workspace.getBoards();

        // 3. Quản lý RecyclerView con: Cài đặt Adapter nếu chưa có, hoặc chỉ cập nhật dữ liệu.
        if (holder.rvBoards.getAdapter() == null) {
            BoardAdapter boardAdapter = new BoardAdapter(context, boardsInThisWorkspace);

            // Cấu hình LayoutManager (Vertical)
            holder.rvBoards.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            holder.rvBoards.setAdapter(boardAdapter);
        } else {
            // Nếu Adapter đã tồn tại, chỉ cần cập nhật dữ liệu
            ((BoardAdapter) Objects.requireNonNull(holder.rvBoards.getAdapter())).updateData(boardsInThisWorkspace);
        }
    }

    @Override
    public int getItemCount() {
        return workspaceList != null ? workspaceList.size() : 0;
    }

    public static class WorkspaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkspaceName;
        RecyclerView rvBoards;

        public WorkspaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkspaceName = itemView.findViewById(R.id.tv_workspace_name);
            rvBoards = itemView.findViewById(R.id.rv_boards);
        }
    }

    public void updateData(List<Workspace> newWorkspaceList) {
        this.workspaceList = newWorkspaceList;
        notifyDataSetChanged();
    }
}