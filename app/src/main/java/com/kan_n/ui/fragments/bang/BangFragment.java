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
    private BoardRepository boardRepository; // Thêm repository để xử lý data

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bangViewModel = new ViewModelProvider(this).get(BangViewModel.class);
        binding = FragmentBangBinding.inflate(inflater, container, false);
        boardRepository = new BoardRepositoryImpl(); // Khởi tạo Repository

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo NavController
        navController = NavHostFragment.findNavController(this);

        setupRecyclerView();

        // Quan sát LiveData từ ViewModel để cập nhật giao diện
        bangViewModel.getWorkspaces().observe(getViewLifecycleOwner(), workspaces -> {
            if (workspaces != null) {
                workspaceAdapter.updateData(workspaces);
            }
        });

        // XỬ LÝ NHẤN GIỮ VÀO BẢNG
        workspaceAdapter.setOnBoardLongClickListener(new BoardAdapter.OnBoardLongClickListener() {
            @Override
            public void onBoardLongClick(View view, Board board) {
                showBoardPopupMenu(view, board);
            }
        });
        // XỬ LÝ TẠO BẢNG MỚI
        binding.btnTaoBangMoi.setOnClickListener(v -> {
            // Sử dụng Action ID đã định nghĩa trong mobile_thanh_dieu_huong.xml
            navController.navigate(R.id.action_bangFragment_to_taoBangMoiFragment);
        });

        bangViewModel.startListeningForChanges();
    }

    private void setupRecyclerView() {
        rvWorkspaces = binding.rvWorkspaces;
        // Khởi tạo Adapter với chế độ hiển thị (isManageMode = false)
        workspaceAdapter = new WorkspaceAdapter(getContext(), new ArrayList<>(), this);
        rvWorkspaces.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWorkspaces.setAdapter(workspaceAdapter);
    }

    /**
     * Hiển thị PopupMenu khi nhấn giữ vào một Bảng
     */
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

    /**
     * Dialog nhập tên mới cho Bảng
     */
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
                                bangViewModel.loadWorkspaces();

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

    /**
     * Xác nhận trước khi xóa Bảng
     */
    private void showDeleteBoardConfirm(Board board) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa bảng")
                .setMessage("Bạn có chắc chắn muốn xóa bảng '" + board.getName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boardRepository.deleteBoard(board.getUid(), new BoardRepository.GeneralCallback() {
                        @Override
                        public void onSuccess() {
                            bangViewModel.loadWorkspaces();
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
        bangViewModel.loadWorkspaces();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}