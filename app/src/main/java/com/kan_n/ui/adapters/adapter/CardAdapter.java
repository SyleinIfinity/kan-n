// Đặt tại: src/main/java/com/kan_n/ui/adapters/adapter/CardAdapter.java (CẬP NHẬT)
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

// ✨ SỬA ĐỔI: Chuyển sang dùng RecyclerView.ViewHolder chung
public class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Card> cardList = new ArrayList<>();
    private Context context;

    // ✨ BƯỚC 1: Thêm hằng số cho kiểu xem
    private static final int VIEW_TYPE_CARD = 0;
    private static final int VIEW_TYPE_ADD_CARD = 1;

    // ✨ BƯỚC 2: Thêm interface và biến listener cho nút "Thêm"
    private final OnAddCardClickListener addCardClickListener;

    public interface OnAddCardClickListener {
        void onAddCardClick();
    }

    // ✨ BƯỚC 3: Cập nhật Constructor
    public CardAdapter(Context context, OnAddCardClickListener listener) {
        this.context = context;
        this.addCardClickListener = listener;
    }

    // ✨ BƯỚC 4: Thêm getItemViewType
    @Override
    public int getItemViewType(int position) {
        return (position == cardList.size()) ? VIEW_TYPE_ADD_CARD : VIEW_TYPE_CARD;
    }

    // ✨ BƯỚC 5: Cập nhật getItemCount
    @Override
    public int getItemCount() {
        return cardList.size() + 1; // +1 cho nút "Thêm thẻ"
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ✨ BƯỚC 6: Xử lý 2 kiểu ViewHolder
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_CARD) {
            View view = inflater.inflate(R.layout.item_card, parent, false);
            return new CardViewHolder(view);
        } else {
            // Dùng layout item_add_card.xml mới
            View view = inflater.inflate(R.layout.item_add_card, parent, false);
            return new AddCardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // ✨ BƯỚC 7: Cập nhật onBindViewHolder
        if (holder.getItemViewType() == VIEW_TYPE_CARD) {
            // Đây là CardViewHolder
            Card card = cardList.get(position);
            if (card == null) return;
            ((CardViewHolder) holder).bind(card, context);
        } else {
            // Đây là AddCardViewHolder
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
        notifyItemInserted(position); // Chèn vào trước nút "Thêm"
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

    // ✨ BƯỚC 8: Thêm hàm lấy position cuối
    public double getLastItemPosition() {
        if (cardList.isEmpty()) {
            return 0; // Vị trí đầu tiên
        }
        // Trả về vị trí của thẻ cuối cùng
        return cardList.get(cardList.size() - 1).getPosition();
    }

    // --- ViewHolder 1: Cho Thẻ (Giữ nguyên) ---
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
            ivCoverImage = itemView.findViewById(R.id.ivAnhBiaThe);
            rvTags = itemView.findViewById(R.id.rvNhanThe);
            cbComplete = itemView.findViewById(R.id.cbHoanThanhThe);
            tvCardTitle = itemView.findViewById(R.id.tvTieuDeThe);
            layoutFooter = itemView.findViewById(R.id.layoutChanThe);
            ivMenu = itemView.findViewById(R.id.ivMenuThe);
            tvAttachments = itemView.findViewById(R.id.tvDinhKemThe);
            tvChecklist = itemView.findViewById(R.id.tvChecklistThe);
        }

        // Thêm 'context' vào bind
        public void bind(final Card card, Context context) {
            // 1. Tieu de va Checkbox
            tvCardTitle.setText(card.getTitle());
            cbComplete.setChecked(card.isCompleted());

            // 2. Anh bia (Cover Image)
            String coverUrl = card.getCoverImageUrl();
            if (coverUrl != null && !coverUrl.isEmpty()) {
                ivCoverImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(coverUrl)
                        .centerCrop()
                        .into(ivCoverImage);
            } else {
                ivCoverImage.setVisibility(View.GONE);
            }

            // 3. Nhan the (Tags)
            boolean coNhan = card.getTagIds() != null && !card.getTagIds().isEmpty();
            rvTags.setVisibility(coNhan ? View.VISIBLE : View.GONE);
            // (Cần logic để load TagAdapter vào rvTags)

            // 4. Dinh kem (Attachments)
            int attachmentCount = card.getAttachmentCount();
            if (attachmentCount > 0) {
                tvAttachments.setVisibility(View.VISIBLE);
                tvAttachments.setText(String.valueOf(attachmentCount));
            } else {
                tvAttachments.setVisibility(View.GONE);
            }

            // 5. Checklist
            int checklistTotal = card.getChecklistTotal();
            int checklistCompleted = card.getChecklistCompleted();
            if (checklistTotal > 0) {
                tvChecklist.setVisibility(View.VISIBLE);
                tvChecklist.setText(String.format(Locale.getDefault(), "%d/%d", checklistCompleted, checklistTotal));
            } else {
                tvChecklist.setVisibility(View.GONE);
            }

            // 6. Hien thi Chan The (Footer)
            if (attachmentCount > 0 || checklistTotal > 0) {
                layoutFooter.setVisibility(View.VISIBLE);
            } else {
                layoutFooter.setVisibility(View.GONE);
            }
        }
    }

    // --- ✨ BƯỚC 9: Thêm ViewHolder 2: Cho Nút "Thêm Thẻ" ---
    public static class AddCardViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutThemThe;

        public AddCardViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ id từ item_add_card.xml
            layoutThemThe = itemView.findViewById(R.id.layoutThemThe);
        }

        public void bind(final OnAddCardClickListener listener) {
            // Gán sự kiện click cho toàn bộ item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddCardClick();
                }
            });
        }
    }
}