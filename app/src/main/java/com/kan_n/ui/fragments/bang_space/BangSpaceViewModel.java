package com.kan_n.ui.fragments.bang_space;

import androidx.lifecycle.ViewModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.kan_n.data.interfaces.CardRepository;
import com.kan_n.data.interfaces.ListRepository;
import com.kan_n.data.repository.CardRepositoryImpl;
import com.kan_n.data.repository.ListRepositoryImpl;
import com.kan_n.utils.FirebaseUtils;

import java.util.HashMap;
import java.util.Map;

public class BangSpaceViewModel extends ViewModel {

    private final ListRepository listRepository;
    private final CardRepository cardRepository;

    // Database references
    private final DatabaseReference listsRef = FirebaseUtils.getDatabaseInstance().getReference("lists");
    private final DatabaseReference cardsRef = FirebaseUtils.getDatabaseInstance().getReference("cards");

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

        // === [SỬA LỖI] ===
        // Chỉ lọc theo boardId. Viec sap xep se do Adapter dam nhiem.
        this.listsQuery = listsRef.orderByChild("boardId").equalTo(boardId);

        this.listsQuery.addChildEventListener(listener);
    }

    /**
     * Bắt đầu lắng nghe các thẻ (card) cho MỘT danh sách
     */
    public void listenForCards(String listId, ChildEventListener listener) {

        // === [SỬA LỖI] ===
        // Chỉ lọc theo listId. Viec sap xep se do Adapter dam nhiem.
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

    // (Them cac phuong thuc action khac o day...)

}