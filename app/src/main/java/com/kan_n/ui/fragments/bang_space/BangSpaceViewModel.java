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
import com.kan_n.data.models.Membership;
import com.kan_n.data.models.Tag;
import com.kan_n.data.repository.BoardRepositoryImpl;
import com.kan_n.data.repository.CardRepositoryImpl;
import com.kan_n.data.repository.ListRepositoryImpl;
import com.kan_n.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BangSpaceViewModel extends ViewModel {

    private final ListRepository listRepository;
    private final CardRepository cardRepository;
    // private final BoardRepository boardRepository;

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
    private final androidx.lifecycle.MutableLiveData<String> userPermission = new androidx.lifecycle.MutableLiveData<>("view"); // Mặc định là view

    private DatabaseReference membershipsRef;
    private ValueEventListener permissionListener;
    private Query permissionQuery;
    public androidx.lifecycle.LiveData<String> getUserPermission() {
        return userPermission;
    }



    public BangSpaceViewModel() {
        this.listRepository = new ListRepositoryImpl();
        this.cardRepository = new CardRepositoryImpl();
        this.boardRepository = new BoardRepositoryImpl();
        this.membershipsRef = FirebaseUtils.getDatabaseInstance().getReference("memberships");
    }

    public void getBoardDetails(String boardId, BoardRepository.BoardCallback callback) {
        boardRepository.getBoardDetails(boardId, callback);
    }

    /**
     * Bắt đầu lắng nghe các danh sách (cột)
     */
    public void listenForLists(String boardId, ChildEventListener listener) {
        this.listsListener = listener;
        this.listsQuery = listsRef.orderByChild("boardId").equalTo(boardId);
        this.listsQuery.addChildEventListener(listener);
    }

    public void fetchUserPermission(String boardId) {
        String userId = FirebaseUtils.getCurrentUserId();
        if (userId == null) return;

        // Gỡ listener cũ nếu có (đề phòng trường hợp chuyển bảng)
        if (permissionListener != null && permissionQuery != null) {
            permissionQuery.removeEventListener(permissionListener);
        }

        // Thiết lập Query
        permissionQuery = membershipsRef.orderByChild("boardId").equalTo(boardId);

        permissionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Membership m = ds.getValue(Membership.class);
                    if (m != null && userId.equals(m.getUserId())) {
                        // Cập nhật LiveData ngay khi dữ liệu trên Firebase thay đổi
                        if ("owner".equals(m.getRole()) || "edit".equals(m.getPermission())) {
                            userPermission.setValue("edit");
                        } else {
                            userPermission.setValue("view");
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    userPermission.setValue("view");
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };

        permissionQuery.addValueEventListener(permissionListener);
    }


    /**
     * Bắt đầu lắng nghe các thẻ (card) cho MỘT danh sách
     */
    public void listenForCards(String listId, ChildEventListener listener) {
        // Gỡ listener cũ của listId
        removeCardListener(listId);

        Query cardQuery = cardsRef.orderByChild("listId").equalTo(listId);
        cardQueries.put(listId, cardQuery);
        cardListeners.put(listId, listener);
        cardQuery.addChildEventListener(listener);
    }

    /**
     * Gỡ listener của thẻ khi ListModelViewHolder bị recycle
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

    @Override
    protected void onCleared() {
        super.onCleared();
        if (permissionQuery != null && permissionListener != null) {
            permissionQuery.removeEventListener(permissionListener);
        }
        if (listsQuery != null && listsListener != null) {
            listsQuery.removeEventListener(listsListener);
        }
        for (Map.Entry<String, Query> entry : cardQueries.entrySet()) {
            ChildEventListener listener = cardListeners.get(entry.getKey());
            if (listener != null) entry.getValue().removeEventListener(listener);
        }
    }

    // =========================================================================
    // PHẦN 2: CRUD CƠ BẢN & XỬ LÝ XÓA NÂNG CAO (CASCADING DELETE)
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

    public void updateListTitle(String listId, String newTitle) {
        listsRef.child(listId).child("title").setValue(newTitle);
    }

    /**
     * [MỚI] Helper: Xóa Tag khỏi hệ thống và gỡ liên kết khỏi tất cả các thẻ đang dùng nó
     */
    private void deleteTagAndUnlinkOthers(String tagId) {
        if (tagId == null) return;

        // 1. Xóa node Tag trong bảng 'tags'
        tagsRef.child(tagId).removeValue();

        // 2. Tìm tất cả các thẻ đang được gán (assigned) tag này
        cardsRef.orderByChild("assignedTagId").equalTo(tagId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String affectedCardId = ds.getKey();
                            // Reset thẻ bị ảnh hưởng (Hủy assigned, khôi phục màu gốc)
                            resetAssignedTagAndRestoreColor(affectedCardId);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    /**
     * [CẬP NHẬT] Xóa thẻ -> Xóa luôn Self Tag của nó -> Unlink các thẻ khác
     */
    public void deleteCard(String cardId) {
        cardsRef.child(cardId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Card card = snapshot.getValue(Card.class);
                if (card != null) {
                    // Nếu thẻ này tạo ra Tag -> Xóa sạch Tag đó
                    if (card.getSelfTagId() != null) {
                        deleteTagAndUnlinkOthers(card.getSelfTagId());
                    }
                    // Sau đó mới xóa thẻ
                    cardsRef.child(cardId).removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * [CẬP NHẬT] Xóa danh sách -> Xóa từng thẻ con (để kích hoạt logic xóa Tag)
     */
    public void deleteList(String listId) {
        cardsRef.orderByChild("listId").equalTo(listId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Gọi hàm deleteCard ở trên để đảm bảo sạch rác
                    deleteCard(ds.getKey());
                }
                // Sau khi dọn dẹp thẻ, xóa danh sách
                listsRef.child(listId).removeValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // =========================================================================
    // PHẦN 3: LOGIC ASSIGNED-TAG (GIAI ĐOẠN 2 - GIỮ NGUYÊN)
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
                        // 1. Lấy tất cả danh sách về
                        for (DataSnapshot ds : listSnapshot.getChildren()) {
                            ListModel list = ds.getValue(ListModel.class);
                            if (list != null) {
                                list.setUid(ds.getKey());
                                allLists.add(list);
                            }
                        }

                        if (allLists.isEmpty()) {
                            callback.onError("Bảng này chưa có danh sách nào.");
                            return;
                        }

                        // 2. Sắp xếp danh sách theo vị trí (position) tăng dần
                        // Đảm bảo phần tử số 0 luôn là DS đầu tiên (DS1)
                        Collections.sort(allLists, (o1, o2) -> Double.compare(o1.getPosition(), o2.getPosition()));

                        // --- [MỚI] LOGIC CHỈ LẤY TỪ DS1 ---
                        ListModel firstList = allLists.get(0);

                        // Kiểm tra: Nếu thẻ hiện tại đang nằm ở chính DS1 thì không cho tự mượn Tag của người khác trong cùng list (hoặc tùy logic bạn, nhưng thường là không cần thiết)
                        // Tuy nhiên, yêu cầu của bạn là: "Khi chọn gắn tag thì chỉ có thể chọn các tag của ds1".

                        List<String> targetListId = new ArrayList<>();
                        targetListId.add(firstList.getUid());

                        // Gọi hàm tìm thẻ trong DS1
                        findTagsInLists(targetListId, callback);
                        // ----------------------------------
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
        // [THAY ĐỔI] Lưu vào assignedTagColor thay vì labelColor
        updates.put("/assignedTagColor", tag.getColor());

        cardsRef.child(cardId).updateChildren(updates);
    }

    // =========================================================================
    // PHẦN 4: LOGIC MOVEMENT & RESET TAG (GIAI ĐOẠN 3 - HOÀN THIỆN)
    // =========================================================================

    /**
     * [CORE LOGIC]
     * Hủy liên kết Assigned Tag và khôi phục màu của Self Tag (nếu có)
     */
    private void resetAssignedTagAndRestoreColor(String cardId) {
        // [THAY ĐỔI] Logic đơn giản hơn nhiều nhờ cấu trúc dữ liệu mới
        Map<String, Object> updates = new HashMap<>();
        updates.put("assignedTagId", null);
        updates.put("assignedTagColor", null); // Xóa màu được gán

        cardsRef.child(cardId).updateChildren(updates);
    }

    /**
     * [MỚI] Kiểm tra thẻ nguồn (Source Check).
     * Nếu thẻ bị di chuyển là thẻ Nguồn (có tạo Tag), thì tất cả các thẻ đang nhận Tag đó
     * phải bị hủy liên kết ngay lập tức (để đảm bảo tính đúng đắn luồng trái-phải).
     */
    private void checkAndResetSubscribersIfSource(String cardId) {
        cardsRef.child(cardId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Card card = snapshot.getValue(Card.class);
                // Nếu thẻ này có tạo ra Tag (SelfTagId không null)
                if (card != null && card.getSelfTagId() != null && !card.getSelfTagId().isEmpty()) {
                    String sourceTagId = card.getSelfTagId();

                    // Tìm trong TOÀN BỘ database các thẻ đang có assignedTagId == sourceTagId
                    // Lưu ý: Cần Index trên Firebase Rules để query này nhanh
                    cardsRef.orderByChild("assignedTagId").equalTo(sourceTagId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot subSnap) {
                                    if (subSnap.exists()) {
                                        for (DataSnapshot ds : subSnap.getChildren()) {
                                            String subscriberCardId = ds.getKey();
                                            // Reset thẻ đó về trạng thái gốc
                                            resetAssignedTagAndRestoreColor(subscriberCardId);
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * 1. Di chuyển thẻ sang Danh sách khác
     */
    public void moveCardToNewList(String cardId, String newListId) {
        // BƯỚC 1: Lấy dữ liệu thẻ hiện tại để kiểm tra xem nó có phải là "Thẻ Nguồn" không
        cardsRef.child(cardId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Card card = snapshot.getValue(Card.class);
                if (card == null) return;

                // Kiểm tra: Nếu thẻ này đang là Nguồn (có selfTagId)
                if (card.getSelfTagId() != null && !card.getSelfTagId().isEmpty()) {
                    // => Thực hiện Reset diện rộng (Cascading Reset)
                    // Reset các thẻ khác VÀ xóa quyền làm nguồn của thẻ này
                    resetSubscribersAndSelf(cardId, card.getSelfTagId(), newListId);
                } else {
                    // Nếu thẻ bình thường (hoặc thẻ đang đi mượn tag)
                    // Chỉ cần reset trạng thái đi mượn của chính nó (cho sạch sẽ) rồi di chuyển
                    resetAssignedTagAndRestoreColor(cardId);
                    performMove(cardId, newListId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void performMove(String cardId, String newListId) {
        cardsRef.child(cardId).child("listId").setValue(newListId);
    }

    private void resetSubscribersAndSelf(String sourceCardId, String tagId, String targetListId) {
        // 1. Tìm tất cả thẻ đang mượn Tag này (assignedTagId == tagId)
        cardsRef.orderByChild("assignedTagId").equalTo(tagId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Object> updates = new HashMap<>();

                        // A. Reset các thẻ con (Subscribers)
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String subCardId = ds.getKey();
                            updates.put("/" + subCardId + "/assignedTagId", null);
                            updates.put("/" + subCardId + "/assignedTagColor", null);
                        }

                        // B. Tước quyền thẻ nguồn (Source Card) vì đã rời khỏi DS1
                        // Xóa selfTagId, selfTagColor và cả labelColor (để về trắng)
                        updates.put("/" + sourceCardId + "/selfTagId", null);
                        updates.put("/" + sourceCardId + "/selfTagColor", null);
                        updates.put("/" + sourceCardId + "/labelColor", null);

                        // C. Cập nhật vị trí mới luôn trong lần update này
                        updates.put("/" + sourceCardId + "/listId", targetListId);

                        // Thực thi update hàng loạt (Atomic Update)
                        cardsRef.updateChildren(updates, (error, ref) -> {
                            if (error == null) {
                                // Nếu muốn xóa luôn cái Tag trong bảng 'tags' cho sạch DB thì gọi thêm dòng này:
                                // tagsRef.child(tagId).removeValue();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    /**
     * 2. Cập nhật vị trí Danh sách (Kéo thả cột)
     */
    public void updateListPosition(String listId, double newPosition) {
        listsRef.child(listId).child("position").setValue(newPosition);

        // Vì cột di chuyển vị trí, luồng dữ liệu thay đổi -> Quét tất cả thẻ trong cột này
        cardsRef.orderByChild("listId").equalTo(listId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String cardId = ds.getKey();
                    // 1. Reset tag gán của thẻ
                    resetAssignedTagAndRestoreColor(cardId);
                    // 2. Kiểm tra nếu thẻ là nguồn -> Reset các subscriber
                    checkAndResetSubscribersIfSource(cardId);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * 3. Di chuyển thẻ Lên/Xuống (Swap)
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