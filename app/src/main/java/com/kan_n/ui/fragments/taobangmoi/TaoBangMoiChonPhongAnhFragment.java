package com.kan_n.ui.fragments.taobangmoi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.kan_n.R;
import com.kan_n.data.interfaces.BoardRepository;
import com.kan_n.data.models.Background;
import com.kan_n.databinding.FragmentTaoBangmoiChonphongAnhBinding;
import com.kan_n.ui.adapters.adapter.BackgroundAdapter;

import java.util.ArrayList;

public class TaoBangMoiChonPhongAnhFragment extends Fragment implements BackgroundAdapter.OnBackgroundClickListener {

    private FragmentTaoBangmoiChonphongAnhBinding binding;
    private TaoBangMoiViewModel viewModel;
    private BackgroundAdapter backgroundAdapter;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TaoBangMoiViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTaoBangmoiChonphongAnhBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        setupRecyclerView();
        observeViewModel();

        // ViewModel lọc và cung cấp danh sách "image"
        viewModel.filterBackgrounds("image");

        binding.ivBack.setOnClickListener(v -> navController.popBackStack());
    }

    private void setupRecyclerView() {
        backgroundAdapter = new BackgroundAdapter(getContext(), new ArrayList<>(), this);
        binding.rvAnh.setAdapter(backgroundAdapter);
    }

    private void observeViewModel() {
        viewModel.getFilteredBackgroundList().observe(getViewLifecycleOwner(), backgrounds -> {
            if (backgrounds != null) {
                backgroundAdapter.updateData(backgrounds);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sự kiện click vào một ô ảnh
     */
    @Override
    public void onBackgroundClick(Background background) {
        // Kiểm tra xem có đang ở chế độ chỉnh sửa (đổi nền) không
        boolean isEditing = getArguments() != null && getArguments().getBoolean("isEditing", false);
        String boardId = getArguments() != null ? getArguments().getString("boardId") : null;

        if (isEditing && boardId != null) {
            // TRƯỜNG HỢP: ĐỔI NỀN BẢNG HIỆN TẠI
            viewModel.updateBoardBackground(boardId, background, new BoardRepository.GeneralCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Đã cập nhật hình nền bảng", Toast.LENGTH_SHORT).show();
                    // Quay về MenuBangFragment
                    navController.popBackStack(R.id.MenuBangFragment, false);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // TRƯỜNG HỢP: CHỌN NỀN KHI TẠO BẢNG MỚI
            viewModel.selectBackground(background);
            // Quay về màn hình Tạo Bảng
            if (!navController.popBackStack(R.id.taoBangMoiFragment, false)) {
                navController.popBackStack();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}