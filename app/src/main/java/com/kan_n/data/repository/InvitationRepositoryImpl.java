package com.kan_n.data.repository;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.interfaces.InvitationRepository;
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

    @Override
    public void sendInvitation(String boardId, String boardName, String receiverEmail, String role, InviteCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId();

        mRootRef.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot senderSnap) {
                User sender = senderSnap.getValue(User.class);
                String senderName = (sender != null) ? sender.getDisplayName() : "Ai đó";

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

                                String inviteId = mRootRef.child("invitations").push().getKey();
                                Invitation invitation = new Invitation(boardId, boardName, currentUserId, senderName, receiverId, receiverEmail, role);

                                mRootRef.child("invitations").child(inviteId).setValue(invitation.toMap())
                                        .addOnCompleteListener(task -> {
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
            public void onCancelled(@NonNull DatabaseError error) { callback.onError(error.getMessage()); }
        });
    }

    @Override
    public void getMyInvitations(GetInvitationsCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId();
        if (currentUserId == null) return;

        // Xóa listener cũ nếu có để tránh trùng lặp
        removeListener();

        //Tạo Query lắng nghe Realtime
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

    //Hàm dọn dẹp listener
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
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    @Override
    public void declineInvitation(String invitationId, InviteCallback callback) {
        // Xóa lời mời -> Listener tự động cập nhật UI
        mRootRef.child("invitations").child(invitationId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) callback.onSuccess();
                    else callback.onError(task.getException().getMessage());
                });
    }
}