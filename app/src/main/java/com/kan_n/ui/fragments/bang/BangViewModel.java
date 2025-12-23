package com.kan_n.ui.fragments.bang;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.interfaces.BoardRepository;
import com.kan_n.data.models.Membership; // Đảm bảo import model Membership
import com.kan_n.data.models.Workspace;
import com.kan_n.data.repository.BoardRepositoryImpl;
import com.kan_n.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class BangViewModel extends ViewModel {

    private final BoardRepository boardRepository;
    private final MutableLiveData<List<Workspace>> workspacesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    // ✨ [THÊM MỚI] LiveData này sẽ báo cho Fragment biết ID thật của Workspace vừa tìm thấy
    private final MutableLiveData<String> foundActiveWorkspaceId = new MutableLiveData<>();

    private DatabaseReference membershipsRef;
    private DatabaseReference workspacesRef; // ✨ [THÊM MỚI] Tham chiếu để tìm workspace
    private DatabaseReference boardsRef;     // ✨ [THÊM MỚI] Tham chiếu để tra cứu từ board ra workspace
    private ValueEventListener membershipListener;
    private String currentUserId;

    private String activeWsId;

    public void setActiveWsId(String activeWsId) {
        this.activeWsId = activeWsId;
    }

    public BangViewModel() {
        this.boardRepository = new BoardRepositoryImpl();
        this.currentUserId = FirebaseUtils.getCurrentUserId();
        if (this.currentUserId != null) {
            this.membershipsRef = FirebaseUtils.getRootRef().child("memberships");
            this.workspacesRef = FirebaseUtils.getRootRef().child("workspaces"); // ✨ Khởi tạo
            this.boardsRef = FirebaseUtils.getRootRef().child("boards");         // ✨ Khởi tạo
        }
    }

    public LiveData<List<Workspace>> getWorkspaces() {
        return workspacesLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    // ✨ [THÊM MỚI] Getter cho LiveData tìm ID
    public LiveData<String> getFoundActiveWorkspaceId() {
        return foundActiveWorkspaceId;
    }

    public void startListeningForChanges() {
        if (currentUserId == null) return;
        if (membershipListener == null) {
            membershipListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // ✨ [SỬA ĐỔI] Gọi hàm thông minh thay vì loadWorkspaces() trực tiếp
                    loadDataSmart();
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            };
            membershipsRef.orderByChild("userId").equalTo(currentUserId)
                    .addValueEventListener(membershipListener);
        }
    }

    /**
     * ✨ [THÊM MỚI] Hàm logic trung tâm:
     * Kiểm tra nếu ID hiện tại bị lỗi (rỗng hoặc mặc định), thì đi tìm ID đúng.
     */
    public void loadDataSmart() {
        if (currentUserId == null) return;

        // Nếu activeWsId là null hoặc là giá trị rác mặc định "ws_1_id"
        if (activeWsId == null || "ws_1_id".equals(activeWsId) || activeWsId.isEmpty()) {
            findWorkspaceByOwner(); // 🚀 Bắt đầu BƯỚC 1
        } else {
            loadWorkspaces(); // ID có vẻ ổn, load bình thường
        }
    }

    /**
     * ✨ [THÊM MỚI] BƯỚC 1: Tìm Workspace do user SỞ HỮU (createdBy)
     * (Dành cho New User hoặc Old User có tạo workspace riêng)
     */
    private void findWorkspaceByOwner() {
        if (workspacesRef == null) return;

        workspacesRef.orderByChild("createdBy").equalTo(currentUserId).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            // ✅ Tìm thấy! Lấy cái đầu tiên
                            for (DataSnapshot wsSnap : snapshot.getChildren()) {
                                updateActiveWorkspace(wsSnap.getKey());
                                return;
                            }
                        } else {
                            // ❌ Không tìm thấy -> Chuyển sang BƯỚC 2
                            findWorkspaceByMembership();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Lỗi query -> Thử bước 2 luôn cho chắc
                        findWorkspaceByMembership();
                    }
                });
    }

    /**
     * ✨ [THÊM MỚI] BƯỚC 2: Tìm Workspace mà user THAM GIA (Membership)
     * (Dành cho Old User chỉ được invite vào bảng của người khác)
     */
    private void findWorkspaceByMembership() {
        if (membershipsRef == null) return;

        // Lấy 1 membership bất kỳ của user này
        membershipsRef.orderByChild("userId").equalTo(currentUserId).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot memSnap : snapshot.getChildren()) {
                                Membership mem = memSnap.getValue(Membership.class);
                                if (mem != null && mem.getBoardId() != null) {
                                    // Từ BoardID -> Tìm ra WorkspaceID
                                    findWorkspaceFromBoard(mem.getBoardId());
                                    return;
                                }
                            }
                        }
                        // Vẫn không có -> User này hoàn toàn trắng tinh
                        workspacesLiveData.postValue(new ArrayList<>());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        errorLiveData.postValue(error.getMessage());
                    }
                });
    }

    /**
     * ✨ [THÊM MỚI] Helper: Từ BoardID tra ngược ra WorkspaceID
     */
    private void findWorkspaceFromBoard(String boardId) {
        if (boardsRef == null) return;
        boardsRef.child(boardId).child("workspaceId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String wsId = snapshot.getValue(String.class);
                    if (wsId != null) {
                        updateActiveWorkspace(wsId);
                    }
                } else {
                    // Board lỗi không có workspaceId
                    workspacesLiveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * ✨ [THÊM MỚI] Cập nhật ID tìm được và load lại dữ liệu
     */
    private void updateActiveWorkspace(String id) {
        activeWsId = id;
        foundActiveWorkspaceId.postValue(id); // 📢 Bắn tín hiệu cho Fragment lưu lại
        loadWorkspaces(); // Load dữ liệu thật
    }

    public void loadWorkspaces() {
        if (currentUserId == null || activeWsId == null) {
            workspacesLiveData.setValue(new ArrayList<>());
            return;
        }

        boardRepository.getActiveWorkspaceWithBoards(currentUserId, activeWsId, new BoardRepository.WorkspacesWithBoardsCallback() {
            @Override
            public void onSuccess(List<Workspace> workspaces) {
                workspacesLiveData.postValue(workspaces);
            }
            @Override
            public void onError(String message) {
                workspacesLiveData.postValue(new ArrayList<>());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (membershipListener != null && currentUserId != null) {
            membershipsRef.orderByChild("userId").equalTo(currentUserId)
                    .removeEventListener(membershipListener);
        }
    }
}