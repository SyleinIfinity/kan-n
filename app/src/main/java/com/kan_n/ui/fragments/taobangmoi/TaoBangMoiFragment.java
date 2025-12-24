package com.kan_n.ui.fragments.taobangmoi;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.kan_n.R;
import com.kan_n.data.models.Background;
import com.kan_n.data.models.Workspace;
import com.kan_n.databinding.FragmentTaobangMoiBinding;

public class TaoBangMoiFragment extends Fragment {

    private FragmentTaobangMoiBinding binding;
    private TaoBangMoiViewModel viewModel;
    private NavController navController;
    private ArrayAdapter<Workspace> workspaceAdapter;
    private String currentSelectedWorkspaceId = "";

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
        viewModel.resetCreateStatus();

        setupWorkspaceSpinner();
        setupBackgroundClickListeners();
        setupCreateButton();
        observeViewModel();
        viewModel.loadWorkspaces();
    }

    private void setupWorkspaceSpinner() {
        workspaceAdapter = new ArrayAdapter<Workspace>(getContext(), android.R.layout.simple_spinner_item) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView label = (TextView) super.getView(position, convertView, parent);
                Workspace workspace = getItem(position);
                if (workspace != null) {
                    label.setText(workspace.getName());
                }
                return label;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView label = (TextView) super.getDropDownView(position, convertView, parent);
                Workspace workspace = getItem(position);
                if (workspace != null) {
                    label.setText(workspace.getName());
                }
                return label;
            }
        };
        workspaceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerKhongGianLamViec.setAdapter(workspaceAdapter);
    }

    private void setupBackgroundClickListeners() {
        View.OnClickListener goToChonPhong = v -> {
            navController.navigate(R.id.action_taoBangMoiFragment_to_chonPhongFragment);
        };
        binding.tvPhongNenBangLabel.setOnClickListener(goToChonPhong);
        binding.tvSelectedColor.setOnClickListener(goToChonPhong);
        binding.viewColorIndicator.setOnClickListener(goToChonPhong);
    }

    private void setupCreateButton() {
        binding.btnTaoBang.setOnClickListener(v -> {
            String boardName = binding.etTenBang.getText().toString();
            Workspace selectedWorkspace = (Workspace) binding.spinnerKhongGianLamViec.getSelectedItem();
            String selectedVisibilityString = binding.spinnerQuyenXem.getSelectedItem().toString();
            Background selectedBackground = viewModel.getSelectedBackground().getValue();

            String visibility = mapVisibility(selectedVisibilityString);
            String workspaceId = (selectedWorkspace != null) ? selectedWorkspace.getUid() : null;

            // Lưu lại ID workspace đã chọn để gửi về BangFragment khi thành công
            if (workspaceId != null) {
                this.currentSelectedWorkspaceId = workspaceId;
            }

            viewModel.createBoard(boardName, workspaceId, visibility, selectedBackground);
        });
    }

    private void observeViewModel() {
        viewModel.getSelectedBackground().observe(getViewLifecycleOwner(), this::updateBackgroundPreview);

        viewModel.getWorkspaces().observe(getViewLifecycleOwner(), workspaces -> {
            if (workspaces != null) {
                workspaceAdapter.clear();
                workspaceAdapter.addAll(workspaces);
                workspaceAdapter.notifyDataSetChanged();

                if (getArguments() != null) {
                    String targetWsId = getArguments().getString("workspaceId");
                    if (targetWsId != null && !targetWsId.isEmpty()) {
                        for (int i = 0; i < workspaces.size(); i++) {
                            if (workspaces.get(i).getUid().equals(targetWsId)) {
                                binding.spinnerKhongGianLamViec.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getCreateStatus().observe(getViewLifecycleOwner(), status -> {
            if (status == null) return;
            switch (status) {
                case LOADING:
                    binding.btnTaoBang.setText("Đang tạo...");
                    binding.btnTaoBang.setEnabled(false);
                    break;
                case SUCCESS:
                    Toast.makeText(getContext(), "Tạo bảng thành công!", Toast.LENGTH_SHORT).show();

                    // Gửi kết quả về BangFragment
                    // Giống như callback onSuccess của Delete/Update: "Tôi đã làm xong, hãy reload đi"
                    Bundle result = new Bundle();
                    result.putBoolean("refresh_needed", true);
                    result.putString("target_workspace_id", currentSelectedWorkspaceId);
                    getParentFragmentManager().setFragmentResult("key_create_board", result);

                    navController.popBackStack();
                    break;
                case ERROR:
                    binding.btnTaoBang.setText("Tạo bảng");
                    binding.btnTaoBang.setEnabled(true);
                    break;
                case IDLE:
                    binding.btnTaoBang.setText("Tạo bảng");
                    binding.btnTaoBang.setEnabled(true);
                    break;
            }
        });
    }

    private String mapVisibility(String spinnerValue) {
        String[] options = getResources().getStringArray(R.array.quyen_xem_options);
        if (spinnerValue.equals(options[0])) return "private";
        else if (spinnerValue.equals(options[1])) return "workspace";
        else if (spinnerValue.equals(options[2])) return "public";
        return "private";
    }

    private void updateBackgroundPreview(Background background) {
        if (background == null || getContext() == null) return;
        binding.viewColorIndicator.setBackground(null);
        if ("color".equalsIgnoreCase(background.getType())) {
            try {
                int color = Color.parseColor(background.getValue());
                binding.viewColorIndicator.setBackgroundColor(color);
                binding.tvSelectedColor.setText(background.getValue().toUpperCase());
            } catch (Exception e) {}
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