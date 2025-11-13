// Đặt tại: app/src/main/java/com/kan_n/data/interfaces/BoardRepository.java

package com.kan_n.data.interfaces;

import com.kan_n.data.models.Board;
import com.kan_n.data.models.Workspace; // Cần import model

import java.util.List;
import java.util.Map;

public interface BoardRepository {

    // === ĐỊNH NGHĨA CÁC CALLBACK ===

    /**
     * Callback chung cho các hành động không cần trả về dữ liệu (Tạo, Sửa, Xóa).
     */
    public interface GeneralCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Callback trả về danh sách Workspace (đã chứa các Board).
     */
    public interface WorkspacesWithBoardsCallback {
        void onSuccess(List<Workspace> workspaces);
        void onError(String message);
    }

    /**
     * Callback trả về chi tiết một Board.
     */
    public interface BoardCallback {
        void onSuccess(Board board);
        void onError(String message);
    }


    // === ĐỊNH NGHĨA CÁC HÀNH ĐỘNG (Đã chuẩn hóa) ===

    /**
     * Lấy danh sách Workspace và các Board lồng bên trong.
     * Đã chuyển từ LiveData sang Callback.
     */
    void getWorkspacesWithBoards(String userId, WorkspacesWithBoardsCallback callback);

    /**
     * Lấy chi tiết của một Bảng cụ thể.
     * Đã chuyển từ LiveData sang Callback.
     */
    void getBoardDetails(String boardId, BoardCallback callback);

    /**
     * Tạo một bảng mới (Giữ nguyên, đã chuẩn).
     */
    void createBoard(String workspaceId, String name, String visibility, Map<String, String> background, GeneralCallback callback);

    /**
     * Cập nhật thông tin một Bảng.
     * Đã chuyển từ Task sang Callback.
     */
    void updateBoard(String boardId, Map<String, Object> updates, GeneralCallback callback);

    /**
     * Xóa một Bảng.
     * Đã chuyển từ Task sang Callback.
     */
    void deleteBoard(String boardId, GeneralCallback callback);

    /**
     * Thêm một thành viên vào Bảng.
     * Đã chuyển từ Task sang Callback.
     */
    void addMemberToBoard(String boardId, String userId, GeneralCallback callback);

    /**
     * Xóa một thành viên khỏi Bảng.
     * Đã chuyển từ Task sang Callback.
     */
    void removeMemberFromBoard(String boardId, String userId, GeneralCallback callback);
}