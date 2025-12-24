package com.kan_n.ui.fragments.menu_bang;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Pair; // Import Pair để hứng dữ liệu User + Role
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // Import ViewModelProvider
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager; // Import LayoutManager
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kan_n.R;
import com.kan_n.data.interfaces.BoardRepository;
import com.kan_n.data.interfaces.InvitationRepository;
import com.kan_n.data.models.Board;
import com.kan_n.data.models.User;
import com.kan_n.data.repository.InvitationRepositoryImpl;
import com.kan_n.databinding.FragmentMenuBangBinding;
import com.kan_n.ui.adapters.adapter.MemberAdapter; // Import Adapter
import com.kan_n.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class MenuBangFragment extends Fragment {
    private FragmentMenuBangBinding binding;
    private MenuBangViewModel viewModel; // Khai báo ViewModel
    private NavController navController;
    private MemberAdapter memberAdapter; // Adapter của bạn
    private String boardId;
    private String boardTitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            boardId = getArguments().getString("boardId");
            boardTitle = getArguments().getString("boardTitle", "MENU BẢNG");
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMenuBangBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(MenuBangViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);

        // Cap nhat tieu de toolbar
        binding.tvToolbarTitle.setText(boardTitle);

        setupRecyclerView();

        // Thiet lap su kien cho nut 3 cham
        setupOptionsMenu();

        if (boardId != null) {
            viewModel.loadMembers(boardId);
            viewModel.fetchCurrentUserRole(boardId); // Lay role de phan quyen sau nay
        }

        viewModel.getMembersList().observe(getViewLifecycleOwner(), members -> {
            if (members != null && memberAdapter != null) {
                memberAdapter.setData(members);
            }
        });

        binding.btnInviteMember.setOnClickListener(v -> showInviteDialog());

        binding.btnBack.setOnClickListener(v -> navController.popBackStack());
    }

    private void showInviteDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder =
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Mời thành viên");

        // Layout dialog tự tạo hoặc dùng LinearLayout tạo bằng code
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input_text, null);
        EditText etEmail = view.findViewById(R.id.et_input_title);
        etEmail.setHint("Nhập email người nhận");

        builder.setView(view);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String email = etEmail.getText().toString().trim();
            if (!email.isEmpty() && boardId != null) {
                String boardName = "Bảng công việc";
                viewModel.sendInvite(boardId, boardName, email, "member");
                Toast.makeText(getContext(), "Đang gửi...", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    private void setupOptionsMenu() {

        binding.ivMenuOptions.setOnClickListener(v -> {
            // Khởi tạo PopupMenu gắn vào icon 3 chấm
            PopupMenu popup = new PopupMenu(getContext(), v);

            // Lấy vai trò của người dùng hiện tại từ ViewModel
            // Giá trị này được cập nhật thông qua fetchCurrentUserRole(boardId) trong onViewCreated
            String role = viewModel.getCurrentUserRole();

            // PHÂN QUYỀN MENU HIỂN THỊ
            if ("owner".equals(role)) {
                // Chủ sở hữu có toàn quyền quản trị
                popup.getMenu().add("Đổi tên bảng");
                popup.getMenu().add("Đổi nền bảng");
                popup.getMenu().add("Xóa bảng");
                popup.getMenu().add("Danh sách thành viên");
            } else {
                // Thành viên bình thường chỉ có quyền xem danh sách và rút lui
                popup.getMenu().add("Danh sách thành viên");
                popup.getMenu().add("Rời khỏi bảng");
            }

            // XỬ LÝ SỰ KIỆN KHI NHẤN VÀO TỪNG OPTION
            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();

                switch (title) {
                    case "Đổi tên bảng":
                        showRenameDialog();
                        break;

                    case "Đổi nền bảng":
                        navigateToChangeBackground();
                        break;

                    case "Xóa bảng":
                        showDeleteConfirm();
                        break;

                    case "Danh sách thành viên":
                        // Hiển thị dialog danh sách thành viên (có nút xóa thành viên nếu là owner)
                        showManageMembersDialog();
                        break;

                    case "Rời khỏi bảng":
                        // Thành viên tự xóa chính mình khỏi node memberships
                        showLeaveBoardConfirm();
                        break;
                }
                return true;
            });

            popup.show();
        });
    }

    private void showLeaveBoardConfirm() {
        new AlertDialog.Builder(getContext())
                .setTitle("Rời khỏi bảng")
                .setMessage("Bạn có chắc chắn muốn rời khỏi bảng này?")
                .setPositiveButton("Rời bảng", (d, w) -> {
                    viewModel.leaveOrRemoveMember(boardId, FirebaseUtils.getCurrentUserId(), new BoardRepository.GeneralCallback() {
                        @Override
                        public void onSuccess() {
                            navController.navigate(R.id.thanhDieuHuong_Bang); // Quay về trang chủ
                        }
                        @Override public void onError(String msg) {}
                    });
                }).setNegativeButton("Hủy", null).show();
    }
    private void showRenameDialog() {
        EditText etInput = new EditText(getContext());
        etInput.setText("");
        etInput.setSelection(etInput.getText().length());

        new AlertDialog.Builder(getContext())
                .setTitle("Đổi tên bảng")
                .setView(etInput)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String newName = etInput.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        viewModel.updateBoardName(boardId, newName, new BoardRepository.GeneralCallback() {
                            @Override
                            public void onSuccess() {

                                viewModel.loadMembers(boardId);

                                Toast.makeText(getContext(), "Đã cập nhật thành công", Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onError(String message) {}
                        });
                    }
                }).setNegativeButton("Hủy", null).show();
    }
    private void showDeleteConfirm() {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa bảng")
                .setMessage("Bạn có chắc chắn muốn xóa bảng '" + boardTitle + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteBoard(boardId, new BoardRepository.GeneralCallback() {
                        @Override
                        public void onSuccess() {
                            navController.navigate(R.id.thanhDieuHuong_Bang);
                        }
                        @Override public void onError(String message) {}
                    });
                }).setNegativeButton("Hủy", null).show();
    }

    private void showManageMembersDialog() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_manage_members, null);

        RecyclerView rv = view.findViewById(R.id.rv_members_list);
        // [MỚI] Tìm nút Đóng trong layout dialog
        AppCompatButton btnClose = view.findViewById(R.id.btn_close_dialog);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Gắn sự kiện cho nút Đóng
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> bottomSheet.dismiss());
        }

        viewModel.getBoardMembers(boardId, new BoardRepository.BoardMembersCallback() {
            @Override
            public void onSuccess(List<Pair<User, String>> members) {
                MemberAdapter adapter = new MemberAdapter(getContext(), members, (memberUser, role) -> {
                    // Truyền thêm instance của bottomSheet để có thể đóng/mở lại khi xóa thành công
                    showMemberPermissionOptions(memberUser, bottomSheet);
                });
                rv.setAdapter(adapter);
            }
            @Override public void onError(String msg) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheet.setContentView(view);
        bottomSheet.show();
    }
    private void showMemberPermissionOptions(User targetUser, BottomSheetDialog parentBottomSheet) {
        String role = viewModel.getCurrentUserRole();
        String currentUserId = FirebaseUtils.getCurrentUserId();

        if (!"owner".equals(role)) {
            Toast.makeText(getContext(), "Chỉ quản trị viên mới có quyền này", Toast.LENGTH_SHORT).show();
            return;
        }

        if (targetUser.getUid().equals(currentUserId)) {
            Toast.makeText(getContext(), "Đây là tài khoản của bạn", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {"Quyền Xem", "Quyền Chỉnh sửa", "Xóa khỏi bảng"};
        new AlertDialog.Builder(getContext())
                .setTitle("Quản lý: " + targetUser.getUsername())
                .setItems(options, (dialog, which) -> {
                    if (which == 2) { // Xóa khỏi bảng
                        viewModel.leaveOrRemoveMember(boardId, targetUser.getUid(), new BoardRepository.GeneralCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getContext(), "Đã xóa thành viên", Toast.LENGTH_SHORT).show();
                                // [LOAD LẠI TRANG] Đóng dialog hiện tại và mở lại để cập nhật danh sách
                                if (parentBottomSheet != null) parentBottomSheet.dismiss();
                                showManageMembersDialog();
                                // Cập nhật cả danh sách ở màn hình chính fragment
                                viewModel.loadMembers(boardId);
                            }
                            @Override public void onError(String msg) {
                                Toast.makeText(getContext(), "Lỗi: " + msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Xử lý phân quyền Xem/Sửa
                        String newPerm = (which == 0) ? "view" : "edit";
                        viewModel.updateMemberPermission(boardId, targetUser.getUid(), newPerm, new BoardRepository.GeneralCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getContext(), "Đã cập nhật quyền", Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onError(String msg) {}
                        });
                    }
                }).show();
    }
    private void navigateToChangeBackground() {
        Bundle args = new Bundle();
        args.putString("boardId", boardId);
        args.putBoolean("isEditing", true);
        navController.navigate(R.id.action_MenuBangFragment_to_taoBangMoiChonPhongFragment, args);
    }

    private void setupRecyclerView() {
        memberAdapter = new MemberAdapter(getContext(), new ArrayList<>(), (user, role) -> {
        });

        binding.rcvMembers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rcvMembers.setAdapter(memberAdapter);
    }

    private void setupToolbar() {
        // Logic xử lý toolbar khác nếu cần
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Khi màn hình này hiện lên -> TẮT ActionBar mặc định của Activity
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide();
            }
        }
    }
}