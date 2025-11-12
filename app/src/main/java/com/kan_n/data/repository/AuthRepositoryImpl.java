// Đặt tại: data/repository/AuthRepositoryImpl.java
package com.kan_n.data.repository;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.interfaces.AuthRepository;
import com.kan_n.data.models.User;
import com.kan_n.utils.FirebaseUtils; // <-- SỬ DỤNG UTILS CỦA BẠN

// KHÔNG CẦN PasswordUtils nữa

public class AuthRepositoryImpl implements AuthRepository {

    // Lấy instance từ FirebaseUtils
    private final FirebaseAuth mAuth;
    private final DatabaseReference mUsersRef;

    public AuthRepositoryImpl() {
        // Khởi tạo các dịch vụ thông qua Utils
        this.mAuth = FirebaseUtils.getAuthInstance();
        this.mUsersRef = FirebaseUtils.getRootRef().child("users");
    }

    /**
     * Bước 1: Tạo tài khoản trên Firebase Auth (dùng email, password).
     * Bước 2: Lưu thông tin bổ sung (username, displayName) vào Realtime Database.
     */
    @Override
    public void createUser(String username, String passwordPlain, String displayName, String email, String avatarUrl, GeneralCallback callback) {

        // Bước 1: Tạo user bằng email và password trên FirebaseAuth
        mAuth.createUserWithEmailAndPassword(email, passwordPlain)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Tạo tài khoản Auth thành công
                        String uid = task.getResult().getUser().getUid();

                        // Bước 2: Tạo đối tượng User mới (đã bỏ password_hash)
                        User newUser = new User(username, displayName, email, avatarUrl);

                        // Lưu thông tin user vào Realtime Database với key là UID
                        mUsersRef.child(uid).setValue(newUser.toMap())
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        callback.onSuccess(); // Hoàn tất!
                                    } else {
                                        // (Xử lý lỗi: không lưu được vào DB, nhưng đã tạo Auth)
                                        callback.onError(dbTask.getException().getMessage());
                                    }
                                });
                    } else {
                        // Tạo tài khoản Auth thất bại (ví dụ: email đã tồn tại)
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    /**
     * Logic đăng nhập này phức tạp hơn vì interface của bạn dùng (username, password),
     * nhưng FirebaseAuth dùng (email, password).
     *
     * Bước 1: Tìm email dựa trên username.
     * Bước 2: Dùng email + password để đăng nhập vào FirebaseAuth.
     */
    @Override
    public void login(String username, String passwordPlain, AuthCallback callback) {

        // Bước 1: Tìm user trong Realtime Database bằng username
        mUsersRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // Không tìm thấy username
                            callback.onError("Username không tồn tại.");
                            return;
                        }

                        // Lấy email của user
                        User foundUser = null;
                        String uid = null;
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            foundUser = userSnapshot.getValue(User.class);
                            uid = userSnapshot.getKey();
                            break; // Chỉ lấy user đầu tiên tìm thấy
                        }

                        if (foundUser == null || foundUser.getEmail() == null) {
                            callback.onError("Lỗi dữ liệu người dùng.");
                            return;
                        }

                        foundUser.setUid(uid);
                        String email = foundUser.getEmail();

                        // ✨ BƯỚC SỬA LỖI:
                        // Tạo một biến final mới để lambda có thể sử dụng
                        final User userToLogin = foundUser;

                        // Bước 2: Dùng email + password để đăng nhập FirebaseAuth
                        mAuth.signInWithEmailAndPassword(email, passwordPlain)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Đăng nhập thành công!
                                        callback.onSuccess(userToLogin); // <-- Sử dụng biến final
                                    } else {
                                        // Đăng nhập thất bại (sai mật khẩu)
                                        callback.onError("Sai mật khẩu.");
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