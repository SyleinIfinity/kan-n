package com.kan_n.data.interfaces;

import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.kan_n.data.models.Card; // Bạn cần tạo model Card

import java.util.List;

public interface CardRepository {

    /**
     * Lấy tất cả các Thẻ (card) thuộc về một Danh sách (cột).
     * @param listId ID của danh sách
     * @return LiveData<List<Card>>
     */
    LiveData<List<Card>> getCardsForList(String listId);

    /**
     * Lấy chi tiết một Thẻ cụ thể.
     * @param cardId ID của thẻ
     * @return LiveData<Card>
     */
    LiveData<Card> getCardDetails(String cardId);

    /**
     * Tạo một Thẻ mới trong một Danh sách.
     * @param listId ID của danh sách
     * @param card Đối tượng Thẻ mới
     * @return Task<Void>
     */
    Task<Void> createCard(String listId, Card card);

    /**
     * Cập nhật chi tiết một Thẻ (ví dụ: mô tả, ngày hết hạn).
     * @param cardId ID của thẻ
     * @param updates Map chứa các trường cần cập nhật
     * @return Task<Void>
     */
    Task<Void> updateCard(String cardId, java.util.Map<String, Object> updates);

    /**
     * Xóa một Thẻ.
     * @param cardId ID của thẻ
     * @return Task<Void>
     */
    Task<Void> deleteCard(String cardId);

    /**
     * Di chuyển một Thẻ sang Danh sách (cột) khác.
     * @param cardId ID của thẻ
     * @param newListId ID của danh sách mới
     * @return Task<Void>
     */
    Task<Void> moveCardToList(String cardId, String newListId);

    /**
     * Cập nhật thứ tự các Thẻ trong một Danh sách (khi kéo-thả).
     * @param listId ID của danh sách
     * @param sortedCardIds Danh sách các ID thẻ đã được sắp xếp
     * @return Task<Void>
     */
    Task<Void> updateCardOrder(String listId, List<String> sortedCardIds);

    /**
     * Gán một thành viên vào Thẻ.
     * @param cardId ID của thẻ
     * @param userId ID của người dùng
     * @return Task<Void>
     */
    Task<Void> addMemberToCard(String cardId, String userId);

    /**
     * Xóa một thành viên khỏi Thẻ.
     * @param cardId ID của thẻ
     * @param userId ID của người dùng
     * @return Task<Void>
     */
    Task<Void> removeMemberFromCard(String cardId, String userId);
}