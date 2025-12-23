package com.kan_n.ui.fragments.card_space;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kan_n.databinding.FragmentCardSpaceBinding;
import com.kan_n.data.models.Tag;

import java.util.HashMap;
import java.util.Map;

public class CardSpaceFragment extends Fragment {

    private FragmentCardSpaceBinding binding;
    private NavController navController;

    private String mCardId;
    private String mBoardId;
    private String mCardTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCardSpaceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // --- 1. Nhận dữ liệu ---
        if (getArguments() != null) {
            mCardTitle = getArguments().getString("cardTitle");
            mCardId = getArguments().getString("cardId");
            mBoardId = getArguments().getString("boardId");

            if (mCardTitle != null) {
                binding.tvTitle.setText(mCardTitle);
            }
        }

        // --- 2. Sự kiện ---
        binding.btnClose.setOnClickListener(v -> navController.popBackStack());

        // --- 3. Mở Dialog Tạo Tag Cải Tiến ---
        binding.btnCreateSelfTag.setOnClickListener(v -> showAdvancedColorPickerDialog());
    }

    /**
     * [CẢI TIẾN] Dialog chọn màu tự do từ dải màu Spectrum
     */
    private void showAdvancedColorPickerDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Tạo Tag & Chọn màu");

        // Layout chính của Dialog
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // 1. Ô nhập tên Tag
        final EditText etTagName = new EditText(getContext());
        etTagName.setHint("Nhập tên Tag...");
        layout.addView(etTagName);

        // 2. View hiển thị màu đã chọn (Preview)
        View colorPreview = new View(getContext());
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 200); // Chiều cao 200px
        previewParams.setMargins(0, 30, 0, 30);
        colorPreview.setLayoutParams(previewParams);
        // Bo tròn góc cho đẹp
        GradientDrawable previewShape = new GradientDrawable();
        previewShape.setCornerRadius(20);
        previewShape.setColor(Color.RED); // Màu mặc định
        colorPreview.setBackground(previewShape);
        layout.addView(colorPreview);

        // 3. Thanh trượt chọn màu (SeekBar Spectrum)
        TextView tvLabel = new TextView(getContext());
        tvLabel.setText("Kéo để chọn màu:");
        layout.addView(tvLabel);

        SeekBar colorSeekBar = new SeekBar(getContext());
        colorSeekBar.setMax(360); // Hue chạy từ 0 đến 360 độ
        colorSeekBar.setPadding(20, 20, 20, 20);

        // Tạo background cầu vồng cho SeekBar
        GradientDrawable rainbow = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {
                        0xFFFF0000, // Đỏ
                        0xFFFF00FF, // Tím
                        0xFF0000FF, // Xanh dương
                        0xFF00FFFF, // Cyan
                        0xFF00FF00, // Lá
                        0xFFFFFF00, // Vàng
                        0xFFFF0000  // Đỏ (lặp lại để khép vòng)
                });
        rainbow.setCornerRadius(10);
        colorSeekBar.setBackground(rainbow);

        // Ẩn thanh progress mặc định để hiện background cầu vồng
        colorSeekBar.setProgressDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        layout.addView(colorSeekBar);

        // Biến lưu màu hiện tại (Mặc định Đỏ)
        final int[] selectedColorInt = {Color.RED};
        final String[] selectedColorHex = {"#FF0000"};

        // Xử lý sự kiện kéo thanh trượt
        colorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Chuyển đổi giá trị 0-360 (Hue) thành mã màu
                float[] hsv = new float[3];
                hsv[0] = (float) progress; // Hue
                hsv[1] = 1.0f;             // Saturation (Bão hòa - Max)
                hsv[2] = 1.0f;             // Value (Độ sáng - Max)

                int color = Color.HSVToColor(hsv);
                selectedColorInt[0] = color;

                // Cập nhật View Preview
                previewShape.setColor(color);
                colorPreview.setBackground(previewShape);

                // Chuyển sang mã Hex để lưu Database
                selectedColorHex[0] = String.format("#%06X", (0xFFFFFF & color));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setView(layout);

        // Nút Lưu
        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String tagName = etTagName.getText().toString().trim();
            if (tagName.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên Tag", Toast.LENGTH_SHORT).show();
                return;
            }
            // Gọi hàm lưu với mã màu Hex vừa chọn từ thanh trượt
            saveSelfTagToFirebase(tagName, selectedColorHex[0]);
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void saveSelfTagToFirebase(String tagName, String colorCode) {
        if (mCardId == null) return;

        String currentUserId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        String newTagId = db.child("tags").push().getKey();

        Tag newTag = new Tag(tagName, colorCode, currentUserId);
        newTag.setUid(newTagId);

        Map<String, Object> updates = new HashMap<>();

        // Lưu Tag kèm boardId
        Map<String, Object> tagValues = newTag.toMap();
        if (mBoardId != null) tagValues.put("boardId", mBoardId);
        updates.put("/tags/" + newTagId, tagValues);

        // Cập nhật Card (Tag bản thân + Màu)
        updates.put("/cards/" + mCardId + "/selfTagId", newTagId);
        updates.put("/cards/" + mCardId + "/labelColor", colorCode);

        db.updateChildren(updates).addOnSuccessListener(unused -> {
            Toast.makeText(getContext(), "Đã cập nhật Tag màu mới!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide();
            }
        }
    }
}