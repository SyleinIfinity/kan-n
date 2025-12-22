// Đặt tại: src/main/java/com/kan_n/ui/fragments/bang_space/BangSpaceFragment.java (CẬP NHẬT)

package com.kan_n.ui.fragments.bang_space;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.kan_n.R;
import com.kan_n.data.interfaces.ListRepository;
import com.kan_n.data.models.Card;
import com.kan_n.data.models.ListModel;
import com.kan_n.data.models.Tag;
import com.kan_n.databinding.FragmentBangSpaceBinding;
import com.kan_n.ui.adapters.adapter.ListModelAdapter;

import java.util.List;

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

        // Trong phương thức onViewCreated
        binding.ivMenuOptions.setOnClickListener(v -> {
            // [FIX] Tạo Bundle để truyền boardId sang MenuBangFragment
            Bundle bundle = new Bundle();
            bundle.putString("boardId", boardId); // boardId đã có sẵn trong BangSpaceFragment
            navController.navigate(R.id.action_BangSpaceFragment_to_MenuBangFragment, bundle);
        });


    }

    private void setupToolbar() {
        if (boardTitle != null) {
            binding.tvBoardTitleToolbar.setText(boardTitle);
        }
        binding.ivBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupRecyclerView() {
        // [CẬP NHẬT] Khởi tạo Adapter với tham số LongClick mới
        listModelAdapter = new ListModelAdapter(getContext(), viewModel, this,
                // 1. Click thường (vào chi tiết thẻ)
                new ListModelAdapter.OnItemCardClickListener() {
                    @Override
                    public void onCardClick(Card card) {
                        Bundle bundle = new Bundle();
                        bundle.putString("cardId", card.getUid());
                        bundle.putString("cardTitle", card.getTitle());
                        bundle.putString("boardId", boardId);
                        try {
                            navController.navigate(R.id.action_BangSpaceFragment_to_CardSpaceFragment, bundle);
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                },
                // [MỚI] 2. Long Click (Hiện menu tùy chọn)
                new ListModelAdapter.OnItemCardLongClickListener() {
                    @Override
                    public void onCardLongClick(Card card, View view) {
                        showCardOptionsDialog(card);
                    }
                }
        );

        binding.rvLists.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvLists.setAdapter(listModelAdapter);
    }

    private void showCardOptionsDialog(Card card) {
        if (getContext() == null) return;

        String[] options = {"Đổi tên thẻ", "Gắn Tag (Nhãn màu)", "Xóa thẻ"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Tùy chọn thẻ: " + card.getTitle());
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Đổi tên
                    showRenameCardDialog(card);
                    break;
                case 1: // Gắn Tag
                    showTagSelectionDialog(card);
                    break;
                case 2: // Xóa
                    showDeleteConfirmDialog(card);
                    break;
            }
        });
        builder.show();
    }

    // 2. Dialog Đổi tên
    private void showRenameCardDialog(Card card) {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Đổi tên thẻ");

        final View customLayout = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input_text, null);
        EditText etInput = customLayout.findViewById(R.id.et_input_title);
        etInput.setText(card.getTitle());
        etInput.setSelection(card.getTitle().length());

        builder.setView(customLayout);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newTitle = etInput.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                viewModel.updateCardTitle(card.getUid(), newTitle);
                Toast.makeText(getContext(), "Đã cập nhật tên", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // 3. Dialog Xác nhận Xóa
    private void showDeleteConfirmDialog(Card card) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa thẻ")
                .setMessage("Bạn có chắc chắn muốn xóa thẻ \"" + card.getTitle() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteCard(card.getUid());
                    Toast.makeText(getContext(), "Đã xóa thẻ", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // 4. Dialog Chọn Tag
    private void showTagSelectionDialog(Card card) {
        // Tải danh sách tag từ Firebase thông qua ViewModel
        viewModel.loadAllTags(new BangSpaceViewModel.TagListCallback() {
            @Override
            public void onTagsLoaded(List<Tag> tags) {
                if (getContext() == null) return;

                // Tạo Adapter để hiển thị List Tag kèm màu sắc
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Chọn nhãn màu");

                // Tạo custom adapter đơn giản
                ArrayAdapter<Tag> adapter = new ArrayAdapter<Tag>(getContext(), android.R.layout.select_dialog_singlechoice, tags) {
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        // Hiển thị tên Tag + Màu
                        TextView view = (TextView) super.getView(position, convertView, parent);
                        Tag tag = getItem(position);
                        view.setText(tag.getName());
                        try {
                            view.setTextColor(Color.parseColor(tag.getColor()));
                        } catch (Exception e) {
                            view.setTextColor(Color.BLACK);
                        }
                        return view;
                    }
                };

                builder.setAdapter(adapter, (dialog, which) -> {
                    Tag selectedTag = tags.get(which);
                    // Cập nhật card với tag mới
                    viewModel.updateCardTag(card.getUid(), selectedTag);
                    Toast.makeText(getContext(), "Đã gắn tag: " + selectedTag.getName(), Toast.LENGTH_SHORT).show();
                });

                builder.setNegativeButton("Hủy", null);
                builder.show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Lỗi tải Tag: " + message, Toast.LENGTH_SHORT).show();
            }
        });
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