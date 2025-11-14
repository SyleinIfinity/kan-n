package com.kan_n.data.interfaces;

import com.kan_n.data.models.Board;
import com.kan_n.data.models.Workspace;
import com.kan_n.data.models.Background; // <--- Import Background

import java.util.List;
import java.util.Map;

public interface BoardRepository {

    // --- Định nghĩa Callbacks ---
    interface GeneralCallback {
        void onSuccess();
        void onError(String message);
    }
    interface BoardCallback {
        void onSuccess(Board board);
        void onError(String message);
    }
    interface WorkspacesWithBoardsCallback {
        void onSuccess(List<Workspace> workspaces);
        void onError(String message);
    }

    // --- PHƯƠNG THỨC LẤY DỮ LIỆU ---

    void getWorkspacesWithBoards(String userId, WorkspacesWithBoardsCallback callback);
    void getBoardDetails(String boardId, BoardCallback callback);

    // --- PHƯƠNG THỨC GHI DỮ LIỆU ---

    /**
     * Tạo một Bảng mới trong một Không gian làm việc.
     * @param workspaceId ID của không gian làm việc
     * @param name Tên bảng
     * @param visibility "private" hoặc "workspace"
     * @param background Đối tượng Background chứa type và value (color/image) <--- CẬP NHẬT
     * @param callback Callback kết quả
     */
    void createBoard(String workspaceId, String name, String visibility, Background background, GeneralCallback callback); // <--- CẬP NHẬT

    void updateBoard(String boardId, Map<String, Object> updates, GeneralCallback callback);
    void deleteBoard(String boardId, GeneralCallback callback);
    void addMemberToBoard(String boardId, String userId, GeneralCallback callback);
    void removeMemberFromBoard(String boardId, String userId, GeneralCallback callback);
}