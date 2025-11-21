package com.kan_n.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;

// Thêm import cho OnCompleteListener và Model User
import com.google.android.gms.tasks.OnCompleteListener;
import com.kan_n.data.models.User; // Đảm bảo đường dẫn này đúng tới model User của bạn

public class FirebaseUtils {
    private static FirebaseAuth mAuth;
    private static FirebaseDatabase mDatabase;

    public static FirebaseAuth getAuthInstance() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }

    /**
     * Trả về thể hiện duy nhất của FirebaseDatabase.
     * (đã được cấu hình trong MyApplication)
     */
    public static FirebaseDatabase getDatabaseInstance() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
        }
        return mDatabase;
    }

    public static DatabaseReference getRootRef() {
        return getDatabaseInstance().getReference();
    }

    public static FirebaseUser getCurrentUser() {
        return getAuthInstance().getCurrentUser();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public static void signOut() {
        getAuthInstance().signOut();
    }
    /**
     * Thực hiện đăng xuất người dùng hiện tại.
     * Phương thức này gọi hàm signOut() đã tồn tại ở trên.
     */
    public static void logout() {
        signOut();
    }

    /**
     * Lấy dữ liệu User (từ Realtime Database) bằng uid.
     * Sử dụng .get() để lấy dữ liệu một lần và .continueWith để
     * chuyển đổi kết quả từ DataSnapshot sang đối tượng User.
     *
     * uid UID của người dùng
     * callback Hàm callback (OnCompleteListener<User>) để xử lý kết quả
     */
    public static void getUserData(String uid, OnCompleteListener<User> callback) {
        DatabaseReference userRef = getRootRef().child("users").child(uid);

        // Dùng .get() để lấy dữ liệu một lần
        userRef.get()
                // Dùng continueWith để chuyển đổi Task<DataSnapshot> thành Task<User>
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        // Nếu Task thất bại, ném ra exception
                        if (task.getException() != null) {
                            throw task.getException();
                        }
                        // Trường hợp lỗi không xác định
                        throw new Exception("Lỗi không xác định khi lấy dữ liệu User");
                    }

                    // Nếu thành công, chuyển đổi DataSnapshot thành User object
                    DataSnapshot dataSnapshot = task.getResult();
                    return dataSnapshot.getValue(User.class);
                })
                // Gắn callback của người dùng vào Task<User> đã được chuyển đổi
                .addOnCompleteListener(callback);
    }
}