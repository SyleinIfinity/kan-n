package com.kan_n.ui.fragments.bang_space;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.interfaces.BoardRepository;
import com.kan_n.data.interfaces.CardRepository;
import com.kan_n.data.interfaces.ListRepository;
import com.kan_n.data.models.Card;
import com.kan_n.data.models.ListModel;
import com.kan_n.data.models.Tag;
import com.kan_n.data.repository.BoardRepositoryImpl;
import com.kan_n.data.repository.CardRepositoryImpl;
import com.kan_n.data.repository.ListRepositoryImpl;
import com.kan_n.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BangSpaceViewModel extends ViewModel {

    private final ListRepository listRepository;
    private final CardRepository cardRepository;

    // Database references
    private final DatabaseReference listsRef = FirebaseUtils.getDatabaseInstance().getReference("lists");
    private final DatabaseReference cardsRef = FirebaseUtils.getDatabaseInstance().getReference("cards");
    private final DatabaseReference tagsRef = FirebaseUtils.getDatabaseInstance().getReference("tags");

    // Listener Management
    private Query listsQuery;
    private ChildEventListener listsListener;
    private Map<String, Query> cardQueries = new HashMap<>();
    private Map<String, ChildEventListener> cardListeners = new HashMap<>();
    private final BoardRepository boardRepository;

    public BangSpaceViewModel() {
        this.listRepository = new ListRepositoryImpl();
        this.cardRepository = new CardRepositoryImpl();
        this.boardRepository = new BoardRepositoryImpl();
    }

    public void getBoardDetails(String boardId, BoardRepository.BoardCallback callback) {
        boardRepository.getBoardDetails(boardId, callback);
    }

    // =========================================================================
    // PHẦN 1: LISTENER (GIỮ NGUYÊN)
    // =========================================================================
    public void listenForLists(String boardId, ChildEventListener listener) {
        this.listsListener = listener;
        this.listsQuery = listsRef.orderByChild("boardId").equalTo(boardId);
        this.listsQuery.addChildEventListener(listener);
    }

    public void listenForCards(String listId, ChildEventListener listener) {
        Query cardQuery = cardsRef.orderByChild("listId").equalTo(listId);
        cardQueries.put(listId, cardQuery);
        cardListeners.put(listId, listener);
        cardQuery.addChildEventListener(listener);
    }

    public void removeCardListener(String listId) {
        ChildEventListener listener = cardListeners.get(listId);
        Query query = cardQueries.get(listId);
        if (listener != null && query != null) {
            query.removeEventListener(listener);
            cardListeners.remove(listId);
            cardQueries.remove(listId);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listsQuery != null && listsListener != null) {
            listsQuery.removeEventListener(listsListener);
        }
        for (Map.Entry<String, Query> entry : cardQueries.entrySet()) {
            ChildEventListener listener = cardListeners.get(entry.getKey());
            if (listener != null) entry.getValue().removeEventListener(listener);
        }
        cardQueries.clear();
        cardListeners.clear();
    }

    // =========================================================================
    // PHẦN 2: CRUD CƠ BẢN (GIỮ NGUYÊN)
    // =========================================================================
    public void createNewList(String boardId, String title, double position, ListRepository.GeneralCallback callback) {
        listRepository.createList(boardId, title, position, callback);
    }

    public void createNewCard(String listId, String title, double position, CardRepository.GeneralCallback callback) {
        cardRepository.createCard(listId, title, position, callback);
    }

    public void updateCardCompletion(String cardId, boolean isCompleted, CardRepository.GeneralCallback callback) {
        cardRepository.setCardCompleted(cardId, isCompleted, callback);
    }

    public void updateCardTitle(String cardId, String newTitle) {
        cardsRef.child(cardId).child("title").setValue(newTitle);
    }

    public void deleteCard(String cardId) {
        cardsRef.child(cardId).removeValue();
    }

    public void updateListTitle(String listId, String newTitle) {
        listsRef.child(listId).child("title").setValue(newTitle);
    }

    public void deleteList(String listId) {
        listsRef.child(listId).removeValue();
    }

    // =========================================================================
    // PHẦN 3: LOGIC ASSIGNED-TAG (GIAI ĐOẠN 2)
    // =========================================================================
    public interface TagListCallback {
        void onTagsLoaded(List<Tag> tags);
        void onError(String message);
    }

    public void loadEligibleTags(String boardId, String currentListId, TagListCallback callback) {
        listsRef.orderByChild("boardId").equalTo(boardId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot listSnapshot) {
                        List<ListModel> allLists = new ArrayList<>();
                        ListModel currentList = null;
                        for (DataSnapshot ds : listSnapshot.getChildren()) {
                            ListModel list = ds.getValue(ListModel.class);
                            if (list != null) {
                                list.setUid(ds.getKey());
                                allLists.add(list);
                                if (list.getUid().equals(currentListId)) currentList = list;
                            }
                        }
                        if (currentList == null) {
                            callback.onError("Không xác định được danh sách.");
                            return;
                        }
                        List<String> previousListIds = new ArrayList<>();
                        for (ListModel list : allLists) {
                            if (list.getPosition() < currentList.getPosition()) {
                                previousListIds.add(list.getUid());
                            }
                        }
                        if (previousListIds.isEmpty()) {
                            callback.onError("Không có danh sách phía trước.");
                            return;
                        }
                        findTagsInLists(previousListIds, callback);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    private void findTagsInLists(List<String> listIds, TagListCallback callback) {
        cardsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot cardSnapshot) {
                Set<String> foundTagIds = new HashSet<>();
                for (DataSnapshot ds : cardSnapshot.getChildren()) {
                    Card card = ds.getValue(Card.class);
                    if (card != null && listIds.contains(card.getListId())) {
                        if (card.getSelfTagId() != null && !card.getSelfTagId().isEmpty()) {
                            foundTagIds.add(card.getSelfTagId());
                        }
                    }
                }
                if (foundTagIds.isEmpty()) {
                    callback.onError("Không có thẻ nào chứa Tag ở phía trước.");
                    return;
                }
                fetchTagsDetails(new ArrayList<>(foundTagIds), callback);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void fetchTagsDetails(List<String> tagIds, TagListCallback callback) {
        tagsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot tagSnapshot) {
                List<Tag> resultTags = new ArrayList<>();
                for (DataSnapshot ds : tagSnapshot.getChildren()) {
                    if (tagIds.contains(ds.getKey())) {
                        Tag tag = ds.getValue(Tag.class);
                        if (tag != null) {
                            tag.setUid(ds.getKey());
                            resultTags.add(tag);
                        }
                    }
                }
                callback.onTagsLoaded(resultTags);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void assignTagToCard(String cardId, Tag tag) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("/assignedTagId", tag.getUid());
        updates.put("/labelColor", tag.getColor());
        cardsRef.child(cardId).updateChildren(updates);
    }

    // =========================================================================
    // PHẦN 4: LOGIC MOVEMENT & RESET TAG (GIAI ĐOẠN 3 - MỚI)
    // =========================================================================

    /**
     * [CORE LOGIC GIAI ĐOẠN 3]
     * Hủy liên kết Assigned Tag và khôi phục màu của Self Tag (nếu có)
     */
    private void resetAssignedTagAndRestoreColor(String cardId) {
        cardsRef.child(cardId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Card card = snapshot.getValue(Card.class);
                if (card == null) return;

                // Chuẩn bị Map update
                Map<String, Object> updates = new HashMap<>();
                updates.put("assignedTagId", null); // 1. Hủy liên kết Tag gán

                // 2. Logic Khôi phục màu
                if (card.getSelfTagId() != null && !card.getSelfTagId().isEmpty()) {
                    // Nếu thẻ có Self Tag -> Lấy màu của Self Tag để hiển thị lại
                    tagsRef.child(card.getSelfTagId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot tagSnap) {
                            Tag selfTag = tagSnap.getValue(Tag.class);
                            String originalColor = (selfTag != null) ? selfTag.getColor() : "";

                            updates.put("labelColor", originalColor);
                            cardsRef.child(cardId).updateChildren(updates); // Thực thi update
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                } else {
                    // Nếu không có Self Tag -> Trả về màu trắng/rỗng
                    updates.put("labelColor", "");
                    cardsRef.child(cardId).updateChildren(updates); // Thực thi update
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * 1. Di chuyển thẻ sang Danh sách khác
     * -> Cập nhật listId MỚI + Gọi hàm reset Tag gán
     */
    public void moveCardToNewList(String cardId, String newListId) {
        // Cập nhật vị trí
        cardsRef.child(cardId).child("listId").setValue(newListId);

        // [QUAN TRỌNG] Hủy tag gán vì đã đổi cột
        resetAssignedTagAndRestoreColor(cardId);
    }

    /**
     * 2. Cập nhật vị trí Danh sách (Kéo thả cột)
     * -> Cập nhật position MỚI + Reset Tag của toàn bộ thẻ trong cột đó
     */
    public void updateListPosition(String listId, double newPosition) {
        // Cập nhật vị trí danh sách
        listsRef.child(listId).child("position").setValue(newPosition);

        // [QUAN TRỌNG] Vì cột di chuyển vị trí, luồng dữ liệu thay đổi
        // -> Quét tất cả thẻ trong cột này để hủy Assigned Tag cho an toàn
        sanitizeTagsInList(listId);
    }

    // Helper: Quét và reset thẻ trong 1 danh sách
    private void sanitizeTagsInList(String listId) {
        cardsRef.orderByChild("listId").equalTo(listId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Reset từng thẻ một
                    resetAssignedTagAndRestoreColor(ds.getKey());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * 3. Di chuyển thẻ Lên/Xuống trong CÙNG 1 danh sách
     * (Giữ nguyên logic cũ, không cần reset tag vì không đổi luồng)
     */
    public void moveCardVertically(String listId, String currentCardId, boolean isUp) {
        Query query = cardsRef.orderByChild("listId").equalTo(listId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Card> cardsInList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Card c = data.getValue(Card.class);
                    if (c != null) {
                        c.setUid(data.getKey());
                        cardsInList.add(c);
                    }
                }
                Collections.sort(cardsInList, (o1, o2) -> Double.compare(o1.getPosition(), o2.getPosition()));

                int currentIndex = -1;
                for (int i = 0; i < cardsInList.size(); i++) {
                    if (cardsInList.get(i).getUid().equals(currentCardId)) {
                        currentIndex = i;
                        break;
                    }
                }
                if (currentIndex == -1) return;

                if (isUp) {
                    if (currentIndex > 0) {
                        swapCardPositions(cardsInList.get(currentIndex), cardsInList.get(currentIndex - 1));
                    }
                } else {
                    if (currentIndex < cardsInList.size() - 1) {
                        swapCardPositions(cardsInList.get(currentIndex), cardsInList.get(currentIndex + 1));
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void swapCardPositions(Card card1, Card card2) {
        double pos1 = card1.getPosition();
        double pos2 = card2.getPosition();
        cardsRef.child(card1.getUid()).child("position").setValue(pos2);
        cardsRef.child(card2.getUid()).child("position").setValue(pos1);
    }
}