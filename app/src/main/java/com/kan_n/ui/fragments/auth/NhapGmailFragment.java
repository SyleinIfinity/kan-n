package com.kan_n.ui.fragments.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.kan_n.R;
import com.kan_n.databinding.FragmentNhapGmailBinding;
import com.kan_n.ui.activities.MainActivity;

public class NhapGmailFragment extends Fragment {

    private FragmentNhapGmailBinding binding;
    private AuthViewModel viewModel;

    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNhapGmailBinding.inflate(inflater, container, false);

        // Lấy ViewModel chung của AuthActivity
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy NavController
        navController = NavHostFragment.findNavController(this); // Đảm bảo bạn đã có dòng này

        // Nút quay lại
        binding.nutQuayLai.setOnClickListener(v -> {
            navController.popBackStack(); // Sửa lại: Dùng NavController để quay lại
        });

        // ✨ SỬA ĐỔI NÚT GỬI OTP
        binding.nutGuiOtp.setOnClickListener(v -> {
            String email = binding.nhapGmail.getText().toString().trim();
            // (Bạn có thể hiển thị ProgressBar ở đây)
            viewModel.sendPasswordResetLink(email);
        });

        // Lắng nghe kết quả
        observeViewModel(); // Sửa: gọi hàm observeViewModel mới
    }

    private void observeViewModel() {
        // 1. Gửi link thành công
        viewModel.resetEmailSuccess.observe(getViewLifecycleOwner(), successMessage -> {
            // (Ẩn ProgressBar)
            Toast.makeText(getContext(), successMessage, Toast.LENGTH_LONG).show();
            // Quay về màn hình đăng nhập
            navController.popBackStack();
        });

        // 2. Gửi link thất bại
        viewModel.resetEmailError.observe(getViewLifecycleOwner(), errorMessage -> {
            // (Ẩn ProgressBar)
            Toast.makeText(getContext(), "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Phương thức chuyển màn hình an toàn, đảm bảo Intent được gửi đi
     * trước khi Activity hiện tại bị hủy.
     */
    private void goToMainActivityRobust() {
        if (getActivity() == null) return;

        // 1. Khởi chạy MainActivity (sử dụng cờ CLEAR_TASK để xóa AuthActivity khỏi stack)
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // 2. Sử dụng Handler để trì hoãn việc hủy Activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        }, 300); // 300ms là đủ an toàn.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
