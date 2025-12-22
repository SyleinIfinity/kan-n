package com.kan_n.ui.fragments.info_user;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class InfoUserFragment extends Fragment {

    private FragmentInfoUserBinding binding;
    private ThongTinViewModel thongTinViewModel;

    private boolean isEditing = false;
    private User currentUserData;

    // Spinner Adapter
    private final String[] genderOptions = {"Nam", "Nữ", "Khác"};
    private ArrayAdapter<String> genderAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        thongTinViewModel = new ViewModelProvider(this).get(ThongTinViewModel.class);
        binding = FragmentInfoUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupGenderSpinner();
        toggleEditMode(false);
        loadUserData();

        // 1. Sự kiện chọn ngày sinh
        binding.edtNgaysinh.setOnClickListener(v -> {
            // Chỉ hiện lịch khi đang ở chế độ Sửa
            if (isEditing) {
                showDatePickerDialog();
            }
        });

        binding.btnDoiAnh.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        binding.btnCapnhat.setOnClickListener(v -> {
            if (!isEditing) {
                toggleEditMode(true);
            } else {
                performUpdateUser();
            }
        });

        binding.btnHuy.setOnClickListener(v -> {
            toggleEditMode(false);
            if (currentUserData != null) {
                fillDataToUI(currentUserData);
                Toast.makeText(getContext(), "Đã hủy thay đổi", Toast.LENGTH_SHORT).show();
            } else {
                loadUserData();
            }
        });
    }

    /**
     * Hàm hiển thị Lịch để chọn ngày
     */
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        // Nếu trong ô EditText đã có ngày (ví dụ 05/04/2005), ta cố gắng parse để set ngày mặc định cho lịch
        String currentString = binding.edtNgaysinh.getText().toString();
        if (!currentString.isEmpty() && currentString.contains("/")) {
            try {
                String[] parts = currentString.split("/");
                day = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]) - 1; // Tháng trong Calendar bắt đầu từ 0
                year = Integer.parseInt(parts[2]);
            } catch (Exception e) {
                // Ignore parse error
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format lại thành chuỗi dd/MM/yyyy
                    String dateString = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    binding.edtNgaysinh.setText(dateString);
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void setupGenderSpinner() {
        genderAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, genderOptions);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGioitinh.setAdapter(genderAdapter);
    }

    private void toggleEditMode(boolean enable) {
        isEditing = enable;

        binding.edtHoten.setEnabled(enable);
        binding.edtGmail.setEnabled(false);
        binding.edtSdt.setEnabled(enable);

        // Ngày sinh: setEnabled(enable) để điều khiển việc có click được hay không
        // (Do XML đã set focusable=false nên click sẽ gọi listener chứ ko hiện phím)
        binding.edtNgaysinh.setEnabled(enable);

        binding.spinnerGioitinh.setEnabled(enable);

        if (enable) {
            binding.btnCapnhat.setText("Xác nhận");
            binding.btnHuy.setVisibility(View.VISIBLE);
            binding.edtHoten.requestFocus();
        } else {
            binding.btnCapnhat.setText("Cập nhật thông tin");
            binding.btnHuy.setVisibility(View.GONE);
        }
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = thongTinViewModel.getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            thongTinViewModel.getUserData(uid, task -> {
                if (task.isSuccessful()) {
                    User user = task.getResult();
                    if (user != null && getContext() != null) {
                        currentUserData = user;
                        fillDataToUI(user);
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fillDataToUI(User user) {
        binding.edtHoten.setText(user.getDisplayName());
        binding.edtGmail.setText(user.getEmail());

        String phone = user.getPhone();
        binding.edtSdt.setText(phone != null ? phone : "");

        // Load Ngày sinh
        String birth = user.getBirthDate();
        binding.edtNgaysinh.setText(birth != null ? birth : "");

        // Load Giới tính
        String gender = user.getGender();
        if (gender != null) {
            int spinnerPosition = genderAdapter.getPosition(gender);
            if (spinnerPosition >= 0) {
                binding.spinnerGioitinh.setSelection(spinnerPosition);
            }
        }

        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_nguoi_dung)
                    .error(R.drawable.ic_nguoi_dung)
                    .into(binding.imgAvatar);
        } else {
            binding.imgAvatar.setImageResource(R.drawable.ic_nguoi_dung);
        }
    }

    private void performUpdateUser() {
        FirebaseUser firebaseUser = thongTinViewModel.getCurrentUser();
        if (firebaseUser == null) return;
        String uid = firebaseUser.getUid();

        String newName = binding.edtHoten.getText().toString().trim();
        String newPhone = binding.edtSdt.getText().toString().trim();
        String newBirthDate = binding.edtNgaysinh.getText().toString().trim(); // Lấy ngày sinh

        String newGender = "";
        if (binding.spinnerGioitinh.getSelectedItem() != null) {
            newGender = binding.spinnerGioitinh.getSelectedItem().toString();
        }

        if (newName.isEmpty()) {
            binding.edtHoten.setError("Họ tên không được để trống");
            binding.edtHoten.requestFocus();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", newName);
        updates.put("phone", newPhone);
        updates.put("gender", newGender);
        updates.put("birthDate", newBirthDate); // Thêm vào danh sách cập nhật

        binding.btnCapnhat.setEnabled(false);
        binding.btnCapnhat.setText("Đang lưu...");

        String finalNewGender = newGender;

        thongTinViewModel.updateUserInfo(uid, updates, task -> {
            binding.btnCapnhat.setEnabled(true);

            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

                if (currentUserData != null) {
                    currentUserData.setDisplayName(newName);
                    currentUserData.setPhone(newPhone);
                    currentUserData.setGender(finalNewGender);
                    currentUserData.setBirthDate(newBirthDate); // Cập nhật cache
                }
                toggleEditMode(false);
            } else {
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Lỗi";
                Toast.makeText(getContext(), "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
                binding.btnCapnhat.setText("Xác nhận");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}