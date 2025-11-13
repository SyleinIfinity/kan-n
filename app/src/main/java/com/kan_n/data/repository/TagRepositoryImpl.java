// Đặt tại: app/src/main/java/com/kan_n/data/repository/TagRepositoryImpl.java

package com.kan_n.data.repository;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.interfaces.TagRepository;
import com.kan_n.data.models.Tag;
import com.kan_n.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagRepositoryImpl implements TagRepository {

    private final DatabaseReference mRootRef;
    private final DatabaseReference mTagsRef;
    private final DatabaseReference mCardsRef;
    private final DatabaseReference mTagCardsRef; // Tham chiếu đến node mapping

    public TagRepositoryImpl() {
        this.mRootRef = FirebaseUtils.getRootRef();
        this.mTagsRef = mRootRef.child("tags");
        this.mCardsRef = mRootRef.child("cards");
        this.mTagCardsRef = mRootRef.child("tagCards");
    }

    @Override
    public void getTagsForUser(String userId, TagsCallback callback) {
        if (userId == null) {
            callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        mTagsRef.orderByChild("createdBy").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Tag> tagList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot tagSnap : snapshot.getChildren()) {
                        Tag tag = tagSnap.getValue(Tag.class);
                        if (tag != null) {
                            tag.setUid(tagSnap.getKey());
                            tagList.add(tag);
                        }
                    }
                }
                callback.onSuccess(tagList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void createTag(Tag tag, GeneralCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        String tagId = mTagsRef.push().getKey();
        if (tagId == null) {
            callback.onError("Không thể tạo ID cho Tag.");
            return;
        }

        // Đảm bảo tag có thông tin người tạo
        tag.setCreatedBy(currentUserId);
        tag.setCreatedAt(System.currentTimeMillis());

        mTagsRef.child(tagId).setValue(tag.toMap()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    @Override
    public void updateTag(String tagId, Map<String, Object> updates, GeneralCallback callback) {
        mTagsRef.child(tagId).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    /**
     * Gắn một Nhãn vào một Thẻ.
     * Cập nhật đồng thời 2 nơi:
     * 1. /cards/{cardId}/tagIds/{tagId} = true
     * 2. /tagCards/{tagId}/{cardId} = true
     */
    @Override
    public void addTagToCard(String cardId, String tagId, GeneralCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("/cards/" + cardId + "/tagIds/" + tagId, true);
        updates.put("/tagCards/" + tagId + "/" + cardId, true);

        mRootRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    /**
     * Gỡ một Nhãn khỏi một Thẻ.
     * Cập nhật đồng thời 2 nơi (xóa = set giá trị null).
     * 1. /cards/{cardId}/tagIds/{tagId} = null
     * 2. /tagCards/{tagId}/{cardId} = null
     */
    @Override
    public void removeTagFromCard(String cardId, String tagId, GeneralCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("/cards/" + cardId + "/tagIds/" + tagId, null);
        updates.put("/tagCards/" + tagId + "/" + cardId, null);

        mRootRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    /**
     * Xóa một Nhãn (cascade delete).
     * CẢNH BÁO: Đây là hành động nặng, chạy trên client có thể rủi ro.
     * Nên được thực hiện bằng Cloud Function.
     */
    @Override
    public void deleteTag(String tagId, GeneralCallback callback) {

        // Bước 1: Lấy danh sách các thẻ đang dùng tag này từ /tagCards/
        mTagCardsRef.child(tagId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Map<String, Object> updates = new HashMap<>();

                // Đánh dấu xóa node tag chính
                updates.put("/tags/" + tagId, null);

                // Đánh dấu xóa node tagCards mapping
                updates.put("/tagCards/" + tagId, null);

                // Lặp qua các thẻ và đánh dấu xóa tagId khỏi chúng
                if (snapshot.exists()) {
                    for (DataSnapshot cardIdSnap : snapshot.getChildren()) {
                        String cardId = cardIdSnap.getKey();
                        if (cardId != null) {
                            updates.put("/cards/" + cardId + "/tagIds/" + tagId, null);
                        }
                    }
                }

                // Bước 2: Thực hiện xóa đồng thời
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