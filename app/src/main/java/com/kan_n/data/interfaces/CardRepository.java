// Đặt tại: app/src/main/java/com/kan_n/data/interfaces/CardRepository.java

package com.kan_n.data.interfaces;

import com.google.firebase.database.ChildEventListener;
import com.kan_n.data.models.Card;
import java.util.Map;

public interface CardRepository {

    // Callback chung
    public interface GeneralCallback {
        void onSuccess();
        void onError(String message);
    }

    // Callback trả về 1 Card (ví dụ khi tạo mới)
    public interface CardCallback {
        void onSuccess(Card card);
        void onError(String message);
    }

    /**
     * Tạo một thẻ mới trong một danh sách.
     */
    void createCard(String listId, String title, double position, GeneralCallback callback);

    /**
     * Lắng nghe (real-time) tất cả các thẻ trong một danh sách cụ thể.
     * Đây là phương thức quan trọng cho RecyclerView lồng bên trong item_ListModel.
     *
     * @param listId ID của danh sách (ListModel)
     * @param listener Một ChildEventListener sẽ được kích hoạt khi có thẻ
     * thêm/sửa/xóa/di chuyển.
     */
    void getCardsForList(String listId, ChildEventListener listener);

    /**
     * Cập nhật trạng thái 'isCompleted' của một thẻ (khi nhấn ic_tron_v1).
     */
    void setCardCompleted(String cardId, boolean isCompleted, GeneralCallback callback);

    /**
     * Cập nhật một trường bất kỳ của thẻ (ví dụ: coverImageUrl).
     */
    void updateCardField(String cardId, String fieldName, Object value, GeneralCallback callback);

    /**
     * Cập nhật nhiều trường của thẻ.
     */
    void updateCard(String cardId, Map<String, Object> updates, GeneralCallback callback);

    /**
     * Xóa một thẻ.
     */
    void deleteCard(String cardId, GeneralCallback callback);
}