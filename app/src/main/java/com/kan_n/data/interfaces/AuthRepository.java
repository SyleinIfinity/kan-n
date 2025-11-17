// Đặt tại: app/src/main/java/com/kan_n/data/interfaces/AuthRepository.java

package com.kan_n.data.interfaces;

import com.google.firebase.auth.FirebaseAuth;
import com.kan_n.data.models.User;

public interface AuthRepository {

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface GeneralCallback {
        void onSuccess();
        void onError(String message);
    }

    FirebaseAuth getAuthInstance();

    /**
     * Hành động đăng ký user mới.
     * ✨ Đã bổ sung 'phone'
     */
    void createUser(String username, String passwordPlain, String displayName, String email, String avatarUrl, String phone, GeneralCallback callback);

    /**
     * Hành động đăng nhập.
     * ✨ Đã thay đổi 'username' thành 'email'
     */
    void login(String email, String passwordPlain, AuthCallback callback);
}