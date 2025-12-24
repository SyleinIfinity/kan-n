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
import com.kan_n.data.models.Membership;
import com.kan_n.data.models.User; // ✨ Import User
import com.kan_n.data.models.Workspace;
import com.kan_n.data.repository.BoardRepositoryImpl;
import com.kan_n.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class BangViewModel extends ViewModel {

    private final BoardRepository boardRepository;
    private final MutableLiveData<List<Workspace>> workspacesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private final MutableLiveData<String> foundActiveWorkspaceId = new MutableLiveData<>();

    private DatabaseReference membershipsRef;
    private DatabaseReference workspacesRef;
    private DatabaseReference boardsRef;
    private DatabaseReference usersRef; // ✨ [MỚI] Tham chiếu User
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
            this.workspacesRef = FirebaseUtils.getRootRef().child("workspaces");
            this.boardsRef = FirebaseUtils.getRootRef().child("boards");
            this.usersRef = FirebaseUtils.getRootRef().child("users"); // ✨ [MỚI]
        }
    }

    public LiveData<List<Workspace>> getWorkspaces() {
        return workspacesLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<String> getFoundActiveWorkspaceId() {
        return foundActiveWorkspaceId;
    }

    public void startListeningForChanges() {
        if (currentUserId == null) return;
        if (membershipListener == null) {
            membershipListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    loadDataSmart();
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            };
            membershipsRef.orderByChild("userId").equalTo(currentUserId)
                    .addValueEventListener(membershipListener);
        }
    }

    /**
     * ✨ LOGIC THÔNG MINH (CẬP NHẬT):
     * 1. Nếu ID local rỗng/rác -> Check trên Cloud.
     * 2. Nếu Cloud có ID -> Kiểm tra tồn tại -> Load.
     * 3. Nếu Cloud không có hoặc Workspace đã xóa -> Tìm thủ công (Owner -> Member).
     */
    public void loadDataSmart() {
        if (currentUserId == null) return;

        // Nếu activeWsId chưa có hoặc là mặc định
        if (activeWsId == null || "ws_1_id".equals(activeWsId) || activeWsId.isEmpty()) {
            fetchLastActiveWorkspaceFromCloud(); // 🚀 Ưu tiên lấy từ Cloud
        } else {
            // ID có vẻ ổn, nhưng cần validate xem nó còn tồn tại không (phòng trường hợp đã bị xóa ở máy khác)
            validateAndLoadWorkspace(activeWsId);
        }
    }

    // ✨ [MỚI] Lấy ID đã lưu trên Cloud về
    private void fetchLastActiveWorkspaceFromCloud() {
        if (usersRef == null) return;
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.getLastActiveWorkspace() != null && !user.getLastActiveWorkspace().isEmpty()) {
                    // Có ID trên Cloud -> Kiểm tra xem nó còn sống không
                    validateAndLoadWorkspace(user.getLastActiveWorkspace());
                } else {
                    // Không có trên Cloud -> Tìm thủ công
                    findWorkspaceByOwner();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { findWorkspaceByOwner(); }
        });
    }

    // ✨ [MỚI] Kiểm tra Workspace có tồn tại không trước khi load
    private void validateAndLoadWorkspace(String targetWsId) {
        if (workspacesRef == null) return;
        workspacesRef.child(targetWsId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Workspace tồn tại -> Load nó & Update lại biến local
                    updateActiveWorkspace(targetWsId);
                } else {
                    // ❌ Workspace này đã bị XÓA -> Tìm cái khác thay thế
                    findWorkspaceByOwner();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                findWorkspaceByOwner();
            }
        });
    }

    // ✨ [MỚI] Hàm lưu ID đang chọn lên Cloud
    public void saveCurrentWorkspaceToCloud(String wsId) {
        if (currentUserId != null && wsId != null && usersRef != null) {
            usersRef.child(currentUserId).child("lastActiveWorkspace").setValue(wsId);
        }
    }

    /**
     * BƯỚC 1: Tìm Workspace do user SỞ HỮU
     */
    private void findWorkspaceByOwner() {
        if (workspacesRef == null) return;

        workspacesRef.orderByChild("createdBy").equalTo(currentUserId).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            for (DataSnapshot wsSnap : snapshot.getChildren()) {
                                updateActiveWorkspace(wsSnap.getKey());
                                return;
                            }
                        } else {
                            findWorkspaceByMembership();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        findWorkspaceByMembership();
                    }
                });
    }

    /**
     * BƯỚC 2: Tìm Workspace mà user THAM GIA
     */
    private void findWorkspaceByMembership() {
        if (membershipsRef == null) return;

        membershipsRef.orderByChild("userId").equalTo(currentUserId).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot memSnap : snapshot.getChildren()) {
                                Membership mem = memSnap.getValue(Membership.class);
                                if (mem != null && mem.getBoardId() != null) {
                                    findWorkspaceFromBoard(mem.getBoardId());
                                    return;
                                }
                            }
                        }
                        workspacesLiveData.postValue(new ArrayList<>());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        errorLiveData.postValue(error.getMessage());
                    }
                });
    }

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
                    workspacesLiveData.postValue(new ArrayList<>());
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateActiveWorkspace(String id) {
        this.activeWsId = id;
        foundActiveWorkspaceId.postValue(id); // Báo UI
        saveCurrentWorkspaceToCloud(id); // ✨ [MỚI] Lưu lên Cloud để đồng bộ
        loadWorkspaces();
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