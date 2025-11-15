package com.kan_n.ui.fragments.thongtin;

import androidx.lifecycle.ViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseUser;
import com.kan_n.data.models.User;
import com.kan_n.data.repository.AuthRepositoryImpl;
import com.kan_n.data.interfaces.AuthRepository;
import com.kan_n.utils.FirebaseUtils;

public class ThongTinViewModel extends ViewModel {

    private final FirebaseUtils firebaseutils;

    public ThongTinViewModel() {
        // Khởi tạo repository
        firebaseutils = new FirebaseUtils();
    }

    /**
     * Lấy thông tin FirebaseUser hiện tại (nếu đã đăng nhập).
     * @return FirebaseUser
     */
    public FirebaseUser getCurrentUser() {
        return firebaseutils.getCurrentUser();
    }

    /**
     * Lấy dữ liệu User (từ Realtime Database) bằng uid.
     * @param uid UID của người dùng
     * @param callback Hàm callback để xử lý kết quả
     */
    public void getUserData(String uid, OnCompleteListener<User> callback) {
        firebaseutils.getUserData(uid, callback);
    }

    /**
     * Thực hiện đăng xuất người dùng hiện tại.
     */
    public void logout() {
        firebaseutils.logout();
    }
}