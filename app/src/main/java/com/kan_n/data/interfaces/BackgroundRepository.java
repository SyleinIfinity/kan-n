package com.kan_n.data.interfaces;

import com.kan_n.data.models.Background;
import java.util.List;

public interface BackgroundRepository {

    interface BackgroundsCallback {
        void onSuccess(List<Background> backgrounds);
        void onError(String message);
    }

    /**
     * Lấy TẤT CẢ phông nền (cả màu và ảnh) từ Firebase.
     */
    void getAllBackgrounds(BackgroundsCallback callback);
}