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
import com.kan_n.data.interfaces.BoardRepository;
import com.kan_n.data.interfaces.ListRepository;
import com.kan_n.data.models.Board;
import com.kan_n.data.models.Card;
import com.kan_n.data.models.ListModel;
import com.kan_n.data.models.Tag;
import com.kan_n.data.repository.BoardRepositoryImpl;
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
    private final BoardRepository boardRepository = new BoardRepositoryImpl();

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
            viewModel.getBoardDetails(boardId, new BoardRepository.BoardCallback() {
                @Override
                public void onSuccess(Board board) {
                    if (board.getBackground() != null && "color".equals(board.getBackground().getType())) {
                        applyDynamicColors(board.getBackground().getValue());
                    }
                }
                @Override
                public void onError(String message) {}
            });
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
    public void getBoardDetails(String boardId, BoardRepository.BoardCallback callback) {
        boardRepository.getBoardDetails(boardId, callback);
    }

    private void applyDynamicColors(String hexColor) {
        try {
            int baseColor = Color.parseColor(hexColor);

            // 1. Màu Toolbar (Màu gốc)
            binding.toolbarBangSpace.setBackgroundColor(baseColor);

            // 2. Màu Status Bar (Đậm hơn màu gốc 20%)
            if (getActivity() != null) {
                getActivity().getWindow().setStatusBarColor(adjustColor(baseColor, 0.8f));
            }

            // 3. Màu nền Fragment (Rất nhạt)
            int bgLightColor = lightenColor(baseColor);
            binding.getRoot().setBackgroundColor(bgLightColor);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupToolbar() {
        if (boardTitle != null) {
            binding.tvBoardTitleToolbar.setText(boardTitle);
        }
        binding.ivBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupRecyclerView() {
        // Khởi tạo Adapter với 4 tham số listener đầy đủ
        listModelAdapter = new ListModelAdapter(getContext(), viewModel, this,
                // 1. Click vào thẻ (Mở chi tiết thẻ - Code cũ của bạn)
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
                // 2. Nhấn giữ thẻ (Hiện menu tùy chọn thẻ - Code cũ của bạn)
                new ListModelAdapter.OnItemCardLongClickListener() {
                    @Override
                    public void onCardLongClick(Card card, View view) {
                        showCardOptionsDialog(card);
                    }
                },
                // 3. [MỚI] Click vào menu danh sách (Dấu 3 chấm ở cột list)
                new ListModelAdapter.OnListMenuClickListener() {
                    @Override
                    public void onListMenuClick(View view, ListModel listModel, int position) {
                        showListOptionsDialog(listModel, position);
                    }
                }
        );

        binding.rvLists.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvLists.setAdapter(listModelAdapter);
    }

    // --- CÁC HÀM XỬ LÝ LOGIC MENU LIST ---

    // 1. Hiển thị Dialog tùy chọn
    private void showListOptionsDialog(ListModel listModel, int position) {
        if (getContext() == null) return;

        String[] options = {"Đổi tên danh sách", "Di chuyển sang trái", "Di chuyển sang phải", "Xóa danh sách"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Tùy chọn: " + listModel.getTitle());
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Đổi tên
                    showRenameListDialog(listModel);
                    break;
                case 1: // Sang trái
                    moveList(listModel, position, -1); // -1 là trái
                    break;
                case 2: // Sang phải
                    moveList(listModel, position, 1);  // 1 là phải
                    break;
                case 3: // Xóa
                    showDeleteListConfirmDialog(listModel);
                    break;
            }
        });
        builder.show();
    }

    // 2. Dialog Đổi tên danh sách
    private void showRenameListDialog(ListModel listModel) {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Đổi tên danh sách");

        final View customLayout = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input_text, null);
        EditText etInput = customLayout.findViewById(R.id.et_input_title);
        etInput.setText(listModel.getTitle());
        etInput.setSelection(listModel.getTitle().length());

        builder.setView(customLayout);
        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String newTitle = etInput.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                viewModel.updateListTitle(listModel.getUid(), newTitle);
                Toast.makeText(getContext(), "Đã cập nhật tên", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // 3. Logic Di chuyển (Trái/Phải)
    private void moveList(ListModel currentList, int currentPosition, int direction) {
        List<ListModel> allLists = listModelAdapter.getCurrentList();
        int targetIndex = currentPosition + direction;

        // Kiểm tra biên (không thể sang trái nếu ở đầu, không thể sang phải nếu ở cuối)
        if (targetIndex < 0 || targetIndex >= allLists.size()) {
            Toast.makeText(getContext(), "Không thể di chuyển thêm nữa", Toast.LENGTH_SHORT).show();
            return;
        }

        ListModel targetList = allLists.get(targetIndex);

        // Logic đổi chỗ: Hoán đổi giá trị 'position' của 2 danh sách trong Database
        // Firebase sẽ tự động trigger onChildChanged và sắp xếp lại UI
        double pos1 = currentList.getPosition();
        double pos2 = targetList.getPosition();

        // Cập nhật lên Firebase
        viewModel.updateListPosition(currentList.getUid(), pos2);
        viewModel.updateListPosition(targetList.getUid(), pos1);
    }

    // 4. Dialog Xác nhận Xóa danh sách
    private void showDeleteListConfirmDialog(ListModel listModel) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa danh sách")
                .setMessage("Bạn có chắc chắn muốn xóa danh sách \"" + listModel.getTitle() + "\" và toàn bộ thẻ bên trong không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteList(listModel.getUid());
                    Toast.makeText(getContext(), "Đã xóa danh sách", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
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

    // Hàm làm sáng hoặc làm đậm màu sắc
    private int adjustColor(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor; // Thay đổi giá trị độ sáng (Value)
        return Color.HSVToColor(hsv);
    }

    // Hàm làm nhạt màu để làm nền
    private int lightenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] *= 0.3f; // Giảm độ bão hòa xuống 30% để màu trông nhạt và dễ nhìn hơn
        hsv[2] = 0.95f; // Tăng độ sáng lên cao
        return Color.HSVToColor(hsv);
    }
    @Override
    public void onResume() {
        super.onResume();
        // Mỗi khi quay lại trang này, thực hiện reload thông tin bảng
        if (boardId != null) {
            viewModel.getBoardDetails(boardId, new BoardRepository.BoardCallback() {
                @Override
                public void onSuccess(Board board) {
                    if (board != null) {
                        // Cập nhật lại tiêu đề trên Toolbar bằng tên mới từ database
                        boardTitle = board.getName();
                        binding.tvBoardTitleToolbar.setText(boardTitle);
                    }
                }

                @Override
                public void onError(String message) {
                    // Xử lý lỗi nếu cần
                }
            });
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}