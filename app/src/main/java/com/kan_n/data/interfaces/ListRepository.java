// Đặt tại: app/src/main/java/com/kan_n/data/interfaces/ListRepository.java

package com.kan_n.data.interfaces;

import com.google.firebase.database.ChildEventListener;
import com.kan_n.data.models.ListModel;
import java.util.List;

public interface ListRepository {

    // --- Callbacks ---
    public interface GeneralCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface ListsCallback {
        void onSuccess(List<ListModel> lists);
        void onError(String message);
    }

    // --- Methods ---

    /**
     * Tạo một danh sách (cột) mới trong bảng.
     */
    void createList(String boardId, String title, double position, GeneralCallback callback);

    /**
     * Lắng nghe (real-time) các danh sách (cột) của một bảng.
     * Dùng ChildEventListener để RecyclerView tự động cập nhật khi có thêm/sửa/xóa.
     */
    void getListsForBoard(String boardId, ChildEventListener listener);

    /**
     * Cập nhật tiêu đề của một danh sách (cột).
     */
    void updateListTitle(String listId, String newTitle, GeneralCallback callback);

    /**
     * Xóa một danh sách (cột) VÀ tất cả các thẻ (cards) bên trong nó.
     */
    void deleteList(String listId, GeneralCallback callback);
}