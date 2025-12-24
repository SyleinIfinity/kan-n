// Đặt tại: app/src/main/java/com/kan_n/data/repository/BoardRepositoryImpl.java

package com.kan_n.data.repository;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.interfaces.BoardRepository;
import com.kan_n.data.models.Background;
import com.kan_n.data.models.Board;
import com.kan_n.data.models.Membership;
import com.kan_n.data.models.User;
import com.kan_n.data.models.Workspace;
import com.kan_n.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BoardRepositoryImpl implements BoardRepository {

    private final DatabaseReference mRootRef;
    private final DatabaseReference mBoardsRef;
    private final DatabaseReference mMembershipsRef;
    private final DatabaseReference mWorkspacesRef; // Tham chiếu đến Workspaces


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

        // 1. Lấy tất cả Memberships của user để biết user tham gia những bảng nào
        mMembershipsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot membershipSnapshot) {
                Map<String, String> userBoardIds = new HashMap<>();
                for (DataSnapshot snap : membershipSnapshot.getChildren()) {
                    Membership membership = snap.getValue(Membership.class);
                    if (membership != null) userBoardIds.put(membership.getBoardId(), membership.getRole());
                }

                // 2. Lấy TẤT CẢ các Bảng để phân loại theo Workspace
                mBoardsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot boardsSnapshot) {
                        Map<String, List<Board>> boardsByWorkspace = new HashMap<>();
                        for (DataSnapshot snap : boardsSnapshot.getChildren()) {
                            String boardId = snap.getKey();
                            if (userBoardIds.containsKey(boardId)) {
                                Board board = snap.getValue(Board.class);
                                if (board != null) {
                                    board.setUid(boardId);
                                    String wsId = board.getWorkspaceId();
                                    if (!boardsByWorkspace.containsKey(wsId)) {
                                        boardsByWorkspace.put(wsId, new ArrayList<>());
                                    }
                                    boardsByWorkspace.get(wsId).add(board);
                                }
                            }
                        }

                        // 3. Lấy tất cả Workspace và lọc:
                        // THÊM: Nếu user là người tạo (createdBy) HOẶC Workspace có bảng user tham gia
                        mWorkspacesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot workspacesSnapshot) {
                                List<Workspace> finalWorkspaces = new ArrayList<>();
                                for (DataSnapshot snap : workspacesSnapshot.getChildren()) {
                                    Workspace workspace = snap.getValue(Workspace.class);
                                    if (workspace != null) {
                                        String wsId = snap.getKey();
                                        // Kiểm tra điều kiện hiển thị
                                        boolean isOwner = userId.equals(workspace.getCreatedBy());
                                        boolean hasMemberBoards = boardsByWorkspace.containsKey(wsId);

                                        if (isOwner || hasMemberBoards) {
                                            workspace.setUid(wsId);
                                            List<Board> boards = boardsByWorkspace.get(wsId);
                                            if (boards == null) {
                                                boards = new ArrayList<>();
                                            }
                                            workspace.setBoards(boards);
                                            finalWorkspaces.add(workspace);
                                        }
                                    }
                                }
                                callback.onSuccess(finalWorkspaces);
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onError(error.getMessage()); }
                        });
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) { callback.onError(error.getMessage()); }
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onError(error.getMessage()); }
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

        Board newBoard = new Board(workspaceId, name, "", visibility, currentUserId, background);

        String membershipId = mMembershipsRef.push().getKey();
        Membership ownerMembership = new Membership(boardId, currentUserId, "owner", "edit");

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

    @Override
    public void addMemberToBoard(String boardId, String userId, GeneralCallback callback) {

        String membershipId = mMembershipsRef.push().getKey();
        if (membershipId == null) {
            callback.onError("Không thể tạo ID membership.");
            return;
        }
        // Mặc định là member và quyền view
        Membership newMember = new Membership(boardId, userId, "member", "view");
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
        // Tìm đúng node membership của user trong bảng này
        mMembershipsRef.orderByChild("boardId").equalTo(boardId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Membership m = ds.getValue(Membership.class);
                            if (m != null && m.getUserId().equals(userId)) {
                                ds.getRef().removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) callback.onSuccess();
                                    else callback.onError(task.getException().getMessage());
                                });
                                return;
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }
    @Override
    public void getBoardMembers(String boardId, BoardMembersCallback callback) {
        mMembershipsRef.orderByChild("boardId").equalTo(boardId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onSuccess(new ArrayList<>());
                            return;
                        }

                        List<Pair<User, String>> members = new ArrayList<>();
                        AtomicInteger counter = new AtomicInteger(0);
                        long totalMembers = snapshot.getChildrenCount();

                        for (DataSnapshot memSnap : snapshot.getChildren()) {
                            Membership mem = memSnap.getValue(Membership.class);
                            if (mem != null && mem.getUserId() != null) {
                                String role = mem.getRole(); // Lấy Role

                                mRootRef.child("users").child(mem.getUserId())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot userSnap) {
                                                User user = userSnap.getValue(User.class);
                                                if (user != null) {
                                                    user.setUid(userSnap.getKey());
                                                    // Thêm cả User và Role vào list
                                                    members.add(new Pair<>(user, role));
                                                }
                                                if (counter.incrementAndGet() == totalMembers) {
                                                    callback.onSuccess(members);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                if (counter.incrementAndGet() == totalMembers) {
                                                    callback.onSuccess(members);
                                                }
                                            }
                                        });
                            } else {
                                if (counter.incrementAndGet() == totalMembers) {
                                    callback.onSuccess(members);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    @Override
    public void createWorkspace(String name, String description, GeneralCallback callback) {
        String userId = FirebaseUtils.getCurrentUserId();
        if (userId == null) {
            callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        String wsId = mWorkspacesRef.push().getKey();
        if (wsId == null) return;

        Workspace workspace = new Workspace(name, description, userId);

        mWorkspacesRef.child(wsId).setValue(workspace.toMap()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    @Override
    public void updateWorkspace(String workspaceId, String newName, GeneralCallback callback) {
        mWorkspacesRef.child(workspaceId).child("name").setValue(newName)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) callback.onSuccess();
                    else callback.onError(task.getException().getMessage());
                });
    }

    @Override
    public void deleteWorkspace(String workspaceId, GeneralCallback callback) {
        // Lưu ý: Logic thực tế nên xóa cả các Board bên trong, ở đây ta xóa node Workspace trước
        mWorkspacesRef.child(workspaceId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) callback.onSuccess();
            else callback.onError(task.getException().getMessage());
        });
    }

    @Override
    public void getActiveWorkspaceWithBoards(String userId, String workspaceId, WorkspacesWithBoardsCallback callback) {
        // 1. Lấy thông tin chi tiết của Workspace cụ thể từ node /workspaces
        mWorkspacesRef.child(workspaceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Workspace workspace = snapshot.getValue(Workspace.class);
                if (workspace == null) {
                    callback.onError("Không tìm thấy Không gian làm việc.");
                    return;
                }
                workspace.setUid(snapshot.getKey());

                // 2. Lấy danh sách Board mà user này có quyền tham gia
                mMembershipsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot membershipSnapshot) {
                        List<String> userBoardIds = new ArrayList<>();
                        for (DataSnapshot ds : membershipSnapshot.getChildren()) {
                            Membership m = ds.getValue(Membership.class);
                            if (m != null) userBoardIds.add(m.getBoardId());
                        }

                        // 3. Lọc các Board thuộc Workspace này
                        mBoardsRef.orderByChild("workspaceId").equalTo(workspaceId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot boardsSnapshot) {
                                List<Board> boards = new ArrayList<>();
                                for (DataSnapshot bs : boardsSnapshot.getChildren()) {
                                    if (userBoardIds.contains(bs.getKey())) {
                                        Board b = bs.getValue(Board.class);
                                        if (b != null) {
                                            b.setUid(bs.getKey());
                                            boards.add(b);
                                        }
                                    }
                                }
                                workspace.setBoards(boards);
                                List<Workspace> result = new ArrayList<>();
                                result.add(workspace); // Trả về danh sách chỉ có 1 phần tử
                                callback.onSuccess(result);
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onError(error.getMessage()); }
                        });
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) { callback.onError(error.getMessage()); }
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onError(error.getMessage()); }
        });
    }

    @Override
    public void updateBoard(String boardId, String newName, GeneralCallback callback) {
        // Chỉ cập nhật trường "title" của Board
        mBoardsRef.child(boardId).child("name").setValue(newName)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) callback.onSuccess();
                    else callback.onError(task.getException().getMessage());
                });
    }

    @Override
    public void deleteBoard(String boardId, GeneralCallback callback) {
        // 1. Xóa node Board
        mBoardsRef.child(boardId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 2. Quan trọng: Xóa luôn các Membership liên quan để tránh rác dữ liệu
                mMembershipsRef.orderByChild("boardId").equalTo(boardId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    ds.getRef().removeValue();
                                }
                                callback.onSuccess();
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onSuccess(); }
                        });
            } else callback.onError(task.getException().getMessage());
        });
    }

    public void updateBoardBackground(String boardId, Background background, GeneralCallback callback) {
        mBoardsRef.child(boardId).child("background").setValue(background)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) callback.onSuccess();
                    else callback.onError(task.getException().getMessage());
                });
    }
    @Override
    public void updateMemberPermission(String boardId, String userId, String newPermission, GeneralCallback callback) {
        // Tìm node membership tương ứng với boardId và userId
        mMembershipsRef.orderByChild("boardId").equalTo(boardId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean found = false;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Membership m = ds.getValue(Membership.class);
                            if (m != null && m.getUserId().equals(userId)) {
                                // Cập nhật trường permission
                                ds.getRef().child("permission").setValue(newPermission)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) callback.onSuccess();
                                            else callback.onError(task.getException().getMessage());
                                        });
                                found = true;
                                break;
                            }
                        }
                        if (!found) callback.onError("Không tìm thấy thành viên.");
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) { callback.onError(error.getMessage()); }
                });
    }

}