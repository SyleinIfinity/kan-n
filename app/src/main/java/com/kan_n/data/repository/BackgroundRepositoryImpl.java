package com.kan_n.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.interfaces.BackgroundRepository;
import com.kan_n.data.models.Background;
import com.kan_n.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class BackgroundRepositoryImpl implements BackgroundRepository {

    // Tham chiếu đến node "backgrounds"
    private final DatabaseReference mBackgroundsRef;

    public BackgroundRepositoryImpl() {
        this.mBackgroundsRef = FirebaseUtils.getRootRef().child("backgrounds");
    }

    /**
     * Lấy dữ liệu từ node `backgrounds`
     * Vì nó là một Array trong JSON, Firebase sẽ đọc nó theo index (0, 1, 2...)
     */
    @Override
    public void getAllBackgrounds(BackgroundsCallback callback) {
        mBackgroundsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Background> backgroundList = new ArrayList<>();
                if (snapshot.exists()) {
                    // Lặp qua các phần tử của array (0, 1, 2...)
                    for (DataSnapshot bgSnapshot : snapshot.getChildren()) {
                        Background bg = bgSnapshot.getValue(Background.class);
                        if (bg != null) {
                            backgroundList.add(bg);
                        }
                    }
                }
                callback.onSuccess(backgroundList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}