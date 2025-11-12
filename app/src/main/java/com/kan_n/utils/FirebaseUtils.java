package com.kan_n.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {

    // Thể hiện (instance) của FirebaseAuth
    private static FirebaseAuth mAuth;

    // Thể hiện (instance) của FirebaseDatabase
    private static FirebaseDatabase mDatabase;

    /**
     * Trả về thể hiện duy nhất của FirebaseAuth.
     * Nếu chưa được khởi tạo, nó sẽ được khởi tạo.
     *
     * @return Thể hiện của FirebaseAuth.
     */
    public static FirebaseAuth getAuthInstance() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }

    /**
     * Trả về thể hiện duy nhất của FirebaseDatabase.
     * Nếu chưa được khởi tạo, nó sẽ được khởi tạo.
     *
     * @return Thể hiện của FirebaseDatabase.
     */
    public static FirebaseDatabase getDatabaseInstance() {
        if (mDatabase == null) {
            // Khởi tạo FirebaseDatabase
            // Bạn có thể bật tính năng duy trì dữ liệu offline nếu cần
            // mDatabase.setPersistenceEnabled(true);
            mDatabase = FirebaseDatabase.getInstance();
        }
        return mDatabase;
    }

    /**
     * Phương thức tiện ích để lấy DatabaseReference gốc.
     *
     * @return DatabaseReference gốc.
     */
    public static DatabaseReference getRootRef() {
        return getDatabaseInstance().getReference();
    }

    /**
     * Phương thức tiện ích để lấy người dùng hiện tại đang đăng nhập.
     *
     * @return FirebaseUser nếu đã đăng nhập, ngược lại trả về null.
     */
    public static FirebaseUser getCurrentUser() {
        return getAuthInstance().getCurrentUser();
    }

    /**
     * Phương thức tiện ích để lấy ID của người dùng hiện tại.
     *
     * @return Chuỗi String UID của người dùng nếu đã đăng nhập, ngược lại trả về null.
     */
    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    /**
     * Kiểm tra xem có người dùng nào đang đăng nhập hay không.
     *
     * @return true nếu có người dùng đăng nhập, false nếu không.
     */
    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * Đăng xuất người dùng hiện tại.
     */
    public static void signOut() {
        getAuthInstance().signOut();
    }
}