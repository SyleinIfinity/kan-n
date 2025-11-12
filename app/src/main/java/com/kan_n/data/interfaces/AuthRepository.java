// Đặt tại: app/src/main/java/com/kan_n/data/interfaces/AuthRepository.java

package com.kan_n.data.interfaces;

import com.kan_n.data.models.User;

public interface AuthRepository {

    /**
     * Định nghĩa các callback interface lồng nhau
     * để AuthRepositoryImpl có thể sử dụng và báo cáo kết quả.
     */
    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface GeneralCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Hành động đăng ký user mới.
     * Phương thức này khớp với phương thức trong AuthRepositoryImpl,
     * yêu cầu cả username và email.
     */
    void createUser(String username, String passwordPlain, String displayName, String email, String avatarUrl, GeneralCallback callback);

    /**
     * Hành động đăng nhập.
     * Phương thức này khớp với phương thức trong AuthRepositoryImpl,
     * cho phép đăng nhập bằng username.
     */
    void login(String username, String passwordPlain, AuthCallback callback);
}