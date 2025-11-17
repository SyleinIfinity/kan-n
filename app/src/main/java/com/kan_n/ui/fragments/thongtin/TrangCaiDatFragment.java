package com.kan_n.ui.fragments.thongtin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.kan_n.R;
import com.kan_n.databinding.FragmentTrangCaidatBinding;
import com.kan_n.ui.activities.AuthActivity;

public class TrangCaiDatFragment extends Fragment {
    private FragmentTrangCaidatBinding binding;
    private ThongTinViewModel thongTinViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        thongTinViewModel = new ViewModelProvider(this).get(ThongTinViewModel.class);

        binding = FragmentTrangCaidatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // MỚI: Gọi hàm thiết lập nội dung cho các item menu
        setupMenuItems();

        binding.itemDangxuat.getRoot().setOnClickListener(v -> {
            // 1. Gọi hàm logout từ ViewModel
            thongTinViewModel.logout();

            // 2. Chuyển về màn hình AuthActivity
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            // Xóa tất cả các Activity trước đó khỏi back stack
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // 3. Đóng Activity hiện tại (MainActivity)
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void setupMenuItems() {
        // 1. Thiết lập cho "Mở thiết lập hệ thống"
        binding.itemMothietlaphethong.tvTitle.setText("Mở thiết lập hệ ");
        // 2. Thiết lập cho "Đặt hẹn giờ ngủ"
        binding.itemDathengiongu.tvTitle.setText("Đặt hẹn giờ ngủ");

        // 3. Thiết lập cho "Chọn chủ đề"
        binding.itemChonchude.tvTitle.setText("Chọn chủ đề");
        // 4. Thiết lập cho "Phong cách"
        binding.itemPhongcach.tvTitle.setText("Phong cách");
        // 5. Thiết lập cho "Hồ sơ"
        binding.itemHoso.tvTitle.setText("Hồ sơ");
        // 6. Thiết lập cho "Ngôn ngữ"
        binding.itemNgonngu.tvTitle.setText("Ngôn ngữ");
        // 7. Thiết lập cho "Tìm hiểu về Kan-n"
        binding.itemTimhieuvekann.tvTitle.setText("Tìm hiểu về Kan-n");
        // 8. Thiết lập cho "Liên hệ hỗ trợ"
        binding.itemLienhehotro.tvTitle.setText("Liên hệ hỗ trợ");
        // 9. Thiết lập cho "Bảo mật & Quyền riêng tư"
        binding.itemBaomatvaquyenriengtu.tvTitle.setText("Bảo mật & Quyền riêng tư");
        // 10. Thiết lập cho "Kiểm tra lịch sử đăng nhập"
        binding.itemKiemtralichsudangnhap.tvTitle.setText("Kiểm tra lịch sử đăng nhập");
        // 11. Thiết lập cho "Xóa tàu khoản"
        binding.itemXoataikhoan.tvTitle.setText("Xóa tài khoản");
        // 12. Thiết lập cho "Đăng xuất"
        binding.itemDangxuat.tvTitle.setText("Đăng Xuất");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
