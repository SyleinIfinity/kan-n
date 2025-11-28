package com.kan_n.ui.fragments.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // <-- Thêm import này
import android.os.Looper; // <-- Thêm import này
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
import com.kan_n.databinding.FragmentTrangDangnhapBinding;
import com.kan_n.ui.activities.MainActivity;

public class DangNhapFragment extends Fragment {

    private FragmentTrangDangnhapBinding binding;
    private AuthViewModel viewModel;

    private NavController navController;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrangDangnhapBinding.inflate(inflater, container, false);

        // Lấy ViewModel chung của AuthActivity
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nút quay lại
        binding.nutQuayLai.setOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        // Nút Đăng nhập
        binding.nutDangNhap.setOnClickListener(v -> {
            String email = binding.nhapGmail.getText().toString().trim();
            String password = binding.nhapMatKhau.getText().toString().trim();
            viewModel.login(email, password);
        });

        navController = NavHostFragment.findNavController(this);

        binding.textQuenMatKhau.setOnClickListener(v -> {
            navController.navigate(R.id.action_dangNhapFragment_to_nhapGmailFragment);
        });

        // Lắng nghe kết quả đăng nhập
        observeViewModel();
    }

    private void observeViewModel() {
        // 1. Đăng nhập thành công
        viewModel.loginSuccess.observe(getViewLifecycleOwner(), user -> {
            Toast.makeText(getContext(), "Đăng nhập thành công! Chào " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
            // ✨ Gọi hàm chuyển màn hình robust
            goToMainActivityRobust();
        });

        // 2. Đăng nhập thất bại
        viewModel.loginError.observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
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