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
import com.kan_n.databinding.FragmentTaoBangmoiChonphongMauBinding;
import com.kan_n.ui.adapters.adapter.BackgroundAdapter;

import java.util.ArrayList;

public class TaoBangMoiChonPhongMauFragment extends Fragment implements BackgroundAdapter.OnBackgroundClickListener {

    private FragmentTaoBangmoiChonphongMauBinding binding;
    private TaoBangMoiViewModel viewModel;
    private BackgroundAdapter backgroundAdapter;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy ViewModel chung
        viewModel = new ViewModelProvider(requireActivity()).get(TaoBangMoiViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTaoBangmoiChonphongMauBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        setupRecyclerView();
        observeViewModel();

        // ViewModel lọc và cung cấp danh sách "color"
        viewModel.filterBackgrounds("color");

        binding.ivBack.setOnClickListener(v -> navController.popBackStack());
    }

    private void setupRecyclerView() {
        backgroundAdapter = new BackgroundAdapter(getContext(), new ArrayList<>(), this);
        binding.rvMauSac.setAdapter(backgroundAdapter);
    }

    private void observeViewModel() {
        // Lắng nghe danh sách ĐÃ LỌC
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
     * Sự kiện click vào một ô màu
     */
    @Override
    public void onBackgroundClick(Background background) {
        boolean isEditing = getArguments() != null && getArguments().getBoolean("isEditing", false);
        String boardId = getArguments() != null ? getArguments().getString("boardId") : null;

        if (isEditing && boardId != null) {
            // TRƯỜNG HỢP: ĐỔI NỀN BẢNG HIỆN TẠI
            viewModel.updateBoardBackground(boardId, background, new BoardRepository.GeneralCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Đã cập nhật nền bảng", Toast.LENGTH_SHORT).show();
                    // Quay về Menu hoặc màn hình chi tiết bảng
                    navController.popBackStack(R.id.MenuBangFragment, false);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // TRƯỜNG HỢP: TẠO BẢNG MỚI
            viewModel.selectBackground(background);
            // Kiểm tra xem có Fragment tạo bảng trong stack không để pop cho đúng
            if (!navController.popBackStack(R.id.taoBangMoiFragment, false)) {
                navController.popBackStack(); // Nếu không có thì chỉ lùi lại 1 bước
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}