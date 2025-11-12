package com.kan_n.ui.fragments.bang;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment; // Thêm import này
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kan_n.R; // Thêm import này
import com.kan_n.databinding.FragmentBangBinding;
import com.kan_n.ui.adapters.adapter.WorkspaceAdapter;

import java.util.ArrayList; // Thêm import này

public class BangFragment extends Fragment {

    private FragmentBangBinding binding;

    private BangViewModel bangViewModel;
    private WorkspaceAdapter workspaceAdapter;
    private RecyclerView rvWorkspaces;
    private NavController navController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Khởi tạo ViewModel
        bangViewModel = new ViewModelProvider(this).get(BangViewModel.class);

        // Khởi tạo ViewBinding
        binding = FragmentBangBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Lấy tham chiếu đến RecyclerView chính
        rvWorkspaces = binding.rvWorkspaces;

        // Thiết lập Adapter (Từ phiên bản 2)
        setupRecyclerView();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy NavController (Từ phiên bản 1)
        navController = NavHostFragment.findNavController(this);

        // Gán sự kiện click cho nút FAB (Từ phiên bản 1)
        binding.btnTaoBangMoi.setOnClickListener(v -> {
            // Dùng action ID bạn đã định nghĩa trong nav graph
            // (Bạn cần chắc chắn đã tạo action này trong file navigation)
            navController.navigate(R.id.action_bangFragment_to_taoBangMoiFragment);
        });

        // Lắng nghe dữ liệu (Từ phiên bản 2)
        bangViewModel.getWorkspaces().observe(getViewLifecycleOwner(), workspaces -> {
            if (workspaces != null) {
                workspaceAdapter.updateData(workspaces);
            }
        });
    }

    /**
     * Khởi tạo RecyclerView và Adapter (Từ phiên bản 2)
     */
    private void setupRecyclerView() {
        // Khởi tạo Adapter với một danh sách rỗng ban đầu
        workspaceAdapter = new WorkspaceAdapter(getContext(), new ArrayList<>());

        // Thiết lập LayoutManager cho RecyclerView chính (chiều dọc)
        rvWorkspaces.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWorkspaces.setAdapter(workspaceAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh rò rỉ bộ nhớ
    }
}