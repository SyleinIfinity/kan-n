package com.kan_n.data.interfaces;

import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.kan_n.data.models.User; // Bạn cần tạo model User

public interface AuthRepository {

    /**
     * Lấy người dùng Firebase hiện tại (nếu đã đăng nhập).
     * @return LiveData<FirebaseUser>
     */
    LiveData<FirebaseUser> getCurrentUser();

    /**
     * Lấy chi tiết thông tin người dùng (từ Realtime Database/Firestore) bằng ID.
     * @param userId ID của người dùng
     * @return LiveData<User>
     */
    LiveData<User> getUserDetails(String userId);

    /**
     * Thực hiện đăng nhập bằng email và mật khẩu.
     * @param email Email người dùng
     * @param password Mật khẩu
     * @return Task<AuthResult> để theo dõi kết quả
     */
    Task<AuthResult> login(String email, String password);

    /**
     * Thực hiện đăng ký người dùng mới.
     * @param email Email
     * @param password Mật khẩu
     * @param username Tên hiển thị
     * @return Task<AuthResult>
     */
    Task<AuthResult> register(String email, String password, String username);

    /**
     * Lưu thông tin người dùng (như username, email) vào cơ sở dữ liệu sau khi đăng ký thành công.
     * @param user Đối tượng User chứa thông tin
     * @return Task<Void>
     */
    Task<Void> saveUserDetails(User user);

    /**
     * Đăng xuất người dùng hiện tại.
     */
    void logout();
}