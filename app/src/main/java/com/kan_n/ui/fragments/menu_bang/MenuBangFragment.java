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

        // [FIX QUAN TRONG] Phai goi ham nay de thiet lap su kien cho nut 3 cham
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
            PopupMenu popup = new PopupMenu(getContext(), v);
            popup.getMenu().add("Đổi tên bảng");
            popup.getMenu().add("Đổi nền bảng");
            popup.getMenu().add("Xóa bảng");
            popup.getMenu().add("Quản lý thành viên");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Đổi tên bảng")) showRenameDialog();
                else if (title.equals("Đổi nền bảng")) navigateToChangeBackground();
                else if (title.equals("Xóa bảng")) showDeleteConfirm();
                else if (title.equals("Quản lý thành viên")) showManageMembersDialog();
                return true;
            });
            popup.show();
        });
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
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel.getBoardMembers(boardId, new BoardRepository.BoardMembersCallback() {
            @Override
            public void onSuccess(List<Pair<User, String>> members) {
                // Su dung constructor day du cua MemberAdapter
                MemberAdapter adapter = new MemberAdapter(getContext(), members, (memberUser, role) -> {
                    showMemberPermissionOptions(memberUser);
                });
                rv.setAdapter(adapter);
            }
            @Override public void onError(String msg) {}
        });

        bottomSheet.setContentView(view);
        bottomSheet.show();
    }
    private void showMemberPermissionOptions(User targetUser) {
        String[] options = {"Quyền Xem (View)", "Quyền Chỉnh sửa (Edit)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Quyền hạn của " + targetUser.getUsername());

        if ("owner".equals(viewModel.getCurrentUserRole())) {
            builder.setItems(options, (dialog, which) -> {
                String newPerm = (which == 0) ? "view" : "edit";
                viewModel.updateMemberPermission(boardId, targetUser.getUid(), newPerm, new BoardRepository.GeneralCallback() {
                    @Override public void onSuccess() {
                        Toast.makeText(getContext(), "Đã cập nhật quyền", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onError(String msg) {}
                });
            });
        } else {
            builder.setMessage("Bạn không có quyền thay đổi phân quyền của thành viên này.");
        }
        builder.show();
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