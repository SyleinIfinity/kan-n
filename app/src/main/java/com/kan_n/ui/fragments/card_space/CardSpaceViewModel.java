package com.kan_n.ui.fragments.card_space;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.models.CheckItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardSpaceViewModel extends ViewModel {

    private final DatabaseReference mDatabase;

    public CardSpaceViewModel() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    // --- XỬ LÝ NGÀY THÁNG ---
    public void updateCardStartDate(String cardId, long timestamp) {
        mDatabase.child("cards").child(cardId).child("startDate").setValue(timestamp);
    }

    public void updateCardDueDate(String cardId, long timestamp) {
        mDatabase.child("cards").child(cardId).child("dueDate").setValue(timestamp);
    }

    // --- XỬ LÝ CHECKLIST ---
    public void addChecklistItem(String cardId, CheckItem item) {

        DatabaseReference ref = mDatabase.child("cards").child(cardId).child("checkList");

        mDatabase.child("cards").child(cardId).child("checkList").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                mDatabase.child("cards").child(cardId).child("checkList").child(String.valueOf(count)).setValue(item);
            }
            @Override public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) { }
        });
    }

    // Cập nhật toàn bộ checklist (Dùng khi tick/untick checkbox)
    public void updateCheckList(String cardId, List<CheckItem> newCheckList) {
        mDatabase.child("cards").child(cardId).child("checkList").setValue(newCheckList);
    }


    public void logActivity(String cardId, String content) {
        if (cardId == null) return;

        com.kan_n.data.models.Activity activity = new com.kan_n.data.models.Activity(content, System.currentTimeMillis());

        // Lưu vào node "activities"
        mDatabase.child("cards").child(cardId).child("activities").push().setValue(activity);
    }

    public void updateTagAndPropagate(String cardId, String tagId, String newName, String newColor, TagUpdateCallback callback) {
        // Map chứa tất cả các lệnh update để chạy 1 lần duy nhất (Atomic)
        Map<String, Object> updates = new HashMap<>();

        // 1. Cập nhật thông tin gốc tại bảng 'tags'
        updates.put("/tags/" + tagId + "/name", newName);
        updates.put("/tags/" + tagId + "/color", newColor);

        // 2. Cập nhật cho THẺ NGUỒN (Source Card - Thẻ đang giữ selfTagId)
        updates.put("/cards/" + cardId + "/selfTagColor", newColor);
        updates.put("/cards/" + cardId + "/labelColor", newColor);

        // 3. QUÉT TOÀN BỘ DB ĐỂ TÌM THẺ ĐANG MƯỢN TAG (Subscribers)
        // Lưu ý: Logic này sẽ quét toàn bộ node 'cards' để tìm assignedTagId khớp
        mDatabase.child("cards").orderByChild("assignedTagId").equalTo(tagId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Duyệt qua tất cả kết quả tìm được (Bất kể nó nằm ở DS2, DS3 hay DSn)
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String subCardId = ds.getKey();
                            if (subCardId != null) {
                                // Cập nhật màu mượn mới
                                updates.put("/cards/" + subCardId + "/assignedTagColor", newColor);
                                // Cập nhật luôn labelColor để hiển thị ngay
                                updates.put("/cards/" + subCardId + "/labelColor", newColor);
                            }
                        }

                        // 4. Thực thi Update
                        mDatabase.updateChildren(updates, (error, ref) -> {
                            if (error != null) {
                                callback.onError("Lỗi cập nhật: " + error.getMessage());
                            } else {
                                callback.onSuccess();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError("Lỗi tìm kiếm: " + error.getMessage());
                    }
                });
    }

    // Interface callback cho việc update Tag
    public interface TagUpdateCallback {
        void onSuccess();
        void onError(String message);
    }
}