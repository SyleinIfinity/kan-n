package com.kan_n.data.interfaces;

import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.kan_n.data.models.Tag; // Bạn cần tạo model Tag

import java.util.List;

public interface TagRepository {

    /**
     * Lấy tất cả các Nhãn (tag) có sẵn trong một Bảng.
     * @param boardId ID của bảng
     * @return LiveData<List<Tag>>
     */
    LiveData<List<Tag>> getTagsForBoard(String boardId);

    /**
     * Tạo một Nhãn mới cho Bảng.
     * @param boardId ID của bảng
     * @param tag Đối tượng Nhãn mới (ví dụ: tên, màu sắc)
     * @return Task<Void>
     */
    Task<Void> createTagForBoard(String boardId, Tag tag);

    /**
     * Cập nhật một Nhãn (ví dụ: đổi màu, đổi tên).
     * @param tagId ID của nhãn
     * @param updates Map chứa các trường cần cập nhật
     * @return Task<Void>
     */
    Task<Void> updateTag(String tagId, java.util.Map<String, Object> updates);

    /**
     * Xóa một Nhãn khỏi Bảng.
     * @param tagId ID của nhãn
     * @return Task<Void>
     */
    Task<Void> deleteTag(String tagId);

    /**
     * Gắn một Nhãn vào một Thẻ.
     * @param cardId ID của thẻ
     * @param tagId ID của nhãn
     * @return Task<Void>
     */
    Task<Void> addTagToCard(String cardId, String tagId);

    /**
     * Gỡ một Nhãn khỏi một Thẻ.
     * @param cardId ID của thẻ
     * @param tagId ID của nhãn
     * @return Task<Void>
     */
    Task<Void> removeTagFromCard(String cardId, String tagId);
}