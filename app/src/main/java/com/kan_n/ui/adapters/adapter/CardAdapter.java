// Đặt tại: src/main/java/com/kan_n/ui/adapters/adapter/CardAdapter.java
package com.kan_n.ui.adapters.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kan_n.R;
import com.kan_n.data.models.Card;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Card> cardList = new ArrayList<>();
    private Context context;

    // Hằng số cho kiểu xem
    private static final int VIEW_TYPE_CARD = 0;
    private static final int VIEW_TYPE_ADD_CARD = 1;

    // 1. Listener cho nút "Thêm thẻ"
    private final OnAddCardClickListener addCardClickListener;

    // Listener cho việc Click vào thẻ để xem chi tiết
    private final OnCardClickListener cardClickListener;

    public interface OnAddCardClickListener {
        void onAddCardClick();
    }

    // Interface cho click vào thẻ
    public interface OnCardClickListener {
        void onCardClick(Card card);
    }

    public CardAdapter(Context context, OnAddCardClickListener addListener, OnCardClickListener cardListener) {
        this.context = context;
        this.addCardClickListener = addListener;
        this.cardClickListener = cardListener;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == cardList.size()) ? VIEW_TYPE_ADD_CARD : VIEW_TYPE_CARD;
    }

    @Override
    public int getItemCount() {
        return cardList.size() + 1;
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
        if (holder.getItemViewType() == VIEW_TYPE_CARD) {
            Card card = cardList.get(position);
            if (card == null) return;

            ((CardViewHolder) holder).bind(card, context, cardClickListener);

        } else {
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

    public void addCard(Card card) {
        int position = getInsertPosition(card);
        cardList.add(position, card);
        notifyItemInserted(position);
    }

    public void updateCard(Card updatedCard) {
        String cardId = updatedCard.getUid();
        for (int i = 0; i < cardList.size(); i++) {
            if (cardList.get(i).getUid().equals(cardId)) {
                boolean positionChanged = cardList.get(i).getPosition() != updatedCard.getPosition();
                cardList.set(i, updatedCard);
                if (positionChanged) {
                    notifyDataSetChanged();
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

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCoverImage = itemView.findViewById(R.id.ivAnhBiaThe);
            rvTags = itemView.findViewById(R.id.rvNhanThe);
            cbComplete = itemView.findViewById(R.id.cbHoanThanhThe);
            tvCardTitle = itemView.findViewById(R.id.tvTieuDeThe);
            layoutFooter = itemView.findViewById(R.id.layoutChanThe);
            tvAttachments = itemView.findViewById(R.id.tvDinhKemThe);
            tvChecklist = itemView.findViewById(R.id.tvChecklistThe);
        }

        // Thêm tham số listener vào hàm bind
        public void bind(final Card card, Context context, final OnCardClickListener listener) {
            // 1. Tieu de va Checkbox
            tvCardTitle.setText(card.getTitle());
            cbComplete.setChecked(card.isCompleted());

            // Bắt sự kiện click vào Tiêu đề thẻ
            tvCardTitle.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCardClick(card);
                }
            });

            // (Các phần bind ảnh bìa, tag, checklist...)
            String coverUrl = card.getCoverImageUrl();
            if (coverUrl != null && !coverUrl.isEmpty()) {
                ivCoverImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(coverUrl).centerCrop().into(ivCoverImage);
            } else {
                ivCoverImage.setVisibility(View.GONE);
            }

            boolean coNhan = card.getTagIds() != null && !card.getTagIds().isEmpty();
            rvTags.setVisibility(coNhan ? View.VISIBLE : View.GONE);

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
}