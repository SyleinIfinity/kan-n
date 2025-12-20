package com.kan_n.ui.fragments.work_space;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kan_n.R;
import com.kan_n.data.interfaces.BoardRepository;
import com.kan_n.data.models.Workspace;
import com.kan_n.data.repository.BoardRepositoryImpl;
import com.kan_n.databinding.FragmentWorkspaceListBinding;
import com.kan_n.ui.adapters.adapter.WorkspaceAdapter;
import com.kan_n.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceListFragment extends Fragment {
    private FragmentWorkspaceListBinding binding;
    private BoardRepository boardRepository;
    private WorkspaceAdapter adapter;
    private String activeWsId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkspaceListBinding.inflate(inflater, container, false);
        boardRepository = new BoardRepositoryImpl();

        // Lấy Workspace đang hoạt động từ SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("KanN_Prefs", Context.MODE_PRIVATE);
        activeWsId = prefs.getString("active_ws_id", "ws_1_id"); // Mặc định lấy id đầu tiên

        setupUI();
        loadWorkspaces();
        return binding.getRoot();
    }

    // Trong WorkspaceListFragment.java

    private void setupUI() {
        // 1. Nút Back
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // 2. Lấy activeWsId từ SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("KanN_Prefs", Context.MODE_PRIVATE);
        activeWsId = prefs.getString("active_ws_id", "ws_1_id");

        // 3. Khởi tạo WorkspaceAdapter ở chế độ quản lý
        adapter = new WorkspaceAdapter(getContext(), new ArrayList<>(), activeWsId, new WorkspaceAdapter.OnWorkspaceActionListener() {
            @Override
            public void onSelect(Workspace ws) {
                confirmSwitchWorkspace(ws);
            }

            @Override
            public void onEdit(Workspace ws) {
                showEditDialog(ws);
            }

            @Override
            public void onDelete(Workspace ws) {
                showDeleteConfirm(ws);
            }
        });

        binding.rvWorkspaceList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvWorkspaceList.setAdapter(adapter);

        // 4. FAB Thêm mới
        binding.fabAddWorkspace.setOnClickListener(v -> showAddDialog());
    }

    private void loadWorkspaces() {
        boardRepository.getWorkspacesWithBoards(FirebaseUtils.getCurrentUserId(), new BoardRepository.WorkspacesWithBoardsCallback() {
            @Override
            public void onSuccess(List<Workspace> workspaces) {
                if (isAdded()) adapter.updateData(workspaces);
            }
            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Logic chuyển đổi Không gian làm việc
    private void confirmSwitchWorkspace(Workspace ws) {
        if (ws.getUid().equals(activeWsId)) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Đổi Không gian làm việc")
                .setMessage("Bạn có muốn chuyển sang: " + ws.getName() + "?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    // Lưu trạng thái mới
                    requireActivity().getSharedPreferences("KanN_Prefs", Context.MODE_PRIVATE)
                            .edit().putString("active_ws_id", ws.getUid()).apply();

                    // Chuyển về màn hình Bảng
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.thanhDieuHuong_Bang);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // 1. Phương thức hiển thị Dialog thêm mới
    private void showAddDialog() {
        EditText input = new EditText(getContext());
        input.setHint("Nhập tên không gian làm việc...");

        new AlertDialog.Builder(getContext())
                .setTitle("Tạo Không gian mới")
                .setView(input)
                .setPositiveButton("Tạo", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        boardRepository.createWorkspace(name, "Mô tả mới", new BoardRepository.GeneralCallback() {
                            @Override
                            public void onSuccess() {
                                loadWorkspaces(); // Tải lại danh sách
                                Toast.makeText(getContext(), "Đã tạo thành công", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String msg) {
                                Toast.makeText(getContext(), "Lỗi: " + msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // 2. Phương thức hiển thị Dialog sửa tên
    private void showEditDialog(Workspace ws) {
        EditText input = new EditText(getContext());
        input.setText(ws.getName());

        new AlertDialog.Builder(getContext())
                .setTitle("Đổi tên không gian")
                .setView(input)
                .setPositiveButton("Cập nhật", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        boardRepository.updateWorkspace(ws.getUid(), newName, new BoardRepository.GeneralCallback() {
                            @Override
                            public void onSuccess() {
                                loadWorkspaces();
                                Toast.makeText(getContext(), "Đã đổi tên", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String msg) {
                                Toast.makeText(getContext(), "Lỗi: " + msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // 3. Phương thức hiển thị xác nhận xóa
    private void showDeleteConfirm(Workspace ws) {
        // Không cho phép xóa Workspace đang hoạt động (active)
        if (ws.getUid().equals(activeWsId)) {
            Toast.makeText(getContext(), "Không thể xóa không gian đang mở!", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa không gian làm việc")
                .setMessage("Bạn có chắc chắn muốn xóa '" + ws.getName() + "'? Dữ liệu bên trong sẽ bị mất.")
                .setPositiveButton("Xóa", (d, w) -> {
                    boardRepository.deleteWorkspace(ws.getUid(), new BoardRepository.GeneralCallback() {
                        @Override
                        public void onSuccess() {
                            loadWorkspaces();
                            Toast.makeText(getContext(), "Đã xóa không gian", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String msg) {
                            Toast.makeText(getContext(), "Lỗi: " + msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
