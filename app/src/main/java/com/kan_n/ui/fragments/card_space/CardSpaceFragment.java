package com.kan_n.ui.fragments.card_space;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.models.ListModel;
import com.kan_n.databinding.FragmentCardSpaceBinding;
import com.kan_n.data.models.Card;
import com.kan_n.data.models.Tag;

import java.util.HashMap;
import java.util.Map;

public class CardSpaceFragment extends Fragment {

    private FragmentCardSpaceBinding binding;
    private NavController navController;

    private String mCardId;
    private String mBoardId;
    private String mCardTitle;

    // Biến lưu thông tin Tag hiện tại (nếu có)
    private String currentSelfTagId = null;
    private String currentTagName = "";
    private String currentTagColor = "#FF0000"; // Mặc định đỏ

    private DatabaseReference mDatabase;
    private ValueEventListener mCardListener;

    private boolean isCardInFirstList = false;

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
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // --- 1. Nhận dữ liệu ---
        if (getArguments() != null) {
            mCardTitle = getArguments().getString("cardTitle");
            mCardId = getArguments().getString("cardId");
            mBoardId = getArguments().getString("boardId");

            if (mCardTitle != null) binding.tvTitle.setText(mCardTitle);
        }

        // --- 2. Load dữ liệu thẻ & Tag ---
        if (mCardId != null) {
            loadCardData();
        }

        binding.btnCreateSelfTag.setVisibility(View.GONE);

        if (mBoardId != null && mCardId != null) {
            loadCardData(); // Code cũ
            checkIfCardInFirstListAndSetupUI(); // [MỚI] Gọi hàm kiểm tra quyền
        }

        // --- 3. Sự kiện ---
        binding.btnClose.setOnClickListener(v -> navController.popBackStack());

