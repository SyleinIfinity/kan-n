package com.kan_n.ui.fragments.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.kan_n.R;
import com.kan_n.databinding.FragmentTrangKhoidongBinding; // Import ViewBinding

public class KhoiDongFragment extends Fragment {

    private FragmentTrangKhoidongBinding binding;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrangKhoidongBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy NavController
        navController = NavHostFragment.findNavController(this);

        // --- XỬ LÝ SỰ KIỆN NHẤN NÚT ---

        // 1. Nút Đăng nhập
        binding.btnDangNhap.setOnClickListener(v -> {
            // Điều hướng sang trang đăng nhập
            navController.navigate(R.id.action_khoiDongFragment_to_dangNhapFragment);
        });

        // 2. Nút Đăng ký
        binding.btnDangKy.setOnClickListener(v -> {
            // Điều hướng sang trang đăng ký
            navController.navigate(R.id.action_khoiDongFragment_to_dangKyFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh rò rỉ bộ nhớ
    }
}