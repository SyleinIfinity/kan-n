// Đặt tại: app/src/main/java/com/kan_n/data/repository/ListRepositoryImpl.java

package com.kan_n.data.repository;

import androidx.annotation.NonNull;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.interfaces.ListRepository;
import com.kan_n.data.models.ListModel;
import com.kan_n.utils.FirebaseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListRepositoryImpl implements ListRepository {

    private final DatabaseReference mRootRef;
    private final DatabaseReference mListsRef;
    private final DatabaseReference mCardsRef;

    private final DatabaseReference listsRef = FirebaseUtils.getDatabaseInstance().getReference("lists");

    public ListRepositoryImpl() {
        this.mRootRef = FirebaseUtils.getRootRef();
        this.mListsRef = mRootRef.child("lists");
        this.mCardsRef = mRootRef.child("cards");
    }

    @Override
    public void createList(String boardId, String title, double position, GeneralCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        String listId = mListsRef.push().getKey();
        if (listId == null) {
            callback.onError("Không thể tạo ID cho danh sách.");
            return;
        }

        ListModel newList = new ListModel(boardId, title, position, currentUserId);

        mListsRef.child(listId).setValue(newList.toMap()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    @Override
    public void getListsForBoard(String boardId, ChildEventListener listener) {
        // Lấy các danh sách thuộc boardId này,
        // Sắp xếp theo "position"
        Query listsQuery = listsRef.orderByChild("boardId").equalTo(boardId);

        // Gắn ChildEventListener
        listsQuery.addChildEventListener(listener);
    }

    @Override
    public void updateListTitle(String listId, String newTitle, GeneralCallback callback) {
        mListsRef.child(listId).child("title").setValue(newTitle).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    @Override
    public void deleteList(String listId, GeneralCallback callback) {

        // Tìm tất cả thẻ thuộc về danh sách này
        mCardsRef.orderByChild("listId").equalTo(listId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Map<String, Object> updates = new HashMap<>();

                // Đánh dấu xóa chính danh sách này
                updates.put("/lists/" + listId, null);

                // Đánh dấu xóa tất cả thẻ con
                if (snapshot.exists()) {
                    for (DataSnapshot cardSnap : snapshot.getChildren()) {
                        updates.put("/cards/" + cardSnap.getKey(), null);
                    }
                }

                // Thực hiện xóa đồng thời
                mRootRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}