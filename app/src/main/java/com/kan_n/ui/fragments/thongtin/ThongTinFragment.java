package com.kan_n.ui.fragments.thongtin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.kan_n.R;
import com.kan_n.data.models.User;
import com.kan_n.databinding.FragmentThongtinBinding;
import com.kan_n.ui.activities.AuthActivity;

public class ThongTinFragment extends Fragment {

    private FragmentThongtinBinding binding;
    private ThongTinViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ThongTinViewModel.class);

        // Inflate layout sử dụng View Binding
        binding = FragmentThongtinBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // MỚI: Gọi hàm thiết lập nội dung cho các item menu
        setupMenuItems();

        // Tải dữ liệu người dùng
        loadUserData();

        // Thiết lập sự kiện click cho item "Cài đặt"
        binding.itemCaidat.getRoot().setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_thanhDieuHuong_ThongTin_to_TrangCaiDatFragment);
        });
        binding.txtUsername.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_thanhDieuHuong_ThongTin_to_infoUserFragment);
        });
        binding.imgAvatar.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_thanhDieuHuong_ThongTin_to_infoUserFragment);
        });
        // Thiết lập sự kiện click cho item "Đăng xuất"
        // (Giả sử bạn có item_dangxuat trong fragment_thongtin.xml)
        binding.itemDangxuat.getRoot().setOnClickListener(v -> {
            viewModel.logout();
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
          
        });

        // Bạn cũng có thể thêm sự kiện click cho các item khác ở đây
        // binding.itemKhonggianlamviec.getRoot().setOnClickListener(v -> { ... });
        // binding.itemHoidap.getRoot().setOnClickListener(v -> { ... });
    }

    /**
     * MỚI: Thiết lập văn bản và biểu tượng cho các item lựa chọn
     */
    private void setupMenuItems() {
        // 1. Thiết lập cho "Không gian làm việc"
        // (Giả sử bạn có file drawable tên là ic_khonggianlamviec)
        binding.itemKhonggianlamviec.iconMenu.setImageResource(R.drawable.ic_khonggianlamviec);
        binding.itemKhonggianlamviec.txtMenuTitle.setText("Không gian làm việc");

        // 2. Thiết lập cho "Cài đặt"
        binding.itemCaidat.iconMenu.setImageResource(R.drawable.ic_caidat);
        binding.itemCaidat.txtMenuTitle.setText("Cài đặt");

        // 3. Thiết lập cho "Hỏi đáp"
        // (Giả sử bạn có file drawable tên là ic_hoidap)
        binding.itemHoidap.iconMenu.setImageResource(R.drawable.ic_hoidap);
        binding.itemHoidap.txtMenuTitle.setText("Hỏi đáp");

        // 4. Thiết lập cho "Đăng xuất"
        // (Giả sử bạn có include-item với id "item_dangxuat" và drawable "ic_dangxuat")
        binding.itemDangxuat.iconMenu.setImageResource(R.drawable.ic_dangxuat);
        binding.itemDangxuat.txtMenuTitle.setText("Đăng xuất");

        // CHỈNH SỬA: Thay đổi icon của nút "Edit Profile" (btnProfileEdit)
        // ID này nằm trực tiếp trong fragment_thongtin.xml
        binding.btnProfileEdit.setImageResource(R.drawable.ic_menu_v1); // Ví dụ đổi sang icon khác
    }

    /**
     * Tải và hiển thị thông tin người dùng
     */
    private void loadUserData() {
        FirebaseUser firebaseUser = viewModel.getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            viewModel.getUserData(uid, task -> {
                if (task.isSuccessful()) {
                    User user = task.getResult();
                    if (user != null && getContext() != null) {
                        // Cập nhật giao diện
                        binding.txtUsername.setText(user.getDisplayName());
                        binding.txtEmail.setText(user.getEmail());

                        // Kiểm tra Avatar URL
                        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                            Glide.with(requireContext())
                                    .load(user.getAvatarUrl())
                                    .placeholder(R.drawable.logo)
                                    .error(R.drawable.logo)
                                    .into(binding.imgAvatar);
                        } else {
                            binding.imgAvatar.setImageResource(R.drawable.logo);
                        }
                    }
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