        // Nút Tạo/Sửa Tag
        binding.btnCreateSelfTag.setOnClickListener(v -> showAdvancedColorPickerDialog());
    }

    private void checkIfCardInFirstListAndSetupUI() {
        // 1. Lấy thông tin thẻ để biết nó nằm ở list nào (listId)
        mDatabase.child("cards").child(mCardId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot cardSnap) {
                Card card = cardSnap.getValue(Card.class);
                if (card == null) return;
                String currentListId = card.getListId();

                // 2. Lấy tất cả danh sách trong Bảng để tìm ra danh sách đầu tiên
                mDatabase.child("lists").orderByChild("boardId").equalTo(mBoardId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot listSnap) {
                                ListModel firstList = null;
                                double minPos = Double.MAX_VALUE;

                                for (DataSnapshot ds : listSnap.getChildren()) {
                                    ListModel list = ds.getValue(ListModel.class);
                                    if (list != null) {
                                        // Tìm list có position nhỏ nhất
                                        if (list.getPosition() < minPos) {
                                            minPos = list.getPosition();
                                            firstList = list;
                                            firstList.setUid(ds.getKey());
                                        }
                                    }
                                }

                                // 3. So sánh
                                if (firstList != null && firstList.getUid().equals(currentListId)) {
                                    // Đây là thẻ thuộc DS1 -> ĐƯỢC QUYỀN TẠO/SỬA TAG
                                    isCardInFirstList = true;
                                    binding.btnCreateSelfTag.setVisibility(View.VISIBLE);
                                } else {
                                    // Thẻ ở DS2, DS3... -> KHÔNG ĐƯỢC QUYỀN TẠO TAG
                                    isCardInFirstList = false;
                                    binding.btnCreateSelfTag.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * [MỚI] Load dữ liệu thẻ để biết đã có Tag hay chưa
     */
    private void loadCardData() {
        mCardListener = mDatabase.child("cards").child(mCardId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Card card = snapshot.getValue(Card.class);
                if (card != null) {
                    // Kiểm tra xem thẻ có Self Tag không
                    if (card.getSelfTagId() != null && !card.getSelfTagId().isEmpty()) {
                        // Đã có Tag -> Chuyển sang chế độ "Sửa"
                        currentSelfTagId = card.getSelfTagId();
                        loadTagDetails(currentSelfTagId);
                    } else {
                        // Chưa có Tag -> Chế độ "Tạo mới"
                        currentSelfTagId = null;
                        resetUIForCreateMode();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải thẻ: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Load chi tiết Tag (Tên, Màu) từ node "tags"
     */
    private void loadTagDetails(String tagId) {
        mDatabase.child("tags").child(tagId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Tag tag = snapshot.getValue(Tag.class);
                if (tag != null) {
                    currentTagName = tag.getName();
                    currentTagColor = tag.getColor();
                    updateUIForEditMode();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateUIForEditMode() {
        if (binding == null) return;
        binding.btnCreateSelfTag.setText("Sửa Tag");
        // Có thể đổi màu nút theo màu Tag để đẹp hơn
        try {
            binding.btnCreateSelfTag.setBackgroundColor(Color.parseColor(currentTagColor));
        } catch (Exception e) {
            binding.btnCreateSelfTag.setBackgroundColor(Color.BLUE);
        }
    }

    private void resetUIForCreateMode() {
        if (binding == null) return;
        binding.btnCreateSelfTag.setText("Tạo Tag Mới");
        binding.btnCreateSelfTag.setBackgroundColor(Color.parseColor("#2196F3")); // Màu mặc định
        currentTagName = "";
        currentTagColor = "#FF0000";
    }

    /**
     * Dialog chọn màu & tên (Đã cải tiến Logic Pre-fill dữ liệu cũ)
     */
    private void showAdvancedColorPickerDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        boolean isEditing = (currentSelfTagId != null);
        builder.setTitle(isEditing ? "Sửa Tag hiện tại" : "Tạo Tag mới");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // 1. Ô nhập tên Tag (Điền sẵn nếu đang sửa)
        final EditText etTagName = new EditText(getContext());
        etTagName.setHint("Nhập tên Tag...");
        etTagName.setText(currentTagName); // Pre-fill
        layout.addView(etTagName);

        // 2. View Preview
        View colorPreview = new View(getContext());
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 200);
        previewParams.setMargins(0, 30, 0, 30);
        colorPreview.setLayoutParams(previewParams);

        GradientDrawable previewShape = new GradientDrawable();
        previewShape.setCornerRadius(20);

        // Parse màu hiện tại (hoặc mặc định)
        int initialColor;
        try {
            initialColor = Color.parseColor(currentTagColor);
        } catch (Exception e) {
            initialColor = Color.RED;
        }
        previewShape.setColor(initialColor);
        colorPreview.setBackground(previewShape);
        layout.addView(colorPreview);

        // 3. SeekBar Spectrum
        TextView tvLabel = new TextView(getContext());
        tvLabel.setText("Kéo để đổi màu:");
        layout.addView(tvLabel);

        SeekBar colorSeekBar = new SeekBar(getContext());
        colorSeekBar.setMax(360);
        colorSeekBar.setPadding(20, 20, 20, 20);

        // Tính toán vị trí SeekBar dựa trên màu hiện tại (để thanh trượt nằm đúng chỗ màu cũ)
        float[] hsvCurrent = new float[3];
        Color.colorToHSV(initialColor, hsvCurrent);
        colorSeekBar.setProgress((int) hsvCurrent[0]); // Set vị trí thanh trượt theo Hue

        GradientDrawable rainbow = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000});
        rainbow.setCornerRadius(10);
        colorSeekBar.setBackground(rainbow);
        colorSeekBar.setProgressDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        layout.addView(colorSeekBar);

        final String[] selectedColorHex = {currentTagColor};

        // Listener SeekBar
        colorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float[] hsv = new float[3];
                hsv[0] = (float) progress;
                hsv[1] = 1.0f;
                hsv[2] = 1.0f;
                int color = Color.HSVToColor(hsv);

                previewShape.setColor(color);
                colorPreview.setBackground(previewShape);
                selectedColorHex[0] = String.format("#%06X", (0xFFFFFF & color));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setView(layout);

        builder.setPositiveButton(isEditing ? "Cập nhật" : "Tạo mới", (dialog, which) -> {
            String tagName = etTagName.getText().toString().trim();
            if (tagName.isEmpty()) {
                Toast.makeText(getContext(), "Tên Tag không được rỗng", Toast.LENGTH_SHORT).show();
                return;
            }
            saveOrUpdateSelfTag(tagName, selectedColorHex[0]);
        });

        // Nút xóa Tag (chỉ hiện khi đang sửa)
        if (isEditing) {
            builder.setNeutralButton("Gỡ Tag", (dialog, which) -> removeSelfTag());
        } else {
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        }

        builder.show();
    }

    /**
     * Logic Lưu hoặc Cập nhật Tag
     */
    private void saveOrUpdateSelfTag(String tagName, String colorCode) {
        if (mCardId == null) return;
        String userId = (FirebaseAuth.getInstance().getCurrentUser() != null) ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> updates = new HashMap<>();
        String tagIdToSave;

        if (currentSelfTagId != null) {
            // --- LOGIC SỬA (Update) ---
            tagIdToSave = currentSelfTagId;
            updates.put("/tags/" + tagIdToSave + "/name", tagName);
            updates.put("/tags/" + tagIdToSave + "/color", colorCode);

            // [MỚI - QUAN TRỌNG] Đồng bộ màu cho TẤT CẢ thẻ khác đang dùng Tag này
            syncColorToSubscribers(tagIdToSave, colorCode);

        } else {
            // --- LOGIC TẠO MỚI (Create) ---
            tagIdToSave = db.child("tags").push().getKey();
            Tag newTag = new Tag(tagName, colorCode, userId);
            newTag.setUid(tagIdToSave);
            Map<String, Object> tagMap = newTag.toMap();
            if (mBoardId != null) tagMap.put("boardId", mBoardId);
            updates.put("/tags/" + tagIdToSave, tagMap);
        }

        // Cập nhật chính thẻ này
        updates.put("/cards/" + mCardId + "/selfTagId", tagIdToSave);
        updates.put("/cards/" + mCardId + "/labelColor", colorCode);

        db.updateChildren(updates).addOnSuccessListener(unused -> {
            Toast.makeText(getContext(), currentSelfTagId != null ? "Đã cập nhật Tag!" : "Đã tạo Tag mới!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void syncColorToSubscribers(String tagId, String newColor) {
        DatabaseReference cardsRef = FirebaseDatabase.getInstance().getReference("cards");

        // Tìm tất cả thẻ có assignedTagId == tagId
        cardsRef.orderByChild("assignedTagId").equalTo(tagId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Object> bulkUpdates = new HashMap<>();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String cardKey = ds.getKey();
                            // Tạo path update cho từng thẻ
                            bulkUpdates.put("/" + cardKey + "/labelColor", newColor);
                        }

                        if (!bulkUpdates.isEmpty()) {
                            cardsRef.updateChildren(bulkUpdates);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Log lỗi nếu cần
                    }
                });
    }

    /**
     * Logic Xóa/Gỡ Tag
     */
    private void removeSelfTag() {
        if (mCardId == null || currentSelfTagId == null) return;

        // Bạn có thể chọn: Xóa hẳn tag trong node 'tags' hay chỉ gỡ khỏi card?
        // Theo logic "Tag bản thân" (1-1), nên xóa luôn Tag trong node tags để không rác DB

        Map<String, Object> updates = new HashMap<>();
        updates.put("/tags/" + currentSelfTagId, null); // Xóa tag
        updates.put("/cards/" + mCardId + "/selfTagId", null); // Gỡ khỏi card
        updates.put("/cards/" + mCardId + "/labelColor", ""); // Xóa màu

        mDatabase.updateChildren(updates).addOnSuccessListener(unused -> {
            Toast.makeText(getContext(), "Đã gỡ Tag", Toast.LENGTH_SHORT).show();
            // UI sẽ tự reset nhờ loadCardData lắng nghe
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Gỡ listener để tránh memory leak
        if (mCardListener != null && mCardId != null) {
            mDatabase.child("cards").child(mCardId).removeEventListener(mCardListener);
        }
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
    }
}