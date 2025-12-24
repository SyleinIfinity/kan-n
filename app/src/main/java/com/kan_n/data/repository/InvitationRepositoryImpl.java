package com.kan_n.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.interfaces.InvitationRepository;
import com.kan_n.data.models.Activity;
import com.kan_n.data.models.Invitation;
import com.kan_n.data.models.Membership;
import com.kan_n.data.models.User;
import com.kan_n.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvitationRepositoryImpl implements InvitationRepository {
    private final DatabaseReference mRootRef;

    private ValueEventListener invitationListener;
    private Query invitationQuery;

    public InvitationRepositoryImpl() {
        this.mRootRef = FirebaseUtils.getRootRef();
    }

    // --- Hàm phụ: Ghi lại hoạt động vào node activities của một userId cụ thể ---
    private void logActivity(String userId, String content) {
        if (userId == null) return;
        String activityId = mRootRef.child("activities").child(userId).push().getKey();
        // Tạo đối tượng Activity với thời gian hiện tại
        Activity activity = new Activity(content, System.currentTimeMillis());
        if (activityId != null) {
            mRootRef.child("activities").child(userId).child(activityId).setValue(activity);
        }
    }

    @Override
    public void sendInvitation(String boardId, String boardName, String receiverEmail, String role, InviteCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId();

        // BƯỚC 1: Lấy thông tin Người gửi (Sender) để có tên hiển thị đẹp
        mRootRef.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot senderSnap) {
                User sender = senderSnap.getValue(User.class);
                String senderName = (sender != null && sender.getDisplayName() != null) ? sender.getDisplayName() : "Ai đó";

                // BƯỚC 2: Tìm Người nhận (Receiver) qua Email
                mRootRef.child("users").orderByChild("email").equalTo(receiverEmail)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    callback.onError("Không tìm thấy người dùng với email này.");
                                    return;
                                }

                                DataSnapshot userSnap = snapshot.getChildren().iterator().next();
                                String receiverId = userSnap.getKey();

                                // BƯỚC 3: Lấy TÊN BẢNG chính xác từ Database (tránh trường hợp tên cũ bị sai)
                                mRootRef.child("boards").child(boardId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot boardNameSnap) {
                                        // Nếu tìm thấy tên trong DB thì dùng, không thì dùng tạm tên truyền vào
                                        String realBoardName = boardNameSnap.exists() ? boardNameSnap.getValue(String.class) : boardName;

                                        // BƯỚC 4: Tạo và gửi lời mời
                                        String inviteId = mRootRef.child("invitations").push().getKey();
                                        Invitation invitation = new Invitation(boardId, realBoardName, currentUserId, senderName, receiverId, receiverEmail, role);

                                        if (inviteId != null) {
                                            mRootRef.child("invitations").child(inviteId).setValue(invitation.toMap())
                                                    .addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            // --- GHI LOG 2 CHIỀU ---

                                                            // 1. Log cho Mình (Người gửi)
                                                            logActivity(currentUserId, "Bạn đã mời " + receiverEmail + " vào bảng " + realBoardName);

                                                            // 2. Log cho Bạn bè (Người nhận)
                                                            logActivity(receiverId, senderName + " đã mời bạn tham gia bảng " + realBoardName);

                                                            callback.onSuccess();
                                                        } else {
                                                            callback.onError(task.getException().getMessage());
                                                        }
                                                    });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        callback.onError("Lỗi khi lấy tên bảng: " + error.getMessage());
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
            public void onCancelled(@NonNull DatabaseError error) { callback.onError(error.getMessage()); }
        });
    }

    @Override
    public void getMyInvitations(GetInvitationsCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId();
        if (currentUserId == null) return;

        // Xóa listener cũ nếu có để tránh trùng lặp
        removeListener();

        // Tạo Query lắng nghe Realtime
        invitationQuery = mRootRef.child("invitations").orderByChild("receiverId").equalTo(currentUserId);

        invitationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Invitation> list = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Invitation inv = snap.getValue(Invitation.class);
                    // Chỉ lấy lời mời đang chờ (pending)
                    if (inv != null && "pending".equals(inv.getStatus())) {
                        inv.setUid(snap.getKey());
                        list.add(inv);
                    }
                }
                // Trả về danh sách mới nhất ngay lập tức khi Firebase có thay đổi
                callback.onSuccess(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        };

        // Kích hoạt lắng nghe
        invitationQuery.addValueEventListener(invitationListener);
    }

    // Hàm dọn dẹp listener
    @Override
    public void removeListener() {
        if (invitationQuery != null && invitationListener != null) {
            invitationQuery.removeEventListener(invitationListener);
            invitationListener = null;
            invitationQuery = null;
        }
    }

    @Override
    public void acceptInvitation(Invitation invitation, InviteCallback callback) {
        String membershipId = mRootRef.child("memberships").push().getKey();
        Membership newMember = new Membership(invitation.getBoardId(), invitation.getReceiverId(), invitation.getRole());

        Map<String, Object> updates = new HashMap<>();
        updates.put("/memberships/" + membershipId, newMember.toMap());
        // Xóa lời mời -> Listener ở trên sẽ tự động thấy thay đổi và cập nhật UI
        updates.put("/invitations/" + invitation.getUid(), null);

        mRootRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // --- GHI LỊCH SỬ 2 CHIỀU ---
                String currentUserId = FirebaseUtils.getCurrentUserId();
                FirebaseUser user = FirebaseUtils.getCurrentUser();
                String myName = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Một thành viên";

                // 1. Log cho Mình (Người chấp nhận)
                logActivity(currentUserId, "Bạn đã tham gia bảng: " + invitation.getBoardName());

                // 2. Log cho Người gửi lời mời (Sender) - Dùng ID có sẵn trong object Invitation
                if (invitation.getSenderId() != null) {
                    logActivity(invitation.getSenderId(), myName + " đã chấp nhận tham gia bảng " + invitation.getBoardName());
                }

                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    @Override
    public void declineInvitation(String invitationId, InviteCallback callback) {
        // --- LOGIC 2 CHIỀU: Cần đọc thông tin lời mời trước khi xóa ---

        mRootRef.child("invitations").child(invitationId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Nếu đã bị xóa từ trước, cứ báo thành công để UI cập nhật
                    callback.onSuccess();
                    return;
                }

                Invitation invitation = snapshot.getValue(Invitation.class);

                // Tiến hành xóa
                snapshot.getRef().removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String currentUserId = FirebaseUtils.getCurrentUserId();
                        FirebaseUser user = FirebaseUtils.getCurrentUser();
                        String myName = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Ai đó";

                        // 1. Log cho Mình (Người từ chối)
                        logActivity(currentUserId, "Bạn đã từ chối một lời mời tham gia bảng.");

                        // 2. Log cho Người gửi (Sender)
                        if (invitation != null && invitation.getSenderId() != null) {
                            // Dùng tên bảng từ invitation (lưu ý: có thể là tên cũ nếu lúc mời chưa fix,
                            // nhưng thường chấp nhận được khi từ chối)
                            String bName = (invitation.getBoardName() != null) ? invitation.getBoardName() : "bảng công việc";
                            logActivity(invitation.getSenderId(), myName + " đã từ chối lời mời vào " + bName);
                        }

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
}