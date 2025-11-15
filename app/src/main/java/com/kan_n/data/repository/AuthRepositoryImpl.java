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
import com.kan_n.utils.FirebaseUtils;

public class AuthRepositoryImpl implements AuthRepository {

    private final FirebaseAuth mAuth;
    private final DatabaseReference mUsersRef;

    public AuthRepositoryImpl() {
        this.mAuth = FirebaseUtils.getAuthInstance();
        this.mUsersRef = FirebaseUtils.getRootRef().child("users");
    }

    /**
     * ✨ Đã cập nhật: Thêm 'phone'
     */
    @Override
    public void createUser(String username, String passwordPlain, String displayName, String email, String avatarUrl, String phone, GeneralCallback callback) {

        // Bước 1: Tạo user bằng email và password trên FirebaseAuth
        mAuth.createUserWithEmailAndPassword(email, passwordPlain)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();

                        // Bước 2: Tạo đối tượng User mới (đã thêm 'phone')
                        User newUser = new User(username, displayName, email, avatarUrl, phone);

                        // Lưu thông tin user vào Realtime Database với key là UID
                        mUsersRef.child(uid).setValue(newUser.toMap())
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        callback.onSuccess(); // Hoàn tất!
                                    } else {
                                        callback.onError(dbTask.getException().getMessage());
                                    }
                                });
                    } else {
                        // Tạo tài khoản Auth thất bại (ví dụ: email đã tồn tại)
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    @Override
    public void login(String email, String passwordPlain, AuthCallback callback) {

        // Bước 1: Đăng nhập bằng Firebase Auth
        mAuth.signInWithEmailAndPassword(email, passwordPlain)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập Auth thành công
                        String uid = task.getResult().getUser().getUid();

                        // Bước 2: Lấy thông tin chi tiết của User từ Realtime Database
                        mUsersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    User user = snapshot.getValue(User.class);
                                    if (user != null) {
                                        user.setUid(snapshot.getKey());
                                        callback.onSuccess(user); // Trả về User model đầy đủ
                                    } else {
                                        callback.onError("Không thể đọc dữ liệu người dùng.");
                                    }
                                } else {
                                    // Hiếm khi xảy ra nếu đăng ký đúng: Auth có user nhưng DB không có
                                    callback.onError("Không tìm thấy dữ liệu người dùng trong DB.");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onError(error.getMessage());
                            }
                        });

                    } else {
                        // Đăng nhập Auth thất bại (sai email, sai mật khẩu, user không tồn tại)
                        callback.onError("Email hoặc mật khẩu không đúng.");
                    }
                });
    }
}