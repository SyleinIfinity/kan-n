// Đặt tại: src/main/java/com/kan_n/ui/adapters/adapter/CardAdapter.java
package com.kan_n.ui.adapters.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kan_n.R;
import com.kan_n.data.models.Card;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Collections;
import java.util.Comparator;

public class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Card> cardList = new ArrayList<>();
    private Context context;

    private boolean isEditable = false;
    // Hằng số cho kiểu xem
    private static final int VIEW_TYPE_CARD = 0;
    private static final int VIEW_TYPE_ADD_CARD = 1;

    private final OnAddCardClickListener addCardClickListener;

    public interface OnAddCardClickListener {
        void onAddCardClick();
    }

    private final OnCardClickListener cardClickListener;

    public interface OnCardClickListener {
        void onCardClick(Card card);
    }


    private final OnCardLongClickListener cardLongClickListener;

    public interface OnCardLongClickListener {
        void onCardLongClick(Card card, View view);
    }


    public CardAdapter(Context context, OnAddCardClickListener addListener, OnCardClickListener cardListener, OnCardLongClickListener longClickListener) {
        this.context = context;
        this.addCardClickListener = addListener;
        this.cardClickListener = cardListener;
        this.cardLongClickListener = longClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == cardList.size()) ? VIEW_TYPE_ADD_CARD : VIEW_TYPE_CARD;
    }





    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_CARD) {
            View view = inflater.inflate(R.layout.item_card, parent, false);
            return new CardViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_add_card, parent, false);
            return new AddCardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CardViewHolder) {
            Card card = cardList.get(position);
            // Truyền isEditable vào bind
            ((CardViewHolder) holder).bind(card, context, cardClickListener, cardLongClickListener, isEditable);
        }
        else if (holder instanceof AddCardViewHolder) {
            // Gán OnAddCardClickListener cho nút bấm
            ((AddCardViewHolder) holder).bind(addCardClickListener);
        }
    }

    // --- CÁC PHƯƠNG THỨC QUẢN LÝ DỮ LIỆU ---
    private int getInsertPosition(Card card) {
        for (int i = 0; i < cardList.size(); i++) {
            if (cardList.get(i).getPosition() > card.getPosition()) {
                return i;
            }
        }
        return cardList.size();
    }


    public void setEditable(boolean editable) {
        this.isEditable = editable;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return isEditable ? cardList.size() + 1 : cardList.size();
    }


    public void addCard(Card card) {
        int position = getInsertPosition(card);
        cardList.add(position, card);
        notifyItemInserted(position);
    }

    // [CẬP NHẬT] Hàm này giúp thẻ tự nhảy vị trí ngay lập tức khi chọn Lên/Xuống
    public void updateCard(Card updatedCard) {
        for (int i = 0; i < cardList.size(); i++) {
            if (cardList.get(i).getUid().equals(updatedCard.getUid())) {
                cardList.set(i, updatedCard);

                Collections.sort(cardList, new Comparator<Card>() {
                    @Override
                    public int compare(Card o1, Card o2) {
                        return Double.compare(o1.getPosition(), o2.getPosition());
                    }
                });
                notifyDataSetChanged();
                return;
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

    public double getLastItemPosition() {
        if (cardList.isEmpty()) {
            return 0;
        }
        return cardList.get(cardList.size() - 1).getPosition();
    }

    // --- ViewHolder 1: Cho Thẻ ---
    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCoverImage;
        RecyclerView rvTags;
        CheckBox cbComplete;
        TextView tvCardTitle;
        LinearLayout layoutFooter;
        TextView tvAttachments;
        TextView tvChecklist;

        ConstraintLayout layoutRoot;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCoverImage = itemView.findViewById(R.id.ivAnhBiaThe);
            rvTags = itemView.findViewById(R.id.rvNhanThe);
            cbComplete = itemView.findViewById(R.id.cbHoanThanhThe);
            tvCardTitle = itemView.findViewById(R.id.tvTieuDeThe);
            layoutFooter = itemView.findViewById(R.id.layoutChanThe);
            tvAttachments = itemView.findViewById(R.id.tvDinhKemThe);
            tvChecklist = itemView.findViewById(R.id.tvChecklistThe);
            layoutRoot = (ConstraintLayout) itemView.findViewById(R.id.tvTieuDeThe).getParent();
        }

        // Thêm tham số listener vào hàm bind
        public void bind(final Card card, Context context,
                         final OnCardClickListener listener,
                         final OnCardLongClickListener longListener, boolean isEditable) {

            tvCardTitle.setText(card.getTitle());
            cbComplete.setChecked(card.isCompleted());

            View.OnClickListener commonClickListener = v -> {
                if (listener != null) listener.onCardClick(card);
            };

            // Gán click cho cả itemView và Title
            itemView.setOnClickListener(commonClickListener);
            tvCardTitle.setOnClickListener(commonClickListener);

            // Tạo một OnLongClickListener chung
            View.OnLongClickListener commonLongListener = v -> {
                if (longListener != null) {
                    longListener.onCardLongClick(card, itemView);
                    return true; // Trả về true để báo là sự kiện đã được xử lý
                }
                return false;
            };

            if (isEditable) {
                itemView.setOnLongClickListener(commonLongListener);
                tvCardTitle.setOnLongClickListener(commonLongListener);
            } else {
                itemView.setOnLongClickListener(null);
                tvCardTitle.setOnLongClickListener(null);
            }


            // --- MÀU SẮC ---
            if (card.getLabelColor() != null && !card.getLabelColor().isEmpty()) {
                try {
                    layoutRoot.setBackgroundColor(Color.parseColor(card.getLabelColor()));
                } catch (IllegalArgumentException e) {
                    layoutRoot.setBackgroundColor(Color.parseColor("#CCF2FB"));
                }
            } else {
                layoutRoot.setBackgroundColor(Color.parseColor("#CCF2FB"));
            }

            // --- ẢNH BÌA ---
            String coverUrl = card.getCoverImageUrl();
            if (coverUrl != null && !coverUrl.isEmpty()) {
                ivCoverImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(coverUrl).centerCrop().into(ivCoverImage);
            } else {
                ivCoverImage.setVisibility(View.GONE);
            }

            rvTags.setVisibility(View.GONE);

            // --- FOOTER INFO ---
            int attachmentCount = card.getAttachmentCount();
            if (attachmentCount > 0) {
                tvAttachments.setVisibility(View.VISIBLE);
                tvAttachments.setText(String.valueOf(attachmentCount));
            } else {
                tvAttachments.setVisibility(View.GONE);
            }

            int checklistTotal = card.getChecklistTotal();
            int checklistCompleted = card.getChecklistCompleted();
            if (checklistTotal > 0) {
                tvChecklist.setVisibility(View.VISIBLE);
                tvChecklist.setText(String.format(Locale.getDefault(), "%d/%d", checklistCompleted, checklistTotal));
            } else {
                tvChecklist.setVisibility(View.GONE);
            }

            if (attachmentCount > 0 || checklistTotal > 0) {
                layoutFooter.setVisibility(View.VISIBLE);
            } else {
                layoutFooter.setVisibility(View.GONE);
            }
        }
    }

    // --- ViewHolder 2: Cho Nút "Thêm Thẻ" ---
    public static class AddCardViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutThemThe;

        public AddCardViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutThemThe = itemView.findViewById(R.id.layoutThemThe);
        }

        public void bind(final OnAddCardClickListener listener) {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddCardClick();
                }
            });
        }
    }
    public void clearData() {
        this.cardList.clear();
        notifyDataSetChanged();
    }
}