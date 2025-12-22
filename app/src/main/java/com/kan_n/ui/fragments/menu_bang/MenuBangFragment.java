package com.kan_n.ui.fragments.menu_bang;

import android.os.Bundle;
import android.util.Pair; // Import Pair để hứng dữ liệu User + Role
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // Import ViewModelProvider
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager; // Import LayoutManager

import com.kan_n.R;
import com.kan_n.data.interfaces.InvitationRepository;
import com.kan_n.data.models.User;
import com.kan_n.data.repository.InvitationRepositoryImpl;
import com.kan_n.databinding.FragmentMenuBangBinding;
import com.kan_n.ui.adapters.adapter.MemberAdapter; // Import Adapter

import java.util.List;

public class MenuBangFragment extends Fragment {
    private FragmentMenuBangBinding binding;
    private MenuBangViewModel viewModel; // Khai báo ViewModel
    private NavController navController;
    private MemberAdapter memberAdapter; // Adapter của bạn
    private String boardId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // [FIX] Nhận boardId từ Bundle
        if (getArguments() != null) {
            boardId = getArguments().getString("boardId");
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMenuBangBinding.inflate(inflater, container, false);
        // [FIX] Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(MenuBangViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // [FIX QUAN TRỌNG] Khởi tạo NavController
        navController = NavHostFragment.findNavController(this);

        // Setup RecyclerView (Gọi hàm setupRecyclerView để code gọn hơn)
        setupRecyclerView();

        // Load dữ liệu
        if (boardId != null) {
            viewModel.loadMembers(boardId);
        }

        // Quan sát dữ liệu từ ViewModel
        viewModel.getMembersList().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                memberAdapter.setData(members);
            }
        });

        binding.btnInviteMember.setOnClickListener(v -> showInviteDialog());

        // Sự kiện nút Back
        binding.btnBack.setOnClickListener(v -> {
            if (navController != null) {
                navController.popBackStack();
            }
        });
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
                // Cần lấy boardName. Nếu chưa có, tạm thời truyền "" hoặc lấy từ ViewModel
                String boardName = "Bảng công việc"; // [FIXME] Hãy lấy tên bảng thật từ Bundle
                viewModel.sendInvite(boardId, boardName, email, "member");
                Toast.makeText(getContext(), "Đang gửi...", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void setupRecyclerView() {
        // Khởi tạo Adapter (bạn cần đảm bảo MemberAdapter đã được viết đúng)
        memberAdapter = new MemberAdapter();

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