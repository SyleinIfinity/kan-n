// File: app/src/main/java/com/kan_n/ui/adapters/adapter/ListModelAdapter.java
package com.kan_n.ui.adapters.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.kan_n.R;
import com.kan_n.data.interfaces.CardRepository;
import com.kan_n.data.models.Card;
import com.kan_n.data.models.ListModel;
import com.kan_n.ui.fragments.bang_space.BangSpaceViewModel;

import java.util.ArrayList;
import java.util.List;

import java.util.Collections;
import java.util.Comparator;

public class ListModelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ListModel> listModelList = new ArrayList<>();
    private Context context;
    private BangSpaceViewModel viewModel;
    private static final int VIEW_TYPE_LIST = 0;
    private static final int VIEW_TYPE_ADD = 1;

    private final OnListMenuClickListener listMenuListener;
    private boolean isEditable = false;

    public void setEditable(boolean editable) {
        this.isEditable = editable;
        notifyDataSetChanged();
    }
    public interface OnListMenuClickListener {
        void onListMenuClick(View view, ListModel listModel, int position);
    }

    private final OnAddListClickListener addListClickListener;
    public interface OnAddListClickListener {
        void onAddListClick();
    }

    private final OnItemCardClickListener cardClickListener;
    public interface OnItemCardClickListener {
        void onCardClick(Card card);
    }

    private final OnItemCardLongClickListener cardLongClickListener;

    public interface OnItemCardLongClickListener {
        void onCardLongClick(Card card, View view);
    }

    // Constructor
    public ListModelAdapter(Context context, BangSpaceViewModel viewModel,
                            OnAddListClickListener addListener,
                            OnItemCardClickListener cardListener,
                            OnItemCardLongClickListener longListener,
                            OnListMenuClickListener listMenuListener) { // <--- Thêm tham số này
        this.context = context;
        this.viewModel = viewModel;
        this.addListClickListener = addListener;
        this.cardClickListener = cardListener;
        this.cardLongClickListener = longListener;
        this.listMenuListener = listMenuListener; // <--- Gán
    }

    //Vi tri trai phai cua danh sach
    public List<ListModel> getCurrentList() {
        return listModelList;
    }


    @Override
    public int getItemCount() {
        // Nếu không được sửa, ẩn cột "Thêm danh sách" ở cuối
        return isEditable ? listModelList.size() + 1 : listModelList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == listModelList.size()) ? VIEW_TYPE_ADD : VIEW_TYPE_LIST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == 0) { // VIEW_TYPE_LIST
            View view = inflater.inflate(R.layout.item_listmodel, parent, false);
            return new ListModelViewHolder(view); // Class này cần sửa constructor bên dưới
        } else {
            View view = inflater.inflate(R.layout.item_add_list, parent, false);
            return new AddListViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ListModelViewHolder) {
            ListModel listModel = listModelList.get(position);
            if (listModel != null) {
                // Truyền position
                ((ListModelViewHolder) holder).bind(listModel, cardLongClickListener, position);
            }
        } else if (holder instanceof AddListViewHolder) {
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

    // --- Các hàm quản lý dữ liệu list ---
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
        for (int i = 0; i < listModelList.size(); i++) {
            if (listModelList.get(i).getUid().equals(updatedList.getUid())) {
                // 1. Cập nhật dữ liệu mới
                listModelList.set(i, updatedList);

                // 2. [QUAN TRỌNG] Sắp xếp lại danh sách theo 'position' tăng dần
                Collections.sort(listModelList, new Comparator<ListModel>() {
                    @Override
                    public int compare(ListModel o1, ListModel o2) {
                        return Double.compare(o1.getPosition(), o2.getPosition());
                    }
                });

                // 3. Thông báo cho RecyclerView vẽ lại toàn bộ theo thứ tự mới
                notifyDataSetChanged();
                return;
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
        if (listModelList.isEmpty()) return 0;
        return listModelList.get(listModelList.size() - 1).getPosition();
    }

    // --- ViewHolder 1: Cho Danh sách ---
    public class ListModelViewHolder extends RecyclerView.ViewHolder {
        TextView tvListTitle;
        RecyclerView rvCards;
        ImageView ivMenuDanhSach;


        private CardAdapter cardAdapter;
        private String currentListId;

        public ListModelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvListTitle = itemView.findViewById(R.id.tvTieuDeDanhSach);
            rvCards = itemView.findViewById(R.id.rvDanhSachThe);
            ivMenuDanhSach = itemView.findViewById(R.id.ivMenuDanhSach);
            rvCards.setLayoutManager(new LinearLayoutManager(context));

            // 1. Listener mở Dialog Thêm thẻ
            CardAdapter.OnAddCardClickListener addCardListener = new CardAdapter.OnAddCardClickListener() {
                @Override
                public void onAddCardClick() {
                    if (currentListId != null) showAddCardDialog(currentListId);
                }
            };

            // Listener Click thẻ
            CardAdapter.OnCardClickListener itemClickListener = new CardAdapter.OnCardClickListener() {
                @Override
                public void onCardClick(Card card) {
                    if (cardClickListener != null) {
                        cardClickListener.onCardClick(card);
                    }
                }
            };

            CardAdapter.OnCardLongClickListener itemLongClickListener = new CardAdapter.OnCardLongClickListener() {
                @Override
                public void onCardLongClick(Card card, View view) {
                    if (cardLongClickListener != null) {
                        cardLongClickListener.onCardLongClick(card, view);
                    }
                }
            };

            cardAdapter = new CardAdapter(context, addCardListener, itemClickListener, itemLongClickListener);

            rvCards.setLayoutManager(new LinearLayoutManager(context));
            rvCards.setAdapter(cardAdapter);
        }

        public void bind(ListModel listModel, OnItemCardLongClickListener longListener, int position) {
            String currentListId = listModel.getUid();
            tvListTitle.setText(listModel.getTitle());

            ivMenuDanhSach.setVisibility(isEditable ? View.VISIBLE : View.GONE);

            if (this.currentListId != null) {
                viewModel.removeCardListener(this.currentListId);
            }
            this.currentListId = listModel.getUid();
            tvListTitle.setText(listModel.getTitle());

            // Sự kiện click vào Menu danh sách
            ivMenuDanhSach.setOnClickListener(v -> {
                if (listMenuListener != null) {
                    listMenuListener.onListMenuClick(ivMenuDanhSach, listModel, position);
                }
            });

            // Tạo Adapter cho Card bên trong List
            cardAdapter = new CardAdapter(context,
                    // Add Listener
                    new CardAdapter.OnAddCardClickListener() {
                        @Override
                        public void onAddCardClick() {
                            showAddCardDialog(currentListId);
                        }
                    },
                    // Click Listener
                    new CardAdapter.OnCardClickListener() {
                        @Override
                        public void onCardClick(Card card) {
                            if (cardClickListener != null) cardClickListener.onCardClick(card);
                        }
                    },
                    // Long Click Listener
                    new CardAdapter.OnCardLongClickListener() {
                        @Override
                        public void onCardLongClick(Card card, View view) {
                            if (longListener != null) longListener.onCardLongClick(card, view);
                        }
                    }
            );
            cardAdapter.setEditable(isEditable);
            rvCards.setAdapter(cardAdapter);


            ChildEventListener cardListener = new ChildEventListener() {
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
                public void onCancelled(@NonNull DatabaseError error) { }
            };


            viewModel.listenForCards(this.currentListId, cardListener);
        }

        public void clearListener() {
            // Hàm này được gọi khi view bị hủy hoặc recycle
            if (viewModel != null && this.currentListId != null) {
                viewModel.removeCardListener(this.currentListId);
                this.currentListId = null; // Reset ID để tránh gọi remove nhiều lần
            }
        }

        private void showAddCardDialog(String listId) {
            if (context == null || viewModel == null) return;

            com.google.android.material.dialog.MaterialAlertDialogBuilder builder =
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(context);
            builder.setTitle("Thêm thẻ mới");

            final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_input_text, null);
            final EditText input = dialogView.findViewById(R.id.et_input_title);
            input.setHint("Nhập tiêu đề thẻ");
            builder.setView(dialogView);

            builder.setPositiveButton("Thêm", (dialog, which) -> {
                String title = input.getText().toString().trim();
                if (!title.isEmpty()) {
                    double newPosition = cardAdapter.getLastItemPosition() + 1000.0;
                    viewModel.createNewCard(listId, title, newPosition, new CardRepository.GeneralCallback() {
                        @Override
                        public void onSuccess() { }
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

    // --- ViewHolder 2: Cho Nút "Thêm Danh sách" ---
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