package com.kan_n.ui.fragments.card_space;

import androidx.lifecycle.ViewModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kan_n.data.models.CheckItem;
import java.util.List;

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


}