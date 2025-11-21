package com.kan_n.ui.fragments.card_space;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.kan_n.databinding.FragmentCardSpaceBinding;

public class CardSpaceFragment extends Fragment {

    private FragmentCardSpaceBinding binding;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Khởi tạo Binding
        binding = FragmentCardSpaceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy NavController
        navController = Navigation.findNavController(view);

        // --- 1. Nhận dữ liệu từ màn hình BangSpaceFragment gửi sang ---
        if (getArguments() != null) {
            String cardTitle = getArguments().getString("cardTitle");
            String cardId = getArguments().getString("cardId");

            // Ví dụ: Hiển thị tên thẻ lên thanh tiêu đề (tv_title)
            if (cardTitle != null) {
                binding.tvTitle.setText(cardTitle);
            }
        }

        // --- 2. Xử lý sự kiện nút X (btn_close) ---
        binding.btnClose.setOnClickListener(v -> {
            // Lệnh này sẽ đóng màn hình hiện tại và quay về màn hình trước đó
            navController.popBackStack();
        });

        // (Tùy chọn) Xử lý nút Menu (3 chấm) nếu cần
        binding.btnMenu.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Menu clicked", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Giải phóng binding để tránh rò rỉ bộ nhớ
    }

    @Override
    public void onResume() {
        super.onResume();
        // Khi màn hình này hiện lên -> TẮT thanh tiêu đề
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide();
            }
        }
    }
}