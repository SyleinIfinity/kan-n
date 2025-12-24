package com.kan_n.ui.fragments.card_space;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.models.Card;
import com.kan_n.data.models.CheckItem;
import com.kan_n.data.models.ListModel;
import com.kan_n.data.models.Tag;
import com.kan_n.databinding.FragmentCardSpaceBinding;
import com.kan_n.ui.adapters.adapter.AttachmentAdapter;
import com.kan_n.ui.adapters.adapter.ChecklistAdapter;
import com.kan_n.ui.adapters.adapter.ActivityAdapter;
import com.kan_n.data.models.Activity;
import com.kan_n.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class CardSpaceFragment extends Fragment {

    private CardSpaceViewModel viewModel;
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

    // --- Biến View cho phần mở rộng ---
    private TextView tvStartDateVal, tvDueDateVal;
    private RecyclerView rvAttachments, rvCheckList;
    private View btnAddCheckItem, btnUploadFile;
    private View layoutStartDate, layoutDueDate;

    private ChecklistAdapter checklistAdapter;
    private AttachmentAdapter attachmentAdapter;

    // Biến tạm để chọn ngày giờ
    private Calendar tempDate;
    private List<CheckItem> currentCheckList = new ArrayList<>(); // Lưu checklist hiện tại để cập nhật
    //
    private RecyclerView rvActivityLog;
    private ActivityAdapter activityAdapter;
    private List<Activity> activityList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCardSpaceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel và Firebase
        viewModel = new ViewModelProvider(this).get(CardSpaceViewModel.class);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        navController = Navigation.findNavController(view);

        // Gọi hàm setup giao diện mở rộng
        setupExtendedInfo();

        // --- 1. Nhận dữ liệu ---
        if (getArguments() != null) {
            mCardTitle = getArguments().getString("cardTitle");
            mCardId = getArguments().getString("cardId");
            mBoardId = getArguments().getString("boardId");

            if (mCardTitle != null) binding.tvTitle.setText(mCardTitle);
        }

        // --- 2. Load dữ liệu thẻ ---
        if (mCardId != null) {
            loadCardData();
            loadActivities();
        }

        binding.btnCreateSelfTag.setVisibility(View.GONE);

        if (mBoardId != null && mCardId != null) {
            checkIfCardInFirstListAndSetupUI();
        }

        // --- 3. Sự kiện ---
        binding.btnClose.setOnClickListener(v -> navController.popBackStack());

        // Nút Tạo/Sửa Tag
        binding.btnCreateSelfTag.setOnClickListener(v -> showAdvancedColorPickerDialog());
    }

    /**
     * Hàm thiết lập các View và Adapter cho phần thông tin mở rộng (Ngày, Checklist, File)
     */
    private void setupExtendedInfo() {
        if (binding == null) return;

        // Lấy view gốc từ thẻ include
        View includedInfo = binding.includedInfo.getRoot();

        View includedActivityView = binding.includedActivity.getRoot();
        rvActivityLog = includedActivityView.findViewById(R.id.rvActivityLog);

        // Ánh xạ View
        tvStartDateVal = includedInfo.findViewById(com.kan_n.R.id.tvStartDateVal);
        tvDueDateVal = includedInfo.findViewById(com.kan_n.R.id.tvDueDateVal);
        rvAttachments = includedInfo.findViewById(com.kan_n.R.id.rvAttachments);
        rvCheckList = includedInfo.findViewById(com.kan_n.R.id.rvCheckList);
        btnAddCheckItem = includedInfo.findViewById(com.kan_n.R.id.btnAddCheckItem);
        btnUploadFile = includedInfo.findViewById(com.kan_n.R.id.btnUploadFile);
        layoutStartDate = includedInfo.findViewById(com.kan_n.R.id.layoutStartDate);
        layoutDueDate = includedInfo.findViewById(com.kan_n.R.id.layoutDueDate);

        //Setup Adapter cho Log
        activityAdapter = new ActivityAdapter();
        LinearLayoutManager logLayoutManager = new LinearLayoutManager(getContext());

        logLayoutManager.setReverseLayout(true); // Đảo ngược để thấy tin mới nhất ở trên đầu
        logLayoutManager.setStackFromEnd(true);
        rvActivityLog.setLayoutManager(logLayoutManager);
        rvActivityLog.setAdapter(activityAdapter);

        // --- Setup Adapter Checklist ---
        checklistAdapter = new ChecklistAdapter(new ChecklistAdapter.OnCheckItemActionListener() {
            @Override
            public void onItemChecked(CheckItem item, int position, boolean isChecked) {
                if (position >= 0 && position < currentCheckList.size()) {
                    currentCheckList.get(position).setChecked(isChecked);
                    viewModel.updateCheckList(mCardId, currentCheckList);

                    // Ghi log hoạt động Check/Uncheck
                    String action = isChecked ? "Đã hoàn thành công việc: " : "Đã gỡ hoàn thành công việc: ";
                    viewModel.logActivity(mCardId, action + item.getTitle());

                    // Tải lại lịch sử
                    loadActivities();
                }
            }

            @Override
            public void onItemLongClicked(CheckItem item, int position) {

                showRenameCheckItemDialog(item, position);
            }
        });

        rvCheckList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCheckList.setAdapter(checklistAdapter);

        // --- Setup Adapter Attachment ---
        attachmentAdapter = new AttachmentAdapter();
        rvAttachments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAttachments.setAdapter(attachmentAdapter);

        // --- Các sự kiện Click ---

        // 1. Chọn ngày bắt đầu
        layoutStartDate.setOnClickListener(v -> showDateTimePicker(true));

        // 2. Chọn ngày kết thúc
        layoutDueDate.setOnClickListener(v -> showDateTimePicker(false));

        // 3. Thêm công việc
        btnAddCheckItem.setOnClickListener(v -> showAddCheckItemDialog());

        // 4. Chọn File
        btnUploadFile.setOnClickListener(v -> openFilePicker());
    }

    /**
     * Load dữ liệu thẻ từ Firebase và cập nhật giao diện
     */
    private void loadCardData() {
        mCardListener = mDatabase.child("cards").child(mCardId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Card card = snapshot.getValue(Card.class);
                if (card != null) {
                    // --- Xử lý phần Self Tag (Code cũ) ---
                    if (card.getSelfTagId() != null && !card.getSelfTagId().isEmpty()) {
                        currentSelfTagId = card.getSelfTagId();
                        loadTagDetails(currentSelfTagId);
                    } else {
                        currentSelfTagId = null;
                        resetUIForCreateMode();
                    }

                    // --- [QUAN TRỌNG] Cập nhật giao diện mở rộng ---
                    updateExtendedInfoUI(card);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Lỗi tải thẻ: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hàm cập nhật giao diện mở rộng dựa trên dữ liệu Card tải về
     */
    private void updateExtendedInfoUI(Card card) {
        // 1. Cập nhật ngày tháng
        tvStartDateVal.setText(formatDate(card.getStartDate()));
        tvDueDateVal.setText(formatDate(card.getDueDate()));

        // Kiểm tra quá hạn (Deadline)
        if (card.getDueDate() > 0 && System.currentTimeMillis() > card.getDueDate()) {
            tvDueDateVal.setTextColor(Color.RED);
            tvDueDateVal.setText(formatDate(card.getDueDate()) + " (Quá hạn)");
        } else {
            tvDueDateVal.setTextColor(Color.BLACK);
        }

        // 2. Cập nhật Checklist
        // Lưu ý: Firebase có thể trả về List hoặc HashMap tùy vào cách lưu
        // Ở đây ta giả sử ViewModel/Model đã xử lý convert về List
        if (card.getCheckList() != null) {
            currentCheckList = new ArrayList<>(card.getCheckList()); // Copy ra list mới để thao tác
            checklistAdapter.setItems(currentCheckList);
        } else {
            currentCheckList = new ArrayList<>();
            checklistAdapter.setItems(currentCheckList);
        }

        // 3. Cập nhật File đính kèm
        if (card.getAttachmentUrls() != null) {
            attachmentAdapter.setFiles(card.getAttachmentUrls());
            rvAttachments.setVisibility(View.VISIBLE);
        } else {
            rvAttachments.setVisibility(View.GONE);
        }
    }

    // --- CÁC HÀM XỬ LÝ LOGIC NGÀY GIỜ ---

    private void showDateTimePicker(boolean isStartDate) {
        if (getContext() == null) return;
        tempDate = Calendar.getInstance();

        // 1. Chọn Ngày
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            tempDate.set(Calendar.YEAR, year);
            tempDate.set(Calendar.MONTH, month);
            tempDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // 2. Chọn Giờ
            new TimePickerDialog(getContext(), (timeView, hourOfDay, minute) -> {
                tempDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                tempDate.set(Calendar.MINUTE, minute);

                long timestamp = tempDate.getTimeInMillis();

                // Gọi ViewModel lưu lên Firebase & Ghi Log
                if (isStartDate) {
                    viewModel.updateCardStartDate(mCardId, timestamp);
                    // [QUAN TRỌNG] Ghi lại lịch sử
                    viewModel.logActivity(mCardId, "Đã đặt ngày bắt đầu: " + formatDate(timestamp));
                } else {
                    viewModel.updateCardDueDate(mCardId, timestamp);
                    // [QUAN TRỌNG] Ghi lại lịch sử
                    viewModel.logActivity(mCardId, "Đã hẹn ngày kết thúc: " + formatDate(timestamp));
                }

                // Load lại log ngay lập tức để người dùng thấy
                loadActivities();

            }, tempDate.get(Calendar.HOUR_OF_DAY), tempDate.get(Calendar.MINUTE), true).show();

        }, tempDate.get(Calendar.YEAR), tempDate.get(Calendar.MONTH), tempDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String formatDate(long timestamp) {
        if (timestamp == 0) return "--/--/---- --:--";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // --- CÁC HÀM XỬ LÝ LOGIC CHECKLIST & FILE ---

    private void showAddCheckItemDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm công việc");

        final EditText input = new EditText(getContext());
        input.setHint("Nhập tên công việc...");
        builder.setView(input);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String taskName = input.getText().toString().trim();
            if (!taskName.isEmpty()) {
                CheckItem newItem = new CheckItem(taskName, false);
                viewModel.addChecklistItem(mCardId, newItem);

                // [QUAN TRỌNG] Ghi lại lịch sử
                viewModel.logActivity(mCardId, "Đã thêm công việc: " + taskName);

                loadActivities(); // Load lại log
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Cho phép chọn mọi loại file
        filePickerLauncher.launch(intent);
    }



    private final androidx.activity.result.ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        // Xử lý file (Code cũ chuyển vào đây)
                        String fileName = fileUri.getLastPathSegment();
                        Toast.makeText(getContext(), "Đã chọn file: " + fileName, Toast.LENGTH_SHORT).show();

                        // Ghi log và load lại
                        viewModel.logActivity(mCardId, "Đã tải lên tệp: " + fileName);
                        loadActivities();
                    }
                }
            });

    // --- CÁC HÀM CŨ (Tag, Permission) GIỮ NGUYÊN ---

    private void checkIfCardInFirstListAndSetupUI() {
        mDatabase.child("cards").child(mCardId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot cardSnap) {
                Card card = cardSnap.getValue(Card.class);
                if (card == null) return;
                String currentListId = card.getListId();

                mDatabase.child("lists").orderByChild("boardId").equalTo(mBoardId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot listSnap) {
                                ListModel firstList = null;
                                double minPos = Double.MAX_VALUE;

                                for (DataSnapshot ds : listSnap.getChildren()) {
                                    ListModel list = ds.getValue(ListModel.class);
                                    if (list != null) {
                                        if (list.getPosition() < minPos) {
                                            minPos = list.getPosition();
                                            firstList = list;
                                            firstList.setUid(ds.getKey());
                                        }
                                    }
                                }

                                if (firstList != null && firstList.getUid().equals(currentListId)) {
                                    isCardInFirstList = true;
                                    binding.btnCreateSelfTag.setVisibility(View.VISIBLE);
                                } else {
                                    isCardInFirstList = false;
                                    binding.btnCreateSelfTag.setVisibility(View.GONE);
                                }
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) {}
                        });
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

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
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateUIForEditMode() {
        if (binding == null) return;
        binding.btnCreateSelfTag.setText("Sửa Tag");
        try {
            binding.btnCreateSelfTag.setBackgroundColor(Color.parseColor(currentTagColor));
        } catch (Exception e) {
            binding.btnCreateSelfTag.setBackgroundColor(Color.BLUE);
        }
    }

    private void resetUIForCreateMode() {
        if (binding == null) return;
        binding.btnCreateSelfTag.setText("Tạo Tag Mới");
        binding.btnCreateSelfTag.setBackgroundColor(Color.parseColor("#2196F3"));
        currentTagName = "";
        currentTagColor = "#FF0000";
    }

    private void showAdvancedColorPickerDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        boolean isEditing = (currentSelfTagId != null);
        builder.setTitle(isEditing ? "Sửa Tag hiện tại" : "Tạo Tag mới");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etTagName = new EditText(getContext());
        etTagName.setHint("Nhập tên Tag...");
        etTagName.setText(currentTagName);
        layout.addView(etTagName);

        View colorPreview = new View(getContext());
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 200);
        previewParams.setMargins(0, 30, 0, 30);
        colorPreview.setLayoutParams(previewParams);

        GradientDrawable previewShape = new GradientDrawable();
        previewShape.setCornerRadius(20);

        int initialColor;
        try {
            initialColor = Color.parseColor(currentTagColor);
        } catch (Exception e) {
            initialColor = Color.RED;
        }
        previewShape.setColor(initialColor);
        colorPreview.setBackground(previewShape);
        layout.addView(colorPreview);

        TextView tvLabel = new TextView(getContext());
        tvLabel.setText("Kéo để đổi màu:");
        layout.addView(tvLabel);

        SeekBar colorSeekBar = new SeekBar(getContext());
        colorSeekBar.setMax(360);
        colorSeekBar.setPadding(20, 20, 20, 20);

        float[] hsvCurrent = new float[3];
        Color.colorToHSV(initialColor, hsvCurrent);
        colorSeekBar.setProgress((int) hsvCurrent[0]);

        GradientDrawable rainbow = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000});
        rainbow.setCornerRadius(10);
        colorSeekBar.setBackground(rainbow);
        colorSeekBar.setProgressDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        layout.addView(colorSeekBar);

        final String[] selectedColorHex = {currentTagColor};

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

        if (isEditing) {
            builder.setNeutralButton("Gỡ Tag", (dialog, which) -> removeSelfTag());
        } else {
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        }
        builder.show();
    }

    private void saveOrUpdateSelfTag(String tagName, String colorCode) {
        if (mCardId == null) return;
        String userId = (FirebaseAuth.getInstance().getCurrentUser() != null) ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> updates = new HashMap<>();
        String tagIdToSave;

        if (currentSelfTagId != null) {
            tagIdToSave = currentSelfTagId;
            updates.put("/tags/" + tagIdToSave + "/name", tagName);
            updates.put("/tags/" + tagIdToSave + "/color", colorCode);
            syncColorToSubscribers(tagIdToSave, colorCode);
        } else {
            tagIdToSave = db.child("tags").push().getKey();
            Tag newTag = new Tag(tagName, colorCode, userId);
            newTag.setUid(tagIdToSave);
            Map<String, Object> tagMap = newTag.toMap();
            if (mBoardId != null) tagMap.put("boardId", mBoardId);
            updates.put("/tags/" + tagIdToSave, tagMap);
        }

        updates.put("/cards/" + mCardId + "/selfTagId", tagIdToSave);
        updates.put("/cards/" + mCardId + "/labelColor", colorCode);

        db.updateChildren(updates).addOnSuccessListener(unused -> {
            if (getContext() != null) {
                String msg = (currentSelfTagId != null) ? "Đã cập nhật Tag" : "Đã tạo Tag mới";
                Toast.makeText(getContext(), msg + "!", Toast.LENGTH_SHORT).show();

                // Ghi log hoạt động
                viewModel.logActivity(mCardId, msg + ": " + tagName);
                loadActivities(); // Refresh log
            }
        }).addOnFailureListener(e -> {
            if (getContext() != null)
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void syncColorToSubscribers(String tagId, String newColor) {
        DatabaseReference cardsRef = FirebaseDatabase.getInstance().getReference("cards");
        cardsRef.orderByChild("assignedTagId").equalTo(tagId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Object> bulkUpdates = new HashMap<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String cardKey = ds.getKey();
                            bulkUpdates.put("/" + cardKey + "/labelColor", newColor);
                        }
                        if (!bulkUpdates.isEmpty()) {
                            cardsRef.updateChildren(bulkUpdates);
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void removeSelfTag() {
        if (mCardId == null || currentSelfTagId == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("/tags/" + currentSelfTagId, null);
        updates.put("/cards/" + mCardId + "/selfTagId", null);
        updates.put("/cards/" + mCardId + "/labelColor", "");

        mDatabase.updateChildren(updates).addOnSuccessListener(unused -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Đã gỡ Tag", Toast.LENGTH_SHORT).show();

                // Ghi log hoạt động
                viewModel.logActivity(mCardId, "Đã gỡ Tag khỏi thẻ");
                loadActivities(); // Refresh log
            }
        });
    }

    // --- HỘP THOẠI ĐỔI TÊN CÔNG VIỆC ---
    private void showRenameCheckItemDialog(CheckItem item, int position) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sửa tên công việc");

        final EditText input = new EditText(getContext());
        String oldName = item.getTitle(); // Lưu tên cũ để ghi log
        input.setText(oldName);
        input.setSelection(oldName.length());

        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 0, 50, 0);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                // 1. Cập nhật dữ liệu
                currentCheckList.get(position).setTitle(newName);
                viewModel.updateCheckList(mCardId, currentCheckList);

                // [MỚI] Ghi vào lịch sử
                viewModel.logActivity(mCardId, "Đã đổi tên công việc: \"" + oldName + "\" thành \"" + newName + "\"");
                loadActivities(); // Tải lại log ngay

                Toast.makeText(getContext(), "Đã cập nhật", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // --- HÀM TẢI LỊCH SỬ HOẠT ĐỘNG TỪ FIREBASE ---
    private void loadActivities() {
        if (mCardId == null) return;


        mDatabase.child("cards").child(mCardId).child("activities")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        activityList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            com.kan_n.data.models.Activity activity = ds.getValue(com.kan_n.data.models.Activity.class);
                            if (activity != null) {
                                activityList.add(activity);
                            }
                        }


                        if (activityAdapter != null) {
                            activityAdapter.setActivities(activityList);

                            // Tự động cuộn xuống dòng mới nhất (cuối danh sách)
                            if (!activityList.isEmpty()) {
                                rvActivityLog.smoothScrollToPosition(activityList.size() - 1);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mCardListener != null && mCardId != null) {
            mDatabase.child("cards").child(mCardId).removeEventListener(mCardListener);
        }
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
                ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
    }
}