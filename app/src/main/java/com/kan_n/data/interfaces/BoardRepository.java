package com.kan_n.data.interfaces;

import android.util.Pair;

import com.kan_n.data.models.Board;
import com.kan_n.data.models.User;
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

    // Thêm callback này
    interface BoardMembersCallback {
        void onSuccess(List<Pair<User, String>> members);
        void onError(String message);
    }

    // Thêm hàm này vào interface BoardRepository
    void getBoardMembers(String boardId, BoardMembersCallback callback);



    // --- PHƯƠNG THỨC LẤY DỮ LIỆU ---

    void getWorkspacesWithBoards(String userId, WorkspacesWithBoardsCallback callback);
    void getBoardDetails(String boardId, BoardCallback callback);

    // --- PHƯƠNG THỨC GHI DỮ LIỆU ---

    /**
     * Tạo một Bảng mới trong một Không gian làm việc.
     * workspaceId ID - của không gian làm việc
     * name - Tên bảng
     * visibility - "private" hoặc "workspace"
     * background - Đối tượng Background chứa type và value (color/image)
     * callback - Callback kết quả
     */
    void createBoard(String workspaceId, String name, String visibility, Background background, GeneralCallback callback);

    void updateBoard(String boardId, Map<String, Object> updates, GeneralCallback callback);
    void addMemberToBoard(String boardId, String userId, GeneralCallback callback);
    void removeMemberFromBoard(String boardId, String userId, GeneralCallback callback);
    void createWorkspace(String name, String description, GeneralCallback callback);
    void updateWorkspace(String workspaceId, String newName, GeneralCallback callback);
    void deleteWorkspace(String workspaceId, GeneralCallback callback);
    void getActiveWorkspaceWithBoards(String userId, String workspaceId, WorkspacesWithBoardsCallback callback);

    void updateBoard(String boardId, String newName, GeneralCallback callback);
    void deleteBoard(String boardId, GeneralCallback callback);


}