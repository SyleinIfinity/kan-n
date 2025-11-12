package com.kan_n.data.models;

import java.util.List;

public class Workspace {
    private String workspaceId;
    private String workspaceName;
    private List<Board> boards; // Danh sách các bảng trong không gian làm việc này

    // Constructor rỗng cần cho Firebase/Gson
    public Workspace() {
    }

    // Constructor đầy đủ
    public Workspace(String workspaceId, String workspaceName, List<Board> boards) {
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.boards = boards;
    }

    // Getters and Setters
    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public List<Board> getBoards() {
        return boards;
    }

    public void setBoards(List<Board> boards) {
        this.boards = boards;
    }
}