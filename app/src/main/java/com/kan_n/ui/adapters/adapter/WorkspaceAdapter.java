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

import java.util.ArrayList; // <-- Them import
import java.util.List;
import java.util.Objects;

public class WorkspaceAdapter extends RecyclerView.Adapter<WorkspaceAdapter.WorkspaceViewHolder> {

    private List<Workspace> workspaceList;
    private final Context context;
    private final BoardAdapter.OnBoardClickListener boardClickListener;

    // Constructor
    public WorkspaceAdapter(Context context, List<Workspace> workspaceList, BoardAdapter.OnBoardClickListener boardClickListener) {
        this.context = context;
        this.workspaceList = workspaceList;
        this.boardClickListener = boardClickListener;
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

        holder.tvWorkspaceName.setText(workspace.getName());
        List<Board> boardsInThisWorkspace = workspace.getBoards();

        // Cap nhat du lieu cho adapter con (da duoc khoi tao trong ViewHolder)
        holder.updateBoards(boardsInThisWorkspace);
    }

    @Override
    public int getItemCount() {
        return workspaceList != null ? workspaceList.size() : 0;
    }

    public void updateData(List<Workspace> newWorkspaceList) {
        this.workspaceList = newWorkspaceList;
        notifyDataSetChanged();
    }

    public static class WorkspaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkspaceName;
        RecyclerView rvBoards;
        BoardAdapter boardAdapter;
        Context context;

        public WorkspaceViewHolder(@NonNull View itemView, Context context, BoardAdapter.OnBoardClickListener boardClickListener) {
            super(itemView);
            this.context = context;
            tvWorkspaceName = itemView.findViewById(R.id.tv_workspace_name);
            rvBoards = itemView.findViewById(R.id.rv_boards);

            boardAdapter = new BoardAdapter(context, new ArrayList<>(), boardClickListener);
            rvBoards.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            rvBoards.setAdapter(boardAdapter);
        }


        public void updateBoards(List<Board> boards) {
            if (boardAdapter != null) {
                boardAdapter.updateData(boards);
            }
        }
    }
}