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
    private final MutableLiveData<List<Invitation>> mInvitations; // LiveData chứa danh sách lời mời
    private final MutableLiveData<String> mMessage; // LiveData để hiển thị thông báo (Toast)

    public HoatDongViewModel() {
        invitationRepository = new InvitationRepositoryImpl();
        mInvitations = new MutableLiveData<>();
        mMessage = new MutableLiveData<>();

        // Tự động tải danh sách khi ViewModel được khởi tạo
        loadInvitations();
    }

    public LiveData<List<Invitation>> getInvitations() {
        return mInvitations;
    }

    public LiveData<String> getMessage() {
        return mMessage;
    }

    // Hàm tải danh sách lời mời từ Firebase
    public void loadInvitations() {
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

    // Hàm chấp nhận lời mời
    public void acceptInvite(Invitation invitation) {
        invitationRepository.acceptInvitation(invitation, new InvitationRepository.InviteCallback() {
            @Override
            public void onSuccess() {
                mMessage.setValue("Đã tham gia bảng: " + invitation.getBoardName());
                loadInvitations(); // Tải lại danh sách để cập nhật giao diện
            }

            @Override
            public void onError(String message) {
                mMessage.setValue("Lỗi khi chấp nhận: " + message);
            }
        });
    }

    // Hàm từ chối lời mời
    public void declineInvite(String invitationId) {
        invitationRepository.declineInvitation(invitationId, new InvitationRepository.InviteCallback() {
            @Override
            public void onSuccess() {
                mMessage.setValue("Đã từ chối lời mời");
                loadInvitations(); // Tải lại danh sách
            }

            @Override
            public void onError(String message) {
                mMessage.setValue("Lỗi khi từ chối: " + message);
            }
        });
    }
}