// Đặt tại: src/main/java/com/kan_n/ui/fragments/bang_space/BangSpaceFragment.java (CẬP NHẬT)

package com.kan_n.ui.fragments.bang_space;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.kan_n.R;
import com.kan_n.data.interfaces.ListRepository;
import com.kan_n.data.models.ListModel;
import com.kan_n.databinding.FragmentBangSpaceBinding;
import com.kan_n.ui.adapters.adapter.ListModelAdapter;
public class BangSpaceFragment extends Fragment implements ListModelAdapter.OnAddListClickListener {

    private FragmentBangSpaceBinding binding;
    private BangSpaceViewModel viewModel;
    private ListModelAdapter listModelAdapter;

    private NavController navController;

    private String boardId;
    private String boardTitle;

    // private RecyclerView.AdapterDataObserver adapterObserver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BangSpaceViewModel.class);

        if (getArguments() != null) {
            boardId = getArguments().getString("boardId");
            boardTitle = getArguments().getString("boardTitle");
        } else {
            boardId = null;
            boardTitle = "Lỗi";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBangSpaceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupRecyclerView();

        navController = NavHostFragment.findNavController(this);


        if (boardId != null) {
            listenForLists(boardId);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID Bảng", Toast.LENGTH_LONG).show();
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }

        binding.ivMenuOptions.setOnClickListener(v -> {
            navController.navigate(R.id.action_BangSpaceFragment_to_MenuBangFragment);
        });


    }

    private void setupToolbar() {
        if (boardTitle != null) {
            binding.tvBoardTitleToolbar.setText(boardTitle);
        }
        binding.ivBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupRecyclerView() {
        // truyền "this" làm listener
        listModelAdapter = new ListModelAdapter(getContext(), viewModel, this, new ListModelAdapter.OnItemCardClickListener() {
            @Override
            public void onCardClick(com.kan_n.data.models.Card card) {
                // --- XỬ LÝ CHUYỂN TRANG KHI CLICK VÀO THẺ ---
                Bundle bundle = new Bundle();
                bundle.putString("cardId", card.getUid());
                bundle.putString("cardTitle", card.getTitle());
                bundle.putString("boardId", boardId);

                try {
                    navController.navigate(R.id.action_BangSpaceFragment_to_CardSpaceFragment, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Lỗi điều hướng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.rvLists.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvLists.setAdapter(listModelAdapter);
    }


    /**
     * Đây là hàm được gọi khi nhấn vào nút "Thêm danh sách" bên trong RecyclerView
     */
    @Override
    public void onAddListClick() {
        showAddListDialog();
    }

    /**
     * được gọi bởi onAddListClick
     */
    private void showAddListDialog() {
        if (getContext() == null) return;

        com.google.android.material.dialog.MaterialAlertDialogBuilder builder =
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext());
        builder.setTitle("Thêm danh sách mới");

        final View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input_text, null);
        final EditText input = dialogView.findViewById(R.id.et_input_title);
        input.setHint("Nhập tiêu đề danh sách");
        builder.setView(dialogView);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty() && boardId != null) {
                double newPosition = listModelAdapter.getLastItemPosition() + 1000.0;
                viewModel.createNewList(boardId, title, newPosition, new ListRepository.GeneralCallback() {
                    @Override
                    public void onSuccess() {
                    }
                    @Override
                    public void onError(String message) {
                        Toast.makeText(getContext(), "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Tiêu đề không được rỗng", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void listenForLists(String boardId) {
        viewModel.listenForLists(boardId, new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ListModel list = snapshot.getValue(ListModel.class);
                if (list != null) {
                    list.setUid(snapshot.getKey());
                    listModelAdapter.addList(list);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ListModel list = snapshot.getValue(ListModel.class);
                if (list != null) {
                    list.setUid(snapshot.getKey());
                    listModelAdapter.updateList(list);
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                listModelAdapter.removeList(snapshot.getKey());
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Loi tai danh sach: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}