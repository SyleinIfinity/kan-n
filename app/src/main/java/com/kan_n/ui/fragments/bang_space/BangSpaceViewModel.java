package com.kan_n.ui.fragments.bang_space;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.interfaces.CardRepository;
import com.kan_n.data.interfaces.ListRepository;
import com.kan_n.data.models.Tag;
import com.kan_n.data.repository.CardRepositoryImpl;
import com.kan_n.data.repository.ListRepositoryImpl;
import com.kan_n.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BangSpaceViewModel extends ViewModel {

    private final ListRepository listRepository;
    private final CardRepository cardRepository;

    // Database references
    private final DatabaseReference listsRef = FirebaseUtils.getDatabaseInstance().getReference("lists");
    private final DatabaseReference cardsRef = FirebaseUtils.getDatabaseInstance().getReference("cards");

    private final DatabaseReference tagsRef = FirebaseUtils.getDatabaseInstance().getReference("tags");

    // Lưu trữ các Query và Listener để có thể gỡ khi Fragment bị hủy
    private Query listsQuery;
    private ChildEventListener listsListener;

    // Cần thiết để gỡ bỏ listener của thẻ khi list-item bị recycle
    private Map<String, Query> cardQueries = new HashMap<>();
    private Map<String, ChildEventListener> cardListeners = new HashMap<>();

    public BangSpaceViewModel() {
        this.listRepository = new ListRepositoryImpl();
        this.cardRepository = new CardRepositoryImpl();
    }

    /**
     * Bắt đầu lắng nghe các danh sách (cột)
     */
    public void listenForLists(String boardId, ChildEventListener listener) {
        this.listsListener = listener;

        this.listsQuery = listsRef.orderByChild("boardId").equalTo(boardId);

        this.listsQuery.addChildEventListener(listener);
    }

    /**
     * Bắt đầu lắng nghe các thẻ (card) cho MỘT danh sách
     */
    public void listenForCards(String listId, ChildEventListener listener) {

        Query cardQuery = cardsRef.orderByChild("listId").equalTo(listId);

        cardQueries.put(listId, cardQuery);
        cardListeners.put(listId, listener);

        cardQuery.addChildEventListener(listener);
    }

    /**
     * [QUAN TRỌNG] Gỡ listener của thẻ khi ListModelViewHolder bị recycle
     */
    public void removeCardListener(String listId) {
        ChildEventListener listener = cardListeners.get(listId);
        Query query = cardQueries.get(listId);

        if (listener != null && query != null) {
            query.removeEventListener(listener);
            cardListeners.remove(listId);
            cardQueries.remove(listId);
        }
    }


    /**
     * Gỡ tất cả các listener khi ViewModel bị hủy
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        // Gỡ listener của danh sách
        if (listsQuery != null && listsListener != null) {
            listsQuery.removeEventListener(listsListener);
        }

        // Gỡ tất cả listener của các thẻ
        for (Map.Entry<String, Query> entry : cardQueries.entrySet()) {
            ChildEventListener listener = cardListeners.get(entry.getKey());
            if (listener != null) {
                entry.getValue().removeEventListener(listener);
            }
        }
        cardQueries.clear();
        cardListeners.clear();
    }

    // --- CÁC PHƯƠNG THỨC ACTION (GOI REPOSITORY) ---

    public void createNewList(String boardId, String title, double position, ListRepository.GeneralCallback callback) {
        listRepository.createList(boardId, title, position, callback);
    }

    public void createNewCard(String listId, String title, double position, CardRepository.GeneralCallback callback) {
        cardRepository.createCard(listId, title, position, callback);
    }

    public void updateCardCompletion(String cardId, boolean isCompleted, CardRepository.GeneralCallback callback) {
        cardRepository.setCardCompleted(cardId, isCompleted, callback);
    }

    // 1. Đổi tên Card
    public void updateCardTitle(String cardId, String newTitle) {
        cardsRef.child(cardId).child("title").setValue(newTitle);
    }

    // 2. Xóa Card
    public void deleteCard(String cardId) {
        cardsRef.child(cardId).removeValue();
    }

    // 3. Cập nhật Tag cho Card (1 Card 1 Tag)
    public void updateCardTag(String cardId, Tag tag) {
        // Lưu Tag ID vào map (như cấu trúc cũ)
        Map<String, Object> updates = new HashMap<>();

        // Xóa hết tag cũ, chỉ để lại 1 tag mới
        Map<String, Boolean> newTags = new HashMap<>();
        newTags.put(tag.getUid(), true);
        updates.put("tagIds", newTags);

        // [QUAN TRỌNG] Lưu màu của tag trực tiếp vào card để hiển thị nhanh
        updates.put("labelColor", tag.getColor());

        cardsRef.child(cardId).updateChildren(updates);
    }

    // 4. Lấy danh sách tất cả các Tag (để hiển thị dialog chọn)
    public void loadAllTags(TagListCallback callback) {
        tagsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Tag> tags = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Tag tag = data.getValue(Tag.class);
                    if (tag != null) {
                        tag.setUid(data.getKey());
                        tags.add(tag);
                    }
                }
                callback.onTagsLoaded(tags);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public interface TagListCallback {
        void onTagsLoaded(List<Tag> tags);
        void onError(String message);
    }

}