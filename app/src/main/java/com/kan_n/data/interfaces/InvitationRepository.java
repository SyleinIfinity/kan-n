package com.kan_n.data.interfaces;

import com.kan_n.data.models.Invitation;
import java.util.List;

public interface InvitationRepository {

    interface InviteCallback {
        void onSuccess();
        void onError(String message);
    }

    interface GetInvitationsCallback {
        void onSuccess(List<Invitation> invitations);
        void onError(String message);
    }

    // Gửi lời mời
    void sendInvitation(String boardId, String boardName, String receiverEmail, String role, InviteCallback callback);

    // Lấy danh sách lời mời của User hiện tại
    void getMyInvitations(GetInvitationsCallback callback);

    // Chấp nhận lời mời
    void acceptInvitation(Invitation invitation, InviteCallback callback);

    // Từ chối lời mời (Xóa lời mời)
    void declineInvitation(String invitationId, InviteCallback callback);
}