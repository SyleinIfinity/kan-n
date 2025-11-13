// Đặt tại: app/src/main/java/com/kan_n/data/repository/CardRepositoryImpl.java

package com.kan_n.data.repository;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.kan_n.data.interfaces.CardRepository;
import com.kan_n.data.models.Card;
import com.kan_n.utils.FirebaseUtils;

import java.util.Map;

public class CardRepositoryImpl implements CardRepository {

    private final DatabaseReference mRootRef;
    private final DatabaseReference mCardsRef;
    // Chúng ta không cần tham chiếu tagCards ở đây,
    // vì việc đó nên được xử lý trong TagRepository

    public CardRepositoryImpl() {
        this.mRootRef = FirebaseUtils.getRootRef();
        this.mCardsRef = mRootRef.child("cards");
    }

    @Override
    public void createCard(String listId, String title, double position, GeneralCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        String cardId = mCardsRef.push().getKey();
        if (cardId == null) {
            callback.onError("Không thể tạo ID cho thẻ mới.");
            return;
        }

        Card newCard = new Card(listId, title, position, currentUserId);

        mCardsRef.child(cardId).setValue(newCard.toMap()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    @Override
    public void getCardsForList(String listId, ChildEventListener listener) {
        // Lắng nghe real-time các thẻ thuộc listId này
        mCardsRef.orderByChild("listId").equalTo(listId).addChildEventListener(listener);
    }

    @Override
    public void setCardCompleted(String cardId, boolean isCompleted, GeneralCallback callback) {
        // Chỉ cập nhật một trường duy nhất
        mCardsRef.child(cardId).child("isCompleted").setValue(isCompleted)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    @Override
    public void updateCardField(String cardId, String fieldName, Object value, GeneralCallback callback) {
        mCardsRef.child(cardId).child(fieldName).setValue(value)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    @Override
    public void updateCard(String cardId, Map<String, Object> updates, GeneralCallback callback) {
        mCardsRef.child(cardId).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    @Override
    public void deleteCard(String cardId, GeneralCallback callback) {
        // Cần xóa thẻ và cả trong 'tagCards'
        // (Logic xóa tagCards nên nằm trong hàm riêng hoặc TagRepository)
        mCardsRef.child(cardId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
        // (Lưu ý: Logic xóa trong tagCards chưa được triển khai ở đây)
    }
}