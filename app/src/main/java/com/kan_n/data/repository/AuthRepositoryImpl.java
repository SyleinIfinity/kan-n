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
        // ✨ 3. Khởi tạo mRootRef
        this.mRootRef = FirebaseUtils.getRootRef();
    }

    @Override
    public FirebaseAuth getAuthInstance() {
        return this.mAuth;
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

                        // Bước 2: Gửi email kích hoạt (vẫn giữ nguyên)
                        task.getResult().getUser().sendEmailVerification()
                                .addOnCompleteListener(verificationTask -> {
                                    if (verificationTask.isSuccessful()) {

                                        // Bước 3: Tạo tất cả các đối tượng

                                        // 1. Tạo User
                                        User newUser = new User(username, displayName, email, avatarUrl, phone);

                                        // 2. Tạo Workspace mặc định
                                        Workspace defaultWorkspace = new Workspace(
                                                "Không gian cá nhân", // Tên Workspace
                                                "Không gian làm việc đầu tiên của bạn", // Mô tả
                                                uid // createdBy
                                        );
                                        // Lấy key mới cho workspace
                                        String workspaceId = mRootRef.child("workspaces").push().getKey();

                                        // 3. Tạo Board "Chào mừng" mặc định
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

                                        // 4. Tạo Membership (liên kết User với Board)
                                        Membership defaultMembership = new Membership(
                                                boardId, // ID của bảng
                                                uid, // ID của user
                                                "owner" // Vai trò
                                        );
                                        // Lấy key mới cho membership
                                        String membershipId = mRootRef.child("memberships").push().getKey();

                                        // Kiểm tra null cho các key (phòng trường hợp lỗi)
                                        if (workspaceId == null || boardId == null || membershipId == null) {
                                            callback.onError("Không thể tạo ID cho dữ liệu ban đầu.");
                                            return;
                                        }

                                        // Bước 4: Ghi tất cả vào DB bằng một lệnh updateChildren (an toàn)
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("/users/" + uid, newUser.toMap());
                                        updates.put("/workspaces/" + workspaceId, defaultWorkspace.toMap());
                                        updates.put("/boards/" + boardId, defaultBoard.toMap());
                                        updates.put("/memberships/" + membershipId, defaultMembership.toMap());

                                        mRootRef.updateChildren(updates).addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                callback.onSuccess(); // Hoàn tất!
                                            } else {
                                                // Lỗi khi ghi DB
                                                callback.onError(dbTask.getException().getMessage());
                                            }
                                        });

                                    } else {
                                        // Gửi email thất bại
                                        callback.onError(verificationTask.getException().getMessage());
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

                        // ✨ KIỂM TRA KÍCH HOẠT
                        if (!task.getResult().getUser().isEmailVerified()) {
                            callback.onError("Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email.");
                            mAuth.signOut(); // Đăng xuất user ra
                            return;
                        }

                        // Đăng nhập Auth thành công và đã kích hoạt
                        String uid = task.getResult().getUser().getUid();

                        // Bước 2: Lấy thông tin chi tiết của User từ Realtime Database
                        mRootRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
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