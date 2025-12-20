package com.kan_n.ui.adapters.adapter;

import android.content.Context;
import android.graphics.Color;
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

import java.util.ArrayList; // <-- Them import
import java.util.List;
import java.util.Objects;

public class WorkspaceAdapter extends RecyclerView.Adapter<WorkspaceAdapter.WorkspaceViewHolder> {

    private List<Workspace> workspaceList;
    private final Context context;
    private BoardAdapter.OnBoardClickListener boardClickListener;

    // THÊM CÁC BIẾN MỚI
    private boolean isManageMode = false;
    private String activeWorkspaceId = "";
    private OnWorkspaceActionListener manageListener;

    // Interface cho chế độ quản lý
    public interface OnWorkspaceActionListener {
        void onSelect(Workspace ws);
        void onEdit(Workspace ws);
        void onDelete(Workspace ws);
    }

    // Constructor cũ giữ nguyên để không làm hỏng BangFragment
    public WorkspaceAdapter(Context context, List<Workspace> workspaceList, BoardAdapter.OnBoardClickListener boardClickListener) {
        this.context = context;
        this.workspaceList = workspaceList;
        this.boardClickListener = boardClickListener;
    }

    // THÊM Constructor mới cho chế độ quản lý
    public WorkspaceAdapter(Context context, List<Workspace> workspaceList, String activeWorkspaceId, OnWorkspaceActionListener manageListener) {
        this.context = context;
        this.workspaceList = workspaceList;
        this.activeWorkspaceId = activeWorkspaceId;
        this.manageListener = manageListener;
        this.isManageMode = true;
    }

    @NonNull
    @Override
    public WorkspaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Kiểm tra chế độ để inflate layout tương ứng
        int layoutId = isManageMode ? R.layout.item_workspace_manage : R.layout.item_workspace;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new WorkspaceViewHolder(view, context, boardClickListener, isManageMode);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkspaceViewHolder holder, int position) {
        Workspace workspace = workspaceList.get(position);
        if (workspace == null) return;

        holder.tvWorkspaceName.setText(workspace.getName());

        if (isManageMode) {
            // Chế độ quản lý: Xử lý trạng thái Active và các nút Sửa/Xóa
            boolean isActive = workspace.getUid().equals(activeWorkspaceId);
            if (holder.ivActiveStatus != null) {
                holder.ivActiveStatus.setVisibility(isActive ? View.VISIBLE : View.INVISIBLE);
            }

            holder.itemView.setOnClickListener(v -> manageListener.onSelect(workspace));
            if (holder.btnEdit != null) holder.btnEdit.setOnClickListener(v -> manageListener.onEdit(workspace));
            if (holder.btnDelete != null) holder.btnDelete.setOnClickListener(v -> manageListener.onDelete(workspace));

        } else {
            // Chế độ hiển thị: Giữ nguyên logic cũ cho Board list
            List<Board> boardsInThisWorkspace = workspace.getBoards();
            holder.updateBoards(boardsInThisWorkspace);
        }
    }

    @Override
    public int getItemCount() {
        return workspaceList != null ? workspaceList.size() : 0;
    }

    public void updateData(List<Workspace> newWorkspaceList) {
        this.workspaceList = newWorkspaceList;
        notifyDataSetChanged();
    }

    // Cập nhật WorkspaceID đang hoạt động
    public void setActiveWorkspaceId(String id) {
        this.activeWorkspaceId = id;
        notifyDataSetChanged();
    }

    public static class WorkspaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkspaceName;
        // Các view cũ
        RecyclerView rvBoards;
        BoardAdapter boardAdapter;
        // Các view mới cho chế độ quản lý
        View ivActiveStatus, btnEdit, btnDelete;

        public WorkspaceViewHolder(@NonNull View itemView, Context context, BoardAdapter.OnBoardClickListener boardClickListener, boolean isManageMode) {
            super(itemView);
            tvWorkspaceName = itemView.findViewById(R.id.tv_workspace_name); // ID ở item_workspace
            if (tvWorkspaceName == null) tvWorkspaceName = itemView.findViewById(R.id.tv_ws_name); // ID ở item_workspace_manage

            if (isManageMode) {
                ivActiveStatus = itemView.findViewById(R.id.iv_active_status);
                btnEdit = itemView.findViewById(R.id.btn_edit_ws);
                btnDelete = itemView.findViewById(R.id.btn_delete_ws);
            } else {
                rvBoards = itemView.findViewById(R.id.rv_boards);
                boardAdapter = new BoardAdapter(context, new ArrayList<>(), boardClickListener);
                rvBoards.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                rvBoards.setAdapter(boardAdapter);
            }
        }

        public void updateBoards(List<Board> boards) {
            if (boardAdapter != null) {
                boardAdapter.updateData(boards);
            }
        }
    }
}