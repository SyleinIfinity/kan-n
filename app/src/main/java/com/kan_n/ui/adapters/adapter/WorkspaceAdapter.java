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
    private final BoardAdapter.OnBoardClickListener boardClickListener;
    private OnWorkspaceClickListener workspaceListener; // Listener mới
    private String activeWorkspaceId; // ID không gian đang mở

    public interface OnWorkspaceClickListener {
        void onWorkspaceClick(Workspace workspace);
        void onWorkspaceOptions(Workspace workspace, View view);
    }

    public WorkspaceAdapter(Context context, List<Workspace> workspaceList,
                            BoardAdapter.OnBoardClickListener boardClickListener,
                            OnWorkspaceClickListener workspaceListener) {
        this.context = context;
        this.workspaceList = workspaceList;
        this.boardClickListener = boardClickListener;
        this.workspaceListener = workspaceListener;
    }

    public void setActiveWorkspaceId(String id) {
        this.activeWorkspaceId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkspaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_workspace, parent, false);
        return new WorkspaceViewHolder(view, context, boardClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkspaceViewHolder holder, int position) {
        Workspace workspace = workspaceList.get(position);
        if (workspace == null) return;

        // Hiển thị trạng thái "Đang mở"
        String displayName = workspace.getName();
        if (workspace.getUid() != null && workspace.getUid().equals(activeWorkspaceId)) {
            displayName += " (Đang mở)";
            holder.tvWorkspaceName.setTextColor(Color.BLUE);
        } else {
            holder.tvWorkspaceName.setTextColor(Color.BLACK);
        }

        holder.tvWorkspaceName.setText(displayName);

        // Click để đổi không gian
        holder.tvWorkspaceName.setOnClickListener(v -> workspaceListener.onWorkspaceClick(workspace));

        // Nhấn giữ hoặc nhấn nút tùy chọn (nếu có) để Sửa/Xóa
        holder.tvWorkspaceName.setOnLongClickListener(v -> {
            workspaceListener.onWorkspaceOptions(workspace, v);
            return true;
        });

        holder.updateBoards(workspace.getBoards());
    }

    @Override
    public int getItemCount() { return workspaceList != null ? workspaceList.size() : 0; }

    public void updateData(List<Workspace> newWorkspaceList) {
        this.workspaceList = newWorkspaceList;
        notifyDataSetChanged();
    }

    public static class WorkspaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkspaceName;
        RecyclerView rvBoards;
        BoardAdapter boardAdapter;

        public WorkspaceViewHolder(@NonNull View itemView, Context context, BoardAdapter.OnBoardClickListener boardClickListener) {
            super(itemView);
            tvWorkspaceName = itemView.findViewById(R.id.tv_workspace_name);
            rvBoards = itemView.findViewById(R.id.rv_boards);
            boardAdapter = new BoardAdapter(context, new ArrayList<>(), boardClickListener);
            rvBoards.setLayoutManager(new LinearLayoutManager(context));
            rvBoards.setAdapter(boardAdapter);
        }

        public void updateBoards(List<Board> boards) {
            if (boardAdapter != null) boardAdapter.updateData(boards);
        }
    }
}