package com.kan_n.ui.fragments.thongtin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.R;
import com.kan_n.data.models.User;
import com.kan_n.databinding.FragmentThongtinBinding;

public class ThongTinFragment extends Fragment {

    private ThongTinViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_thongtin, container, false);

        // Thuộc tính User
        TextView txtUsername = root.findViewById(R.id.txtUsername);
        TextView txtEmail = root.findViewById(R.id.txtEmail);
        ImageView imgAvatar = root.findViewById(R.id.imgAvatar);

        // Tên các lựa chọn

        // Lựa chọn Không gian làm việc
        LinearLayout itemKhongGianLamViec = root.findViewById(R.id.item_khonggianlamviec);
        ImageView iconKhongGianLamViec = itemKhongGianLamViec.findViewById(R.id.iconMenu);
        TextView titleKhongGianLamViec = itemKhongGianLamViec.findViewById(R.id.txtMenuTitle);

        iconKhongGianLamViec.setImageResource(R.drawable.ic_khonggianlamviec);
        titleKhongGianLamViec.setText("Không gian làm việc");

        // Lựa chọn Cài Đặt
        LinearLayout itemCaiDat = root.findViewById(R.id.item_caidat);
        ImageView iconCaiDat = itemCaiDat.findViewById(R.id.iconMenu);
        TextView titleCaiDat = itemCaiDat.findViewById(R.id.txtMenuTitle);

        iconCaiDat.setImageResource(R.drawable.ic_caidat);
        titleCaiDat.setText("Cài đặt");

        // Lựa chọn Hỏi Đáp
        LinearLayout itemHoiDap = root.findViewById(R.id.item_hoidap);
        ImageView iconHoiDap = itemHoiDap.findViewById(R.id.iconMenu);
        TextView titleHoiDap = itemHoiDap.findViewById(R.id.txtMenuTitle);

        iconHoiDap.setImageResource(R.drawable.ic_hoidap);
        titleHoiDap.setText("Hỏi đáp");

        // Lựa chọn Đăng Xuất
        LinearLayout itemDangXuat = root.findViewById(R.id.item_dangxuat);
        ImageView iconDangXuat = itemDangXuat.findViewById(R.id.iconMenu);
        TextView titleDangXuat = itemDangXuat.findViewById(R.id.txtMenuTitle);

        iconDangXuat.setImageResource(R.drawable.ic_dangxuat);
        titleDangXuat.setText("Đăng xuất");



        viewModel = new ViewModelProvider(this).get(ThongTinViewModel.class);

        // Quan sát LiveData
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                txtUsername.setText(user.getDisplayName());
                txtEmail.setText(user.getEmail());

                // Load avatar (nếu dùng Glide)
                Glide.with(this)
                        .load(user.getAvatarUrl())
                        .placeholder(R.drawable.ic_caidat)
                        .into(imgAvatar);
            }
        });

        // Lấy dữ liệu từ Firebase
        loadUserData();

        return root;
    }

    private void loadUserData() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                .child("DhEoy760JAeQ2EEWjR1zpFTBfby2"); // ID người dùng

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                viewModel.setUser(user);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
}
