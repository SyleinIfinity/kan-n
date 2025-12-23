package com.kan_n.ui.fragments.hoatdong;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kan_n.data.interfaces.InvitationRepository;
import com.kan_n.data.models.Invitation;
import com.kan_n.data.repository.InvitationRepositoryImpl;

import java.util.List;

public class HoatDongViewModel extends ViewModel {

    private final InvitationRepository invitationRepository;
    private final MutableLiveData<List<Invitation>> mInvitations;
    private final MutableLiveData<String> mMessage;

    public HoatDongViewModel() {
        invitationRepository = new InvitationRepositoryImpl();
        mInvitations = new MutableLiveData<>();
        mMessage = new MutableLiveData<>();

        // Tự động kích hoạt lắng nghe khi ViewModel được tạo
        startListening();
    }

    public LiveData<List<Invitation>> getInvitations() {
        return mInvitations;
    }

    public LiveData<String> getMessage() {
        return mMessage;
    }

    // ✨ [MỚI] Hàm xóa thông báo để tránh hiển thị lặp lại
    public void clearMessage() {
        mMessage.setValue(null);
    }

    public void startListening() {
        // Gọi hàm lắng nghe Realtime từ Repository
        invitationRepository.getMyInvitations(new InvitationRepository.GetInvitationsCallback() {
            @Override
            public void onSuccess(List<Invitation> invitations) {
                mInvitations.setValue(invitations);
            }

            @Override
            public void onError(String message) {
                mMessage.setValue("Lỗi tải dữ liệu: " + message);
            }
        });
    }

    public void acceptInvite(Invitation invitation) {
        invitationRepository.acceptInvitation(invitation, new InvitationRepository.InviteCallback() {
            @Override
            public void onSuccess() {
                mMessage.setValue("Đã tham gia bảng: " + invitation.getBoardName());
                // Không cần gọi load lại, Repository tự lo
            }

            @Override
            public void onError(String message) {
                mMessage.setValue("Lỗi khi chấp nhận: " + message);
            }
        });
    }

    public void declineInvite(String invitationId) {
        invitationRepository.declineInvitation(invitationId, new InvitationRepository.InviteCallback() {
            @Override
            public void onSuccess() {
                mMessage.setValue("Đã từ chối lời mời");
                // Không cần gọi load lại
            }

            @Override
            public void onError(String message) {
                mMessage.setValue("Lỗi khi từ chối: " + message);
            }
        });
    }

    // ✨ [QUAN TRỌNG] Hủy lắng nghe khi thoát màn hình để tránh rò rỉ bộ nhớ
    @Override
    protected void onCleared() {
        super.onCleared();
        invitationRepository.removeListener();
    }
}