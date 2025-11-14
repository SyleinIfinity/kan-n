package com.kan_n.ui.fragments.taobangmoi;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter; // ✨ Bổ sung
import android.widget.TextView; // ✨ Bổ sung
import android.widget.Toast; // ✨ Bổ sung

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.kan_n.R;
import com.kan_n.data.models.Background;
import com.kan_n.data.models.Workspace; // ✨ Bổ sung
import com.kan_n.databinding.FragmentTaobangMoiBinding;

import java.util.List; // ✨ Bổ sung

public class TaoBangMoiFragment extends Fragment {

    private FragmentTaobangMoiBinding binding;
    private TaoBangMoiViewModel viewModel;
    private NavController navController;

    // ✨ Adapter cho Spinner Workspace
    private ArrayAdapter<Workspace> workspaceAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TaoBangMoiViewModel.class);
        viewModel.loadAllBackgrounds();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTaobangMoiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        viewModel.resetCreateStatus(); // Reset trạng thái khi vào màn hình

        // 1. Cài đặt Spinner Workspace
        setupWorkspaceSpinner();

        // 2. Gắn listener (đã có từ trước)
        setupBackgroundClickListeners();

        // 3. Gắn listener cho nút TẠO BẢNG
        setupCreateButton();

        // 4. Lắng nghe (Observe) dữ liệu
        observeViewModel();

        // 5. Yêu cầu ViewModel tải Workspaces
        viewModel.loadWorkspaces();
    }

    /**
     * Cài đặt Adapter tùy chỉnh cho Spinner Workspace.
     * Cần tùy chỉnh để hiển thị tên (workspace.getName()) thay vì object hashcode.
     */
    private void setupWorkspaceSpinner() {
        workspaceAdapter = new ArrayAdapter<Workspace>(getContext(), android.R.layout.simple_spinner_item) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView label = (TextView) super.getView(position, convertView, parent);
                Workspace workspace = getItem(position);
                if (workspace != null) {
                    label.setText(workspace.getName()); // Hiển thị tên
                }
                return label;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView label = (TextView) super.getDropDownView(position, convertView, parent);
                Workspace workspace = getItem(position);
                if (workspace != null) {
                    label.setText(workspace.getName()); // Hiển thị tên trong danh sách dropdown
                }
                return label;
            }
        };
        workspaceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerKhongGianLamViec.setAdapter(workspaceAdapter);
    }

    /**
     * Gắn sự kiện click cho ô chọn phông nền
     */
    private void setupBackgroundClickListeners() {
        View.OnClickListener goToChonPhong = v -> {
            navController.navigate(R.id.action_taoBangMoiFragment_to_chonPhongFragment);
        };
        binding.tvPhongNenBangLabel.setOnClickListener(goToChonPhong);
        binding.tvSelectedColor.setOnClickListener(goToChonPhong);
        binding.viewColorIndicator.setOnClickListener(goToChonPhong);
    }

    /**
     * Gắn sự kiện click cho nút "Tạo bảng"
     */
    private void setupCreateButton() {
        binding.btnTaoBang.setOnClickListener(v -> {
            // 1. Lấy dữ liệu từ UI
            String boardName = binding.etTenBang.getText().toString();
            Workspace selectedWorkspace = (Workspace) binding.spinnerKhongGianLamViec.getSelectedItem();
            String selectedVisibilityString = binding.spinnerQuyenXem.getSelectedItem().toString();

            // 2. Lấy dữ liệu đã chọn từ ViewModel
            Background selectedBackground = viewModel.getSelectedBackground().getValue();

            // 3. Xử lý dữ liệu
            String visibility = mapVisibility(selectedVisibilityString);
            String workspaceId = (selectedWorkspace != null) ? selectedWorkspace.getUid() : null;

            // 4. Gọi ViewModel
            viewModel.createBoard(boardName, workspaceId, visibility, selectedBackground);
        });
    }

    /**
     * Lắng nghe LiveData từ ViewModel
     */
    private void observeViewModel() {
        // Lắng nghe phông nền (từ trước)
        viewModel.getSelectedBackground().observe(getViewLifecycleOwner(), this::updateBackgroundPreview);

        // ✨ Lắng nghe danh sách Workspace
        viewModel.getWorkspaces().observe(getViewLifecycleOwner(), workspaces -> {
            if (workspaces != null) {
                workspaceAdapter.clear();
                workspaceAdapter.addAll(workspaces);
                workspaceAdapter.notifyDataSetChanged();
            }
        });

        // Lắng nghe Lỗi
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });

        // ✨ Lắng nghe trạng thái TẠO BẢNG
        viewModel.getCreateStatus().observe(getViewLifecycleOwner(), status -> {
            if (status == null) return;
            switch (status) {
                case LOADING:
                    binding.btnTaoBang.setText("Đang tạo...");
                    binding.btnTaoBang.setEnabled(false);
                    break;
                case SUCCESS:
                    Toast.makeText(getContext(), "Tạo bảng thành công!", Toast.LENGTH_SHORT).show();
                    navController.popBackStack(); // Quay lại màn hình BangFragment
                    break;
                case ERROR:
                    binding.btnTaoBang.setText("Tạo bảng");
                    binding.btnTaoBang.setEnabled(true);
                    // (Toast lỗi đã được xử lý bởi observer _error)
                    break;
                case IDLE:
                    binding.btnTaoBang.setText("Tạo bảng");
                    binding.btnTaoBang.setEnabled(true);
                    break;
            }
        });
    }

    /**
     * Hàm tiện ích: Chuyển đổi chuỗi từ Spinner sang giá trị lưu trữ
     * (Ví dụ: "Riêng tư (Chỉ thành viên)" -> "private")
     */
    private String mapVisibility(String spinnerValue) {
        // Lấy mảng gốc từ strings.xml
        String[] options = getResources().getStringArray(R.array.quyen_xem_options);

        if (spinnerValue.equals(options[0])) { // "Riêng tư (Chỉ thành viên)"
            return "private"; // (Giả sử bạn dùng "private", "workspace", "public")
        } else if (spinnerValue.equals(options[1])) { // "Không gian làm việc (Mọi thành viên)"
            return "workspace";
        } else if (spinnerValue.equals(options[2])) { // "Công khai (Bất kỳ ai)"
            return "public";
        }
        return "private"; // Mặc định
    }

    /**
     * Cập nhật UI phông nền (từ trước)
     */
    private void updateBackgroundPreview(Background background) {
        // (Code này giữ nguyên như đã cung cấp ở lần trước)
        if (background == null || getContext() == null) return;
        binding.viewColorIndicator.setBackground(null);
        if ("color".equalsIgnoreCase(background.getType())) {
            try {
                int color = Color.parseColor(background.getValue());
                binding.viewColorIndicator.setBackgroundColor(color);
                binding.tvSelectedColor.setText(background.getValue().toUpperCase());
            } catch (Exception e) { /*...*/}
        } else if ("image".equalsIgnoreCase(background.getType())) {
            binding.tvSelectedColor.setText("Ảnh");
            Glide.with(getContext())
                    .load(background.getValue())
                    .centerCrop()
                    .placeholder(new ColorDrawable(Color.LTGRAY))
                    .into(new com.bumptech.glide.request.target.CustomViewTarget<View, android.graphics.drawable.Drawable>(binding.viewColorIndicator) {
                        @Override
                        public void onResourceReady(@NonNull android.graphics.drawable.Drawable resource, @Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.drawable.Drawable> transition) {
                            binding.viewColorIndicator.setBackground(resource);
                        }
                        @Override
                        protected void onResourceCleared(@Nullable android.graphics.drawable.Drawable placeholder) {
                            binding.viewColorIndicator.setBackground(placeholder);
                        }
                        @Override
                        public void onLoadFailed(@Nullable android.graphics.drawable.Drawable errorDrawable) {
                            binding.viewColorIndicator.setBackground(errorDrawable);
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