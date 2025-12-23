package com.kan_n.ui.fragments.bang;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kan_n.R;
import com.kan_n.data.interfaces.BoardRepository;
import com.kan_n.data.models.Board;
import com.kan_n.data.repository.BoardRepositoryImpl;
import com.kan_n.databinding.FragmentBangBinding;
import com.kan_n.ui.adapters.adapter.BoardAdapter;
import com.kan_n.ui.adapters.adapter.WorkspaceAdapter;

import java.util.ArrayList;

public class BangFragment extends Fragment implements BoardAdapter.OnBoardClickListener {

    private FragmentBangBinding binding;
    private BangViewModel bangViewModel;
    private WorkspaceAdapter workspaceAdapter;
    private RecyclerView rvWorkspaces;
    private NavController navController;
    private BoardRepository boardRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bangViewModel = new ViewModelProvider(this).get(BangViewModel.class);
        binding = FragmentBangBinding.inflate(inflater, container, false);
        boardRepository = new BoardRepositoryImpl();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);
        setupRecyclerView();

        // 1. Quan sát danh sách Workspace để hiển thị
        bangViewModel.getWorkspaces().observe(getViewLifecycleOwner(), workspaces -> {
            if (workspaces != null) {
                workspaceAdapter.updateData(workspaces);
            }
        });

        // ✨ [THÊM MỚI] Quan sát ID Workspace vừa tìm được (FIX BUG ĐĂNG KÝ MỚI & USER CŨ)
        bangViewModel.getFoundActiveWorkspaceId().observe(getViewLifecycleOwner(), newId -> {
            if (newId != null && !newId.isEmpty()) {
                // Lưu ID đúng vào SharedPreferences ngay lập tức
                SharedPreferences prefs = requireActivity().getSharedPreferences("KanN_Prefs", Context.MODE_PRIVATE);
                prefs.edit().putString("active_ws_id", newId).apply();
            }
        });

        // Xử lý nhấn giữ
        workspaceAdapter.setOnBoardLongClickListener(this::showBoardPopupMenu);

        // Tạo bảng mới
        binding.btnTaoBangMoi.setOnClickListener(v -> {
            navController.navigate(R.id.action_bangFragment_to_taoBangMoiFragment);
        });

        bangViewModel.startListeningForChanges();
    }

    private void setupRecyclerView() {
        rvWorkspaces = binding.rvWorkspaces;
        workspaceAdapter = new WorkspaceAdapter(getContext(), new ArrayList<>(), this);
        rvWorkspaces.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWorkspaces.setAdapter(workspaceAdapter);
    }

    private void showBoardPopupMenu(View view, Board board) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenu().add(0, 1, 0, "Đổi tên bảng");
        popupMenu.getMenu().add(0, 2, 1, "Xóa bảng");

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    showRenameBoardDialog(board);
                    return true;
                case 2:
                    showDeleteBoardConfirm(board);
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.show();
    }

    private void showRenameBoardDialog(Board board) {
        EditText input = new EditText(getContext());
        input.setText(board.getName());
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(getContext())
                .setTitle("Đổi tên bảng")
                .setView(input)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        boardRepository.updateBoard(board.getUid(), newName, new BoardRepository.GeneralCallback() {
                            @Override
                            public void onSuccess() {
                                // ✨ [SỬA ĐỔI] Gọi loadDataSmart thay vì loadWorkspaces
                                bangViewModel.loadDataSmart();
                                Toast.makeText(getContext(), "Đã đổi tên bảng", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(getContext(), "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteBoardConfirm(Board board) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa bảng")
                .setMessage("Bạn có chắc chắn muốn xóa bảng '" + board.getName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boardRepository.deleteBoard(board.getUid(), new BoardRepository.GeneralCallback() {
                        @Override
                        public void onSuccess() {
                            // ✨ [SỬA ĐỔI] Gọi loadDataSmart
                            bangViewModel.loadDataSmart();
                            Toast.makeText(getContext(), "Đã xóa bảng", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(getContext(), "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onBoardClick(Board board) {
        if (navController != null && board != null) {
            Bundle args = new Bundle();
            args.putString("boardId", board.getUid());
            args.putString("boardTitle", board.getName());
            navController.navigate(R.id.action_bangFragment_to_bangSpaceFragment, args);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        // Lấy ID Workspace đang hoạt động từ SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("KanN_Prefs", Context.MODE_PRIVATE);
        String activeWsId = prefs.getString("active_ws_id", "ws_1_id");

        bangViewModel.setActiveWsId(activeWsId);
        // ✨ [SỬA ĐỔI] Gọi hàm thông minh
        bangViewModel.loadDataSmart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}