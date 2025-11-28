// Đặt tại: data/repository/AuthRepositoryImpl.java
package com.kan_n.data.repository;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.interfaces.AuthRepository;
import com.kan_n.data.models.Background;
import com.kan_n.data.models.Board;
import com.kan_n.data.models.Membership;
import com.kan_n.data.models.User;
import com.kan_n.data.models.Workspace;
import com.kan_n.utils.FirebaseUtils;

import java.util.HashMap;
import java.util.Map;

public class AuthRepositoryImpl implements AuthRepository {

    private final FirebaseAuth mAuth;
    private final DatabaseReference mRootRef;

    public AuthRepositoryImpl() {
        this.mAuth = FirebaseUtils.getAuthInstance();
        // Khởi tạo mRootRef
        this.mRootRef = FirebaseUtils.getRootRef();
    }

    @Override
    public FirebaseAuth getAuthInstance() {
        return this.mAuth;
    }
    @Override
    public void createUser(String username, String passwordPlain, String displayName, String email, String avatarUrl, String phone, GeneralCallback callback) {

        // Tạo user bằng email và password trên FirebaseAuth
        mAuth.createUserWithEmailAndPassword(email, passwordPlain)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();

                        // Gửi email kích hoạt
                        task.getResult().getUser().sendEmailVerification()
                                .addOnCompleteListener(verificationTask -> {
                                    if (verificationTask.isSuccessful()) {

                                        // Tạo các đối tượng

                                        User newUser = new User(username, displayName, email, avatarUrl, phone);


                                        Workspace defaultWorkspace = new Workspace(
                                                "Không gian cá nhân", // Tên Workspace
                                                "Không gian làm việc đầu tiên của bạn", // Mô tả
                                                uid // createdBy
                                        );

                                        String workspaceId = mRootRef.child("workspaces").push().getKey();


                                        Background defaultBg = new Background("color", "#0079BF"); // Màu xanh Trello
                                        Board defaultBoard = new Board(
                                                workspaceId,
                                                "Bảng Chào Mừng",
                                                "Đây là bảng đầu tiên của bạn!",
                                                "private", // Quyền riêng tư
                                                uid, // createdBy
                                                defaultBg
                                        );
                                        // Lấy key mới cho board
                                        String boardId = mRootRef.child("boards").push().getKey();

                                        // Tạo Membership (liên kết User với Board)
                                        Membership defaultMembership = new Membership(
                                                boardId, // ID của bảng
                                                uid, // ID của user
                                                "owner" // Vai trò
                                        );
                                        // Lấy key mới cho membership
                                        String membershipId = mRootRef.child("memberships").push().getKey();

                                        // Kiểm tra null cho các key
                                        if (workspaceId == null || boardId == null || membershipId == null) {
                                            callback.onError("Không thể tạo ID cho dữ liệu ban đầu.");
                                            return;
                                        }

                                        // Ghi tất cả vào DB bằng một lệnh updateChildren (an toàn)
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("/users/" + uid, newUser.toMap());
                                        updates.put("/workspaces/" + workspaceId, defaultWorkspace.toMap());
                                        updates.put("/boards/" + boardId, defaultBoard.toMap());
                                        updates.put("/memberships/" + membershipId, defaultMembership.toMap());

                                        mRootRef.updateChildren(updates).addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                callback.onSuccess();
                                            } else {
                                                callback.onError(dbTask.getException().getMessage());
                                            }
                                        });

                                    } else {
                                        callback.onError(verificationTask.getException().getMessage());
                                    }
                                });
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    @Override
    public void login(String email, String passwordPlain, AuthCallback callback) {

        // Đăng nhập bằng Firebase Auth
        mAuth.signInWithEmailAndPassword(email, passwordPlain)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // KIỂM TRA KÍCH HOẠT
                        if (!task.getResult().getUser().isEmailVerified()) {
                            callback.onError("Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email.");
                            mAuth.signOut(); // Đăng xuất user ra
                            return;
                        }

                        String uid = task.getResult().getUser().getUid();

                        // Lấy thông tin chi tiết của User từ Realtime Database
                        mRootRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    User user = snapshot.getValue(User.class);
                                    if (user != null) {
                                        user.setUid(snapshot.getKey());
                                        callback.onSuccess(user);
                                    } else {
                                        callback.onError("Không thể đọc dữ liệu người dùng.");
                                    }
                                } else {
                                    callback.onError("Không tìm thấy dữ liệu người dùng trong DB.");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onError(error.getMessage());
                            }
                        });

                    } else {
                        callback.onError("Email hoặc mật khẩu không đúng.");
                    }
                });
    }
}