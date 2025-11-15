package com.kan_n.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {

    // ✨ XÓA DÒNG NÀY ĐI
    // private static final String DATABASE_URL = "...";

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
     * ✨ ĐÃ CẬP NHẬT: Nó sẽ tự động lấy instance [DEFAULT]
     * (đã được cấu hình trong MyApplication)
     */
    public static FirebaseDatabase getDatabaseInstance() {
        if (mDatabase == null) {
            // ✨ CHỈ GỌI getInstance()
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
}