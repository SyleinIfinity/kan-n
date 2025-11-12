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

public class WorkspaceAdapter extends RecyclerView.Adapter<WorkspaceAdapter.WorkspaceViewHolder> {

    private List<Workspace> workspaceList;
    private Context context;

    public WorkspaceAdapter(Context context, List<Workspace> workspaceList) {
        this.context = context;
        this.workspaceList = workspaceList;
    }

    @NonNull
    @Override
    public WorkspaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item_workspace.xml đã thiết kế
        View view = LayoutInflater.from(context).inflate(R.layout.item_workspace, parent, false);
        return new WorkspaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkspaceViewHolder holder, int position) {
        Workspace workspace = workspaceList.get(position);

        // 1. Đặt tên cho Không gian làm việc
        holder.tvWorkspaceName.setText(workspace.getName());

//        // 2. Chuẩn bị dữ liệu cho RecyclerView con (Board)
//        List<Board> boardsInThisWorkspace = workspace.get;
//
//        // 3. Khởi tạo và thiết lập BoardAdapter
//        BoardAdapter boardAdapter = new BoardAdapter(context, boardsInThisWorkspace);
//
//        // 4. Cấu hình RecyclerView con (rv_boards)
//        // Đặt LayoutManager theo chiều ngang
//        holder.rvBoards.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
//        holder.rvBoards.setHasFixedSize(true);
//        holder.rvBoards.setAdapter(boardAdapter);
    }

    @Override
    public int getItemCount() {
        return workspaceList != null ? workspaceList.size() : 0;
    }

    // Lớp ViewHolder để giữ các tham chiếu đến View của item_workspace
    public static class WorkspaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkspaceName;
        RecyclerView rvBoards; // RecyclerView con

        public WorkspaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkspaceName = itemView.findViewById(R.id.tv_workspace_name);
            rvBoards = itemView.findViewById(R.id.rv_boards);
        }
    }

    // (Tùy chọn) Phương thức để cập nhật dữ liệu cho Adapter
    public void updateData(List<Workspace> newWorkspaceList) {
        this.workspaceList = newWorkspaceList;
        notifyDataSetChanged();
    }
}