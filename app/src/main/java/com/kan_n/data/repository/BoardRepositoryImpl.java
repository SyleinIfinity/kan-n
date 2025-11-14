// Đặt tại: app/src/main/java/com/kan_n/data/repository/BoardRepositoryImpl.java

package com.kan_n.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.interfaces.BoardRepository;
import com.kan_n.data.models.Background;
import com.kan_n.data.models.Board;
import com.kan_n.data.models.Membership;
import com.kan_n.data.models.Workspace;
import com.kan_n.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardRepositoryImpl implements BoardRepository {

    private final DatabaseReference mRootRef;
    private final DatabaseReference mBoardsRef;
    private final DatabaseReference mMembershipsRef;
    private final DatabaseReference mWorkspacesRef; // Cần tham chiếu đến Workspaces
    // Bỏ mCurrentUserId vì chúng ta sẽ nhận userId qua phương thức

    public BoardRepositoryImpl() {
        this.mRootRef = FirebaseUtils.getRootRef();
        this.mBoardsRef = mRootRef.child("boards");
        this.mMembershipsRef = mRootRef.child("memberships");
        this.mWorkspacesRef = mRootRef.child("workspaces"); // Khởi tạo
    }

    // --- CÁC PHƯƠNG THỨC LẤY DỮ LIỆU (GET) ---

    /**
     * Lấy tất cả Workspaces VÀ các Board lồng bên trong mà user có quyền truy cập.
     * Đây là logic phức tạp, cần query 3 nơi: Memberships, Boards, và Workspaces.
     */
    @Override
    public void getWorkspacesWithBoards(String userId, WorkspacesWithBoardsCallback callback) {
        if (userId == null) {
            callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        // 1. Lấy tất cả Memberships của user
        mMembershipsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot membershipSnapshot) {
                if (!membershipSnapshot.exists()) {
                    callback.onSuccess(new ArrayList<>()); // User không có trong bảng nào
                    return;
                }

                // Tạo một Map các boardId mà user có quyền
                Map<String, String> userBoardIds = new HashMap<>(); // <BoardId, Role>
                for (DataSnapshot snap : membershipSnapshot.getChildren()) {
                    Membership membership = snap.getValue(Membership.class);
                    if (membership != null) {
                        userBoardIds.put(membership.getBoardId(), membership.getRole());
                    }
                }

                // 2. Lấy TẤT CẢ các Bảng (boards)
                mBoardsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot boardsSnapshot) {
                        if (!boardsSnapshot.exists()) {
                            callback.onSuccess(new ArrayList<>()); // Không có bảng nào tồn tại
                            return;
                        }

                        // Lọc các bảng mà user có quyền và nhóm chúng theo workspaceId
                        Map<String, List<Board>> boardsByWorkspace = new HashMap<>();
                        for (DataSnapshot snap : boardsSnapshot.getChildren()) {
                            String boardId = snap.getKey();
                            if (userBoardIds.containsKey(boardId)) {
                                Board board = snap.getValue(Board.class);
                                if (board != null) {
                                    board.setUid(boardId); // Gán UID cho model
                                    String wsId = board.getWorkspaceId();

                                    if (!boardsByWorkspace.containsKey(wsId)) {
                                        boardsByWorkspace.put(wsId, new ArrayList<>());
                                    }
                                    boardsByWorkspace.get(wsId).add(board);
                                }
                            }
                        }

                        // 3. Lấy TẤT CẢ các Workspace
                        mWorkspacesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot workspacesSnapshot) {
                                if (!workspacesSnapshot.exists()) {
                                    callback.onSuccess(new ArrayList<>()); // Không có workspace
                                    return;
                                }

                                // Gắn các list board đã lọc vào workspace tương ứng
                                List<Workspace> finalWorkspaces = new ArrayList<>();
                                for (DataSnapshot snap : workspacesSnapshot.getChildren()) {
                                    String wsId = snap.getKey();
                                    if (boardsByWorkspace.containsKey(wsId)) {
                                        Workspace workspace = snap.getValue(Workspace.class);
                                        if (workspace != null) {
                                            workspace.setUid(wsId);
                                            // Gắn danh sách board vào model
                                            workspace.setBoards(boardsByWorkspace.get(wsId));
                                            finalWorkspaces.add(workspace);
                                        }
                                    }
                                }
                                callback.onSuccess(finalWorkspaces);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onError(error.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }


    @Override
    public void getBoardDetails(String boardId, BoardCallback callback) {
        mBoardsRef.child(boardId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Board board = snapshot.getValue(Board.class);
                    if (board != null) {
                        board.setUid(snapshot.getKey());
                        callback.onSuccess(board);
                    } else {
                        callback.onError("Không thể đọc dữ liệu bảng.");
                    }
                } else {
                    callback.onError("Không tìm thấy bảng.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // --- CÁC PHƯƠNG THỨC GHI DỮ LIỆU (WRITE) ---

    @Override
    public void createBoard(String workspaceId, String name, String visibility, Background background, GeneralCallback callback) {

        String currentUserId = FirebaseUtils.getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        String boardId = mBoardsRef.push().getKey();
        if (boardId == null) {
            callback.onError("Không thể tạo ID cho bảng mới.");
            return;
        }

        // [CẬP NHẬT] Sử dụng constructor mới với Background object
        // Lưu ý: Trường description trong Board hiện tại chỉ được dùng để lưu mô tả
        Board newBoard = new Board(workspaceId, name, "", visibility, currentUserId, background);

        String membershipId = mMembershipsRef.push().getKey();
        Membership ownerMembership = new Membership(boardId, currentUserId, "owner");

        if (membershipId == null) {
            callback.onError("Không thể tạo ID cho membership.");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("/boards/" + boardId, newBoard.toMap());
        updates.put("/memberships/" + membershipId, ownerMembership.toMap());

        mRootRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    @Override
    public void updateBoard(String boardId, Map<String, Object> updates, GeneralCallback callback) {
        mBoardsRef.child(boardId).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    /**
     * Xóa Bảng VÀ các Membership liên quan.
     * CẢNH BÁO: CHƯA XÓA Lists, Cards, Tags... của bảng này.
     */
    @Override
    public void deleteBoard(String boardId, GeneralCallback callback) {
        mMembershipsRef.orderByChild("boardId").equalTo(boardId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> updates = new HashMap<>();

                // Đánh dấu xóa bảng
                updates.put("/boards/" + boardId, null);

                // Đánh dấu xóa các membership
                if (snapshot.exists()) {
                    for (DataSnapshot memSnap : snapshot.getChildren()) {
                        updates.put("/memberships/" + memSnap.getKey(), null);
                    }
                }

                // Thực hiện xóa đồng thời
                mRootRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void addMemberToBoard(String boardId, String userId, GeneralCallback callback) {
        // (Nên kiểm tra xem user có tồn tại không trước khi thêm)

        String membershipId = mMembershipsRef.push().getKey();
        if (membershipId == null) {
            callback.onError("Không thể tạo ID membership.");
            return;
        }

        Membership newMember = new Membership(boardId, userId, "member");
        mMembershipsRef.child(membershipId).setValue(newMember.toMap()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    @Override
    public void removeMemberFromBoard(String boardId, String userId, GeneralCallback callback) {
        mMembershipsRef.orderByChild("boardId").equalTo(boardId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onError("Không tìm thấy thành viên nào trong bảng này.");
                    return;
                }

                String membershipIdToDelete = null;
                for (DataSnapshot memSnap : snapshot.getChildren()) {
                    Membership membership = memSnap.getValue(Membership.class);
                    if (membership != null && membership.getUserId().equals(userId)) {
                        membershipIdToDelete = memSnap.getKey();
                        break;
                    }
                }

                if (membershipIdToDelete != null) {
                    mMembershipsRef.child(membershipIdToDelete).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    });
                } else {
                    callback.onError("Không tìm thấy thành viên này trong bảng.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}