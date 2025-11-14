package com.kan_n.ui.fragments.auth;

import android.app.Application;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kan_n.data.interfaces.AuthRepository;
import com.kan_n.data.models.User;
import com.kan_n.data.repository.AuthRepositoryImpl;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    // LiveData cho luồng Đăng nhập
    private final MutableLiveData<User> _loginSuccess = new MutableLiveData<>();
    public LiveData<User> loginSuccess = _loginSuccess;

    private final MutableLiveData<String> _loginError = new MutableLiveData<>();
    public LiveData<String> loginError = _loginError;

    // LiveData cho luồng Đăng ký
    private final MutableLiveData<String> _registerSuccess = new MutableLiveData<>();
    public LiveData<String> registerSuccess = _registerSuccess;

    private final MutableLiveData<String> _registerError = new MutableLiveData<>();
    public LiveData<String> registerError = _registerError;


    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepositoryImpl();
    }

    /**
     * Tách email thành username (ví dụ: "user@gmail.com" -> "user")
     * (Vẫn cần thiết vì model User của bạn có trường 'username'
     * nhưng màn hình đăng ký không có)
     */
    private String deriveUsernameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }
        return email.split("@")[0];
    }

    // --- HÀNH ĐỘNG ĐĂNG NHẬP (Đã cập nhật) ---
    public void login(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            _loginError.setValue("Vui lòng nhập email và mật khẩu.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginError.setValue("Email không hợp lệ.");
            return;
        }

        // ✨ Gọi hàm login bằng email
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                // SỬA LỖI: Dùng setValue() để cập nhật LiveData ngay lập tức
                _loginSuccess.setValue(user);
            }

            @Override
            public void onError(String message) {
                _loginError.postValue(message);
            }
        });
    }

    // --- HÀNH ĐỘNG ĐĂNG KÝ (Giữ nguyên) ---
    public void register(String displayName, String phone, String email, String password, String confirmPassword) {
        // 1. Kiểm tra nhập đầy đủ
        if (TextUtils.isEmpty(displayName) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(confirmPassword)) {
            _registerError.setValue("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        // 2. Kiểm tra định dạng Email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerError.setValue("Email không hợp lệ.");
            return;
        }

        // 3. (Tùy chọn) Kiểm tra SĐT (đơn giản)
        if (phone.length() < 10) {
            _registerError.setValue("Số điện thoại không hợp lệ.");
            return;
        }

        // 4. Kiểm tra mật khẩu nhập lại
        if (!password.equals(confirmPassword)) {
            _registerError.setValue("Mật khẩu xác nhận không khớp.");
            return;
        }

        // 5. Kiểm tra độ dài mật khẩu (Firebase yêu cầu tối thiểu 6)
        if (password.length() < 6) {
            _registerError.setValue("Mật khẩu phải có ít nhất 6 ký tự.");
            return;
        }

        // 6. Lấy username (tạm thời) từ email
        String username = deriveUsernameFromEmail(email);
        if (username == null) {
            _registerError.setValue("Email không hợp lệ (không thể tạo username).");
            return;
        }

        // 7. ✨ Gọi hàm createUser (đã thêm 'phone')
        authRepository.createUser(username, password, displayName, email, "", phone, new AuthRepository.GeneralCallback() {
            @Override
            public void onSuccess() {
                _registerSuccess.postValue("Đăng ký thành công!");
            }

            @Override
            public void onError(String message) {
                // Xử lý lỗi từ Firebase (ví dụ: email đã tồn tại)
                if (message.contains("email address is already in use")) {
                    _registerError.postValue("Email này đã được sử dụng.");
                } else {
                    _registerError.postValue(message);
                }
            }
        });
    }
}