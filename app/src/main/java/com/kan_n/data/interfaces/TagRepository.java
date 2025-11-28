// Đặt tại: app/src/main/java/com/kan_n/data/interfaces/TagRepository.java

package com.kan_n.data.interfaces;

import com.kan_n.data.models.Tag;
import java.util.List;
import java.util.Map;

public interface TagRepository {

    // === ĐỊNH NGHĨA CALLBACKS ===

    public interface GeneralCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface TagsCallback {
        void onSuccess(List<Tag> tags);
        void onError(String message);
    }

    // === ĐỊNH NGHĨA HÀNH ĐỘNG ===

    /**
     * Lấy tất cả các Nhãn (tag) mà người dùng hiện tại đã tạo.
     */
    void getTagsForUser(String userId, TagsCallback callback);

    /**
     * Tạo một Nhãn mới.
     * (Model Tag đã chứa createdBy).
     */
    void createTag(Tag tag, GeneralCallback callback);

    /**
     * Cập nhật một Nhãn
     */
    void updateTag(String tagId, Map<String, Object> updates, GeneralCallback callback);

    /**
     * Xóa một Nhãn VÀ gỡ nó khỏi tất cả các Thẻ.
     */
    void deleteTag(String tagId, GeneralCallback callback);

    /**
     * Gắn một Nhãn vào một Thẻ (Atomic update).
     */
    void addTagToCard(String cardId, String tagId, GeneralCallback callback);

    /**
     * Gỡ một Nhãn khỏi một Thẻ (Atomic update).
     */
    void removeTagFromCard(String cardId, String tagId, GeneralCallback callback);
}