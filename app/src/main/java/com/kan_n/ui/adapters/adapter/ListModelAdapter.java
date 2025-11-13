package com.kan_n.ui.adapters.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // <-- Thêm import
import android.widget.LinearLayout; // <-- Thêm import
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.kan_n.R;
import com.kan_n.data.models.Card;
import com.kan_n.data.models.ListModel;
import com.kan_n.ui.fragments.bang_space.BangSpaceViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListModelAdapter extends RecyclerView.Adapter<ListModelAdapter.ListModelViewHolder> {

    private List<ListModel> listModelList = new ArrayList<>();
    private Context context;
    private BangSpaceViewModel viewModel;

    // Khong can Map o day, ViewHolder se tu quan ly
    // private Map<String, CardAdapter> adapterMap = new HashMap<>();

    public ListModelAdapter(Context context, BangSpaceViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public ListModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_listmodel, parent, false);
        return new ListModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListModelViewHolder holder, int position) {
        ListModel listModel = listModelList.get(position);
        if (listModel == null || listModel.getUid() == null) return;

        holder.bind(listModel);
    }

    @Override
    public int getItemCount() {
        return listModelList.size();
    }

    /**
     * Rất quan trọng: Gỡ bỏ listener khi View bị recycle
     */
    @Override
    public void onViewRecycled(@NonNull ListModelViewHolder holder) {
        super.onViewRecycled(holder);
        // Don dep listener khi ViewHolder bi tai su dung
        holder.clearListener();
    }

    // --- CÁC PHƯƠNG THỨC QUẢN LÝ DỮ LIỆU (CHO LISTENER GỌI) ---

    // Tim vi tri de chen danh sach (sap xep theo position)
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
                // (Xu ly di chuyen neu 'position' thay doi)
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

    // --- ViewHolder ---
    public class ListModelViewHolder extends RecyclerView.ViewHolder {
        TextView tvListTitle;
        RecyclerView rvCards;
        LinearLayout layoutAddCard; // <-- Thay đổi
        ImageView ivMenuDanhSach; // <-- Thêm

        private CardAdapter cardAdapter;
        private ChildEventListener cardListener;
        private String currentListId;

        public ListModelViewHolder(@NonNull View itemView) {
            super(itemView);
            // === [FIX ID] ===
            // Anh xa View (Su dung ID tu item_listmodel.xml)
            tvListTitle = itemView.findViewById(R.id.tvTieuDeDanhSach);
            rvCards = itemView.findViewById(R.id.rvDanhSachThe);
            layoutAddCard = itemView.findViewById(R.id.layoutThemThe);
            ivMenuDanhSach = itemView.findViewById(R.id.ivMenuDanhSach);

            // Cài đặt CardAdapter
            cardAdapter = new CardAdapter(context);
            rvCards.setLayoutManager(new LinearLayoutManager(context));
            rvCards.setAdapter(cardAdapter);
        }

        public void bind(ListModel listModel) {
            currentListId = listModel.getUid();
            tvListTitle.setText(listModel.getTitle());

            // Tao ChildEventListener cho cac the (card)
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
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // (Xu ly sap xep lai)
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ListModelAdapter", "Error listenForCards: " + error.getMessage());
                }
            };

            // Bắt đầu lắng nghe
            viewModel.listenForCards(currentListId, cardListener);

            // (Them listener cho nut Them The, Menu...)
            // layoutAddCard.setOnClickListener(v -> { ... });
            // ivMenuDanhSach.setOnClickListener(v -> { ... });
        }

        // Don dep listener cua ViewHolder nay
        public void clearListener() {
            if (viewModel != null && currentListId != null) {
                // === [FIX LOGIC] ===
                // Goi ViewModel de go bo listener cu the nay
                viewModel.removeCardListener(currentListId);
            }
        }
    }
}