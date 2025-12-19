package com.kan_n.data.repository;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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

    public InvitationRepositoryImpl() {
        this.mRootRef = FirebaseUtils.getRootRef();
    }

    @Override
    public void sendInvitation(String boardId, String boardName, String receiverEmail, String role, InviteCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId();

        // 1. Tìm thông tin người gửi để lấy Tên
        mRootRef.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot senderSnap) {
                User sender = senderSnap.getValue(User.class);
                String senderName = (sender != null) ? sender.getDisplayName() : "Ai đó";

                // 2. Tìm ID của người nhận thông qua Email
                mRootRef.child("users").orderByChild("email").equalTo(receiverEmail)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    callback.onError("Không tìm thấy người dùng với email này.");
                                    return;
                                }

                                // Lấy user đầu tiên tìm thấy
                                DataSnapshot userSnap = snapshot.getChildren().iterator().next();
                                String receiverId = userSnap.getKey();

                                // Kiểm tra xem đã là thành viên chưa (Optional - bỏ qua cho đơn giản, hoặc check membership)

                                // 3. Tạo Invitation
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
        // Lấy các lời mời mà receiverId == currentUserId
        mRootRef.child("invitations").orderByChild("receiverId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Invitation> list = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Invitation inv = snap.getValue(Invitation.class);
                            if (inv != null && "pending".equals(inv.getStatus())) {
                                inv.setUid(snap.getKey());
                                list.add(inv);
                            }
                        }
                        callback.onSuccess(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    @Override
    public void acceptInvitation(Invitation invitation, InviteCallback callback) {
        // Khi chấp nhận:
        // 1. Tạo Membership mới
        // 2. Xóa lời mời (hoặc update status = accepted)

        String membershipId = mRootRef.child("memberships").push().getKey();
        Membership newMember = new Membership(invitation.getBoardId(), invitation.getReceiverId(), invitation.getRole());

        Map<String, Object> updates = new HashMap<>();
        updates.put("/memberships/" + membershipId, newMember.toMap());
        updates.put("/invitations/" + invitation.getUid(), null); // Xóa luôn cho gọn, hoặc setStatus("accepted")

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
        mRootRef.child("invitations").child(invitationId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) callback.onSuccess();
                    else callback.onError(task.getException().getMessage());
                });
    }
}