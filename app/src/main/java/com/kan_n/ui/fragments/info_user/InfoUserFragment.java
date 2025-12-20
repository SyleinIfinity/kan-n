package com.kan_n.ui.fragments.info_user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.kan_n.R; 
import com.kan_n.data.models.User;
import com.kan_n.databinding.FragmentInfoUserBinding;
import com.kan_n.ui.fragments.thongtin.ThongTinViewModel;

public class InfoUserFragment extends Fragment {
    private FragmentInfoUserBinding binding;
    // Khai báo ViewModel để sử dụng trong onViewCreated
    private ThongTinViewModel thongTinViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Lấy ViewModel chung
        thongTinViewModel = new ViewModelProvider(this).get(ThongTinViewModel.class);

        binding = FragmentInfoUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Tải và hiển thị dữ liệu người dùng
        loadUserData();

        // 2. Gắn listener cho nút 'Đổi ảnh' (ví dụ)
        binding.btnDoiAnh.setOnClickListener(v -> {
            // TODO: Triển khai logic chọn ảnh từ Gallery/Camera
        });

        // 3. Gắn listener cho nút 'Cập nhật'
        binding.btnCapnhat.setOnClickListener(v -> {
            // TODO: Triển khai logic lưu thông tin đã chỉnh sửa
        });
    }

    /**
     * Tải và hiển thị thông tin người dùng từ Firebase lên UI.
     */
    private void loadUserData() {
        FirebaseUser firebaseUser = thongTinViewModel.getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            thongTinViewModel.getUserData(uid, task -> {
                if (task.isSuccessful()) {
                    User user = task.getResult();
                    if (user != null && getContext() != null) {

                        // 1. Cập nhật Họ và tên
                        binding.edtHoten.setText(user.getDisplayName());

                        // 2. Cập nhật Email
                        binding.edtGmail.setText(user.getEmail());

                        // 3. Cập nhật Số điện thoại
                        // Kiểm tra null/rỗng vì trường này mới được bổ sung
                        String phone = user.getPhone();
                        binding.edtSdt.setText(phone != null ? phone : "");

                        // 4. Tải Avatar
                        String avatarUrl = user.getAvatarUrl();
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(avatarUrl)
                                    // Sử dụng ic_nguoi_dung làm placeholder/error
                                    .placeholder(R.drawable.ic_nguoi_dung)
                                    .error(R.drawable.ic_nguoi_dung)
                                    .into(binding.imgAvatar);
                        } else {
                            binding.imgAvatar.setImageResource(R.drawable.ic_nguoi_dung);
                        }

                    }
                } else {
                    // Xử lý lỗi tải dữ liệu
                    // Toast.makeText(getContext(), "Lỗi tải dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}