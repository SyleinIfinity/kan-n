// Đặt tại: src/main/java/com/kan_n/ui/adapters/adapter/ListModelAdapter.java (CẬP NHẬT)

package com.kan_n.ui.adapters.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText; // ✨ Thêm
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast; // ✨ Thêm

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.kan_n.R;
import com.kan_n.data.interfaces.CardRepository; // ✨ Thêm
import com.kan_n.data.models.Card;
import com.kan_n.data.models.ListModel;
import com.kan_n.ui.fragments.bang_space.BangSpaceViewModel;

import java.util.ArrayList;
import java.util.List;

public class ListModelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ListModel> listModelList = new ArrayList<>();
    private Context context;
    private BangSpaceViewModel viewModel;

    private static final int VIEW_TYPE_LIST = 0;
    private static final int VIEW_TYPE_ADD = 1;

    private final OnAddListClickListener addListClickListener;

    public interface OnAddListClickListener {
        void onAddListClick();
    }

    public ListModelAdapter(Context context, BangSpaceViewModel viewModel, OnAddListClickListener listener) {
        this.context = context;
        this.viewModel = viewModel;
        this.addListClickListener = listener;
    }

    // (Các hàm getItemCount, getItemViewType, onCreateViewHolder, onBindViewHolder, onViewRecycled, ...
    // ... addList, updateList, removeList, getLastItemPosition ...
    // ... giữ nguyên như trong câu trả lời trước của tôi)
    @Override
    public int getItemCount() {
        return listModelList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == listModelList.size()) ? VIEW_TYPE_ADD : VIEW_TYPE_LIST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_LIST) {
            View view = inflater.inflate(R.layout.item_listmodel, parent, false);
            return new ListModelViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_add_list, parent, false);
            return new AddListViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_LIST) {
            ListModel listModel = listModelList.get(position);
            if (listModel == null || listModel.getUid() == null) return;
            ((ListModelViewHolder) holder).bind(listModel);
        } else {
            ((AddListViewHolder) holder).bind(addListClickListener);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof ListModelViewHolder) {
            ((ListModelViewHolder) holder).clearListener();
        }
    }

    private int getInsertPosition(ListModel list) {
        for (int i = 0; i < listModelList.size(); i++) {
            if (listModelList.get(i).getPosition() > list.getPosition()) {
                return i;
            }
        }
        return listModelList.size();
    }

    public void addList(ListModel list) {
        int position = getInsertPosition(list);
        listModelList.add(position, list);
        notifyItemInserted(position);
    }

    public void updateList(ListModel updatedList) {
        for (int i = 0; i< listModelList.size(); i++) {
            if (listModelList.get(i).getUid().equals(updatedList.getUid())) {
                listModelList.set(i, updatedList);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeList(String listId) {
        for (int i = 0; i < listModelList.size(); i++) {
            if (listModelList.get(i).getUid().equals(listId)) {
                listModelList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public double getLastItemPosition() {
        if (listModelList.isEmpty()) {
            return 0;
        }
        return listModelList.get(listModelList.size() - 1).getPosition();
    }


    // --- ViewHolder 1: Cho Danh sách (ĐÃ CẬP NHẬT) ---
    public class ListModelViewHolder extends RecyclerView.ViewHolder {
        TextView tvListTitle;
        RecyclerView rvCards;
        // LinearLayout layoutAddCard; // <-- ✨ ĐÃ BỊ XÓA (vì đã xóa khỏi layout)
        ImageView ivMenuDanhSach;

        private CardAdapter cardAdapter;
        private ChildEventListener cardListener;
        private String currentListId;


        public ListModelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvListTitle = itemView.findViewById(R.id.tvTieuDeDanhSach);
            rvCards = itemView.findViewById(R.id.rvDanhSachThe);
            // layoutAddCard = itemView.findViewById(R.id.layoutThemThe); // <-- ✨ XÓA DÒNG NÀY
            ivMenuDanhSach = itemView.findViewById(R.id.ivMenuDanhSach);

            // ✨ BƯỚC 1 (TRONG VIEW HOLDER): Tạo listener cho "Add Card"
            CardAdapter.OnAddCardClickListener addCardListener = new CardAdapter.OnAddCardClickListener() {
                @Override
                public void onAddCardClick() {
                    // 'currentListId' sẽ được cập nhật trong hàm bind()
                    if (currentListId != null) {
                        // Gọi hàm hiển thị dialog
                        showAddCardDialog(currentListId);
                    }
                }
            };

            // ✨ BƯỚC 2 (TRONG VIEW HOLDER): Cài đặt CardAdapter với listener mới
            cardAdapter = new CardAdapter(context, addCardListener);
            rvCards.setLayoutManager(new LinearLayoutManager(context));
            rvCards.setAdapter(cardAdapter);
        }

        public void bind(ListModel listModel) {
            currentListId = listModel.getUid();
            tvListTitle.setText(listModel.getTitle());

            cardListener = new ChildEventListener() {
                // (Tất cả logic onChildAdded...onCancelled giữ nguyên)
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Card card = snapshot.getValue(Card.class);
                    if (card != null) {
                        card.setUid(snapshot.getKey());
                        cardAdapter.addCard(card);
                    }
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Card card = snapshot.getValue(Card.class);
                    if (card != null) {
                        card.setUid(snapshot.getKey());
                        cardAdapter.updateCard(card);
                    }
                }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    cardAdapter.removeCard(snapshot.getKey());
                }
                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ListModelAdapter", "Error listenForCards: " + error.getMessage());
                }
            };
            viewModel.listenForCards(currentListId, cardListener);
        }

        public void clearListener() {
            if (viewModel != null && currentListId != null) {
                viewModel.removeCardListener(currentListId);
            }
        }

        // ✨ BƯỚC 3 (TRONG VIEW HOLDER): Thêm hàm hiển thị Dialog "Thêm thẻ"
        /**
         * Hiển thị dialog để nhập tiêu đề thẻ mới.
         * Hàm này được gọi bởi listener 'addCardListener'
         */
        private void showAddCardDialog(String listId) {
            // Cần Context (từ adapter) và ViewModel (từ adapter)
            if (context == null || viewModel == null) {
                Log.e("ListModelAdapter", "Context or ViewModel is null in showAddCardDialog");
                return;
            }

            // Dùng MaterialAlertDialogBuilder
            com.google.android.material.dialog.MaterialAlertDialogBuilder builder =
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(context);
            builder.setTitle("Thêm thẻ mới");

            // Inflate layout dialog_input_text.xml
            final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_input_text, null);
            final EditText input = dialogView.findViewById(R.id.et_input_title);
            input.setHint("Nhập tiêu đề thẻ");
            builder.setView(dialogView);

            // Set up buttons
            builder.setPositiveButton("Thêm", (dialog, which) -> {
                String title = input.getText().toString().trim();
                if (!title.isEmpty()) {

                    // Tính position cho thẻ mới
                    // (Chúng ta đã thêm hàm này vào CardAdapter)
                    double newPosition = cardAdapter.getLastItemPosition() + 1000.0;

                    // Gọi ViewModel để tạo thẻ
                    viewModel.createNewCard(listId, title, newPosition, new CardRepository.GeneralCallback() {
                        @Override
                        public void onSuccess() {
                            // Không cần làm gì, ChildEventListener sẽ tự động cập nhật
                        }
                        @Override
                        public void onError(String message) {
                            Toast.makeText(context, "Lỗi tạo thẻ: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(context, "Tiêu đề không được rỗng", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

            builder.show();
        }
    }

    // --- ViewHolder 2: Cho Nút "Thêm Danh sách" (Giữ nguyên) ---
    public static class AddListViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutThemDanhSach;

        public AddListViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutThemDanhSach = itemView.findViewById(R.id.layoutThemDanhSach);
        }

        public void bind(final OnAddListClickListener listener) {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddListClick();
                }
            });
        }
    }
}