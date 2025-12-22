package com.kan_n.ui.fragments.thongtin;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kan_n.data.models.User;
import com.kan_n.utils.FirebaseUtils;
import java.util.Map;
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
    /**
     * Cập nhật thông tin user lên Firebase Realtime Database
     * @param uid: ID của user
     * @param updates: Map chứa các trường cần update (ví dụ: displayName, phone...)
     * @param onCompleteListener: callback để biết thành công hay thất bại
     */
    public void updateUserInfo(String uid, Map<String, Object> updates, OnCompleteListener<Void> onCompleteListener) {
        // Lấy tham chiếu trực tiếp đến node users/{uid}
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        // Dùng updateChildren để chỉ sửa những trường thay đổi, không ghi đè toàn bộ object
        userRef.updateChildren(updates).addOnCompleteListener(onCompleteListener);
    }
}