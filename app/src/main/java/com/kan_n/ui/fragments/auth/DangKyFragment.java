package com.kan_n.ui.fragments.auth;

import android.os.Bundle;
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

import com.kan_n.databinding.FragmentTrangDangkyBinding;

public class DangKyFragment extends Fragment {

    private FragmentTrangDangkyBinding binding;
    private AuthViewModel viewModel;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrangDangkyBinding.inflate(inflater, container, false);

        // Lấy ViewModel chung
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // Lấy NavController
        navController = NavHostFragment.findNavController(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nút quay lại
        binding.nutQuayLai.setOnClickListener(v -> {
            navController.popBackStack(); // Quay lại màn hình trước
        });

        // Nút Đăng ký
        binding.nutDangKy.setOnClickListener(v -> {
            String displayName = binding.nhapHoTen.getText().toString().trim();
            // Lấy SĐT từ layout
            String phone = binding.nhapSdt.getText().toString().trim();
            String email = binding.nhapGmail.getText().toString().trim();
            String password = binding.nhapMatKhau.getText().toString().trim();
            String confirmPassword = binding.nhapXacNhanMatKhau.getText().toString().trim();


            viewModel.register(displayName, phone, email, password, confirmPassword);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        // 1. Đăng ký thành công
        viewModel.registerSuccess.observe(getViewLifecycleOwner(), successMessage -> {

            Toast.makeText(getContext(), "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.", Toast.LENGTH_LONG).show();

            // Điều hướng về trang đăng nhập
            navController.popBackStack();
        });

        // 2. Đăng ký thất bại
        viewModel.registerError.observe(getViewLifecycleOwner(), errorMessage -> {
            // (Ẩn loading...)
            Toast.makeText(getContext(), "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}