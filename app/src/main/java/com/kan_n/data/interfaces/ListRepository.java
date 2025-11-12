package com.kan_n.data.interfaces;

import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.kan_n.data.models.ListModel; // Bạn cần tạo model ListModel

import java.util.List;

public interface ListRepository {

    /**
     * Lấy tất cả các Danh sách (cột) thuộc về một Bảng.
     * @param boardId ID của bảng
     * @return LiveData<List<ListModel>>
     */
    LiveData<List<ListModel>> getListsForBoard(String boardId);

    /**
     * Tạo một Danh sách (cột) mới trong Bảng.
     * @param boardId ID của bảng
     * @param listModel Đối tượng Danh sách mới
     * @return Task<Void>
     */
    Task<Void> createList(String boardId, ListModel listModel);

    /**
     * Cập nhật một Danh sách (ví dụ: đổi tên).
     * @param listId ID của danh sách
     * @param updates Map chứa các trường cần cập nhật
     * @return Task<Void>
     */
    Task<Void> updateList(String listId, java.util.Map<String, Object> updates);

    /**
     * Xóa một Danh sách (cột).
     * @param listId ID của danh sách
     * @return Task<Void>
     */
    Task<Void> deleteList(String listId);

    /**
     * Cập nhật thứ tự của các Danh sách trong một Bảng (khi kéo-thả).
     * @param boardId ID của bảng
     * @param sortedListIds Danh sách các ID đã được sắp xếp
     * @return Task<Void>
     */
    Task<Void> updateListOrder(String boardId, List<String> sortedListIds);
}