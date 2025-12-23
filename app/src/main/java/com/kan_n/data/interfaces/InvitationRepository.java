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

    void sendInvitation(String boardId, String boardName, String receiverEmail, String role, InviteCallback callback);

    // Hàm này sẽ chuyển sang chế độ lắng nghe liên tục
    void getMyInvitations(GetInvitationsCallback callback);

    void acceptInvitation(Invitation invitation, InviteCallback callback);
    void declineInvitation(String invitationId, InviteCallback callback);

    // ✨ [MỚI] Thêm hàm này để hủy lắng nghe khi thoát màn hình
    void removeListener();
}