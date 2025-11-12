package com.kan_n.data.interfaces;

import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.kan_n.data.models.Board;
import com.kan_n.data.models.Workspace;

import java.util.List;

public interface BoardRepository {

    /**
     * Lấy danh sách tất cả các Không gian làm việc (Workspace) CÙNG VỚI các Bảng (Board)
     * lồng bên trong chúng, dành cho người dùng hiện tại.
     * Đây là phương thức quan trọng cho BangViewModel.
     *
     * @param userId ID của người dùng
     * @return LiveData<List<Workspace>>
     */
    LiveData<List<Workspace>> getWorkspacesWithBoards(String userId);

    /**
     * Lấy chi tiết của một Bảng cụ thể.
     * @param boardId ID của bảng
     * @return LiveData<Board>
     */
    LiveData<Board> getBoardDetails(String boardId);

    /**
     * Tạo một Bảng mới trong một Không gian làm việc.
     * @param workspaceId ID của không gian làm việc
     * @param board Đối tượng Bảng mới
     * @return Task<Void>
     */
    Task<Void> createBoard(String workspaceId, Board board);

    /**
     * Cập nhật thông tin một Bảng (ví dụ: đổi tên, đổi ảnh nền).
     * @param boardId ID của bảng
     * @param updates Map chứa các trường cần cập nhật
     * @return Task<Void>
     */
    Task<Void> updateBoard(String boardId, java.util.Map<String, Object> updates);

    /**
     * Xóa một Bảng.
     * @param boardId ID của bảng
     * @return Task<Void>
     */
    Task<Void> deleteBoard(String boardId);

    /**
     * Thêm một thành viên vào Bảng.
     * @param boardId ID của bảng
     * @param userId ID người dùng được thêm
     * @return Task<Void>
     */
    Task<Void> addMemberToBoard(String boardId, String userId);

    /**
     * Xóa một thành viên khỏi Bảng.
     * @param boardId ID của bảng
     * @param userId ID người dùng bị xóa
     * @return Task<Void>
     */
    Task<Void> removeMemberFromBoard(String boardId, String userId);
}