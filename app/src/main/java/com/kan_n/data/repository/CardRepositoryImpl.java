package com.kan_n.data.repository;

import androidx.annotation.NonNull; // <-- Thêm import

import com.google.android.gms.tasks.OnCompleteListener; // <-- Thêm import
import com.google.android.gms.tasks.Task; // <-- Thêm import
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.kan_n.data.interfaces.CardRepository;
import com.kan_n.data.models.Card; // <-- Thêm import
import com.kan_n.utils.FirebaseUtils;

import java.util.HashMap; // <-- Thêm import
import java.util.Map; // <-- Thêm import

public class CardRepositoryImpl implements CardRepository {

    private final DatabaseReference cardsRef = FirebaseUtils.getDatabaseInstance().getReference("cards");

    @Override
    public void getCardsForList(String listId, ChildEventListener listener) {
        // Các thẻ thuộc listId này,
        // Sắp xếp theo "position"
        Query cardsQuery = cardsRef.orderByChild("listId").equalTo(listId).orderByChild("position"); // <-- Sắp xếp theo position

        // Gắn ChildEventListener
        cardsQuery.addChildEventListener(listener);
    }

    @Override
    public void createCard(String listId, String title, double position, GeneralCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        String cardId = cardsRef.push().getKey();
        if (cardId == null) {
            callback.onError("Không thể tạo Id cho thẻ.");
            return;
        }

        Card newCard = new Card(listId, title, position, currentUserId);

        cardsRef.child(cardId).setValue(newCard.toMap()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    @Override
    public void setCardCompleted(String cardId, boolean isCompleted, GeneralCallback callback) {
        updateCardField(cardId, "completed", isCompleted, callback); // Su dung ham `isCompleted` tu model
    }

    @Override
    public void updateCardField(String cardId, String fieldName, Object value, GeneralCallback callback) {
        cardsRef.child(cardId).child(fieldName).setValue(value).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    @Override
    public void updateCard(String cardId, Map<String, Object> updates, GeneralCallback callback) {
        cardsRef.child(cardId).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    @Override
    public void deleteCard(String cardId, GeneralCallback callback) {
        cardsRef.child(cardId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }
}