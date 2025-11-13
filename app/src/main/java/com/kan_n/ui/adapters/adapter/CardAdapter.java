package com.kan_n.ui.adapters.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox; // <-- Thêm
import android.widget.ImageView; // <-- Thêm
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // <-- Thêm
import com.kan_n.R;
import com.kan_n.data.models.Card;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // <-- Thêm

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    // Danh sách thẻ mà adapter này quản lý
    private List<Card> cardList = new ArrayList<>();
    private Context context;

    // Constructor chi can Context
    public CardAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cardList.get(position);
        if (card == null) return;

        // === [LOGIC MO RONG] ===
        // Gan du lieu tu model Card vao item_card.xml

        // 1. Tieu de va Checkbox
        holder.tvCardTitle.setText(card.getTitle());
        holder.cbComplete.setChecked(card.isCompleted());

        // 2. Anh bia (Cover Image)
        String coverUrl = card.getCoverImageUrl();
        if (coverUrl != null && !coverUrl.isEmpty()) {
            holder.ivCoverImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(coverUrl)
                    .centerCrop()
                    .into(holder.ivCoverImage);
        } else {
            holder.ivCoverImage.setVisibility(View.GONE);
        }

        // 3. Nhan the (Tags)
        // (Hien tai model Card co tagIds, can logic de lay Tag tu ID)
        boolean coNhan = card.getTagIds() != null && !card.getTagIds().isEmpty();
        if (coNhan) {
            holder.rvTags.setVisibility(View.VISIBLE);
            // (Can mot TagAdapter de hien thi cac tagIds)
        } else {
            holder.rvTags.setVisibility(View.GONE);
        }

        // 4. Dinh kem (Attachments)
        int attachmentCount = card.getAttachmentCount();
        if (attachmentCount > 0) {
            holder.tvAttachments.setVisibility(View.VISIBLE);
            holder.tvAttachments.setText(String.valueOf(attachmentCount));
        } else {
            holder.tvAttachments.setVisibility(View.GONE);
        }

        // 5. Checklist
        int checklistTotal = card.getChecklistTotal();
        int checklistCompleted = card.getChecklistCompleted();
        if (checklistTotal > 0) {
            holder.tvChecklist.setVisibility(View.VISIBLE);
            holder.tvChecklist.setText(String.format(Locale.getDefault(), "%d/%d", checklistCompleted, checklistTotal));
            // (Co the set icon checked/unchecked o day)
        } else {
            holder.tvChecklist.setVisibility(View.GONE);
        }

        // 6. Hien thi Chan The (Footer) neu co Dinh Kem hoac Checklist
        if (attachmentCount > 0 || checklistTotal > 0) {
            holder.layoutFooter.setVisibility(View.VISIBLE);
        } else {
            holder.layoutFooter.setVisibility(View.GONE);
        }

        // (Them listener cho Checkbox, Menu...)
        // holder.cbComplete.setOnCheckedChangeListener((buttonView, isChecked) -> { ... });
        // holder.ivMenu.setOnClickListener(v -> { ... });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    // --- CÁC PHƯƠNG THỨC QUẢN LÝ DỮ LIỆU (CHO LISTENER GỌI) ---

    // Tim vi tri de chen the vao (sap xep theo position)
    private int getInsertPosition(Card card) {
        for (int i = 0; i < cardList.size(); i++) {
            if (cardList.get(i).getPosition() > card.getPosition()) {
                return i;
            }
        }
        return cardList.size();
    }

    public void addCard(Card card) {
        int position = getInsertPosition(card);
        cardList.add(position, card);
        notifyItemInserted(position);
    }

    public void updateCard(Card updatedCard) {
        String cardId = updatedCard.getUid();
        for (int i = 0; i < cardList.size(); i++) {
            if (cardList.get(i).getUid().equals(cardId)) {
                // Kiem tra xem co can di chuyen vi tri khong
                boolean positionChanged = cardList.get(i).getPosition() != updatedCard.getPosition();

                cardList.set(i, updatedCard);

                if (positionChanged) {
                    // Neu position thay doi, can sap xep lai va cap nhat
                    notifyDataSetChanged(); // (Cach don gian)
                    // (Cach phuc tap: remove, add, notifyItemMoved)
                } else {
                    notifyItemChanged(i);
                }
                break;
            }
        }
    }

    public void removeCard(String cardId) {
        for (int i = 0; i < cardList.size(); i++) {
            if (cardList.get(i).getUid().equals(cardId)) {
                cardList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    // === [FIX ID] ===
    // --- ViewHolder (Khop voi item_card.xml) ---
    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCoverImage;
        RecyclerView rvTags;
        CheckBox cbComplete;
        TextView tvCardTitle;
        LinearLayout layoutFooter;
        ImageView ivMenu;
        TextView tvAttachments;
        TextView tvChecklist;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ View (Sử dụng ID từ item_card.xml)
            ivCoverImage = itemView.findViewById(R.id.ivAnhBiaThe);
            rvTags = itemView.findViewById(R.id.rvNhanThe);
            cbComplete = itemView.findViewById(R.id.cbHoanThanhThe);
            tvCardTitle = itemView.findViewById(R.id.tvTieuDeThe);
            layoutFooter = itemView.findViewById(R.id.layoutChanThe);
            ivMenu = itemView.findViewById(R.id.ivMenuThe);
            tvAttachments = itemView.findViewById(R.id.tvDinhKemThe);
            tvChecklist = itemView.findViewById(R.id.tvChecklistThe);
        }
    }
}