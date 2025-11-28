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

public class ListModelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ListModel> listModelList = new ArrayList<>();
    private Context context;
    private BangSpaceViewModel viewModel;

    private static final int VIEW_TYPE_LIST = 0;
    private static final int VIEW_TYPE_ADD = 1;

    private final OnAddListClickListener addListClickListener;

    private final OnItemCardClickListener cardClickListener;

    public interface OnAddListClickListener {
        void onAddListClick();
    }

    public interface OnItemCardClickListener {
        void onCardClick(Card card);
    }

    // Constructor
    public ListModelAdapter(Context context, BangSpaceViewModel viewModel,
                            OnAddListClickListener addListener,
                            OnItemCardClickListener cardListener) {
        this.context = context;
        this.viewModel = viewModel;
        this.addListClickListener = addListener;
        this.cardClickListener = cardListener;
    }

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
        if (listModelList.isEmpty()) return 0;
        return listModelList.get(listModelList.size() - 1).getPosition();
    }

    // --- ViewHolder 1: Cho Danh sách ---
    public class ListModelViewHolder extends RecyclerView.ViewHolder {
        TextView tvListTitle;
        RecyclerView rvCards;
        ImageView ivMenuDanhSach;

        private CardAdapter cardAdapter;
        private ChildEventListener cardListener;
        private String currentListId;

        public ListModelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvListTitle = itemView.findViewById(R.id.tvTieuDeDanhSach);
            rvCards = itemView.findViewById(R.id.rvDanhSachThe);
            ivMenuDanhSach = itemView.findViewById(R.id.ivMenuDanhSach);

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

            cardAdapter = new CardAdapter(context, addCardListener, itemClickListener);

            rvCards.setLayoutManager(new LinearLayoutManager(context));
            rvCards.setAdapter(cardAdapter);
        }

        public void bind(ListModel listModel) {
            currentListId = listModel.getUid();
            tvListTitle.setText(listModel.getTitle());

            cardListener = new ChildEventListener() {
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