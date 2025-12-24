package com.kan_n.ui.fragments.hoatdong;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.kan_n.data.interfaces.InvitationRepository;
import com.kan_n.data.models.Activity; // Import model Activity
import com.kan_n.data.models.Invitation;
import com.kan_n.data.repository.InvitationRepositoryImpl;
import com.kan_n.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HoatDongViewModel extends ViewModel {

    private final InvitationRepository invitationRepository;
    private final MutableLiveData<List<Invitation>> mInvitations;
    private final MutableLiveData<List<Activity>> mActivities; // LiveData cho Hoạt động
    private final MutableLiveData<String> mMessage;

    private ValueEventListener activityListener;
    private com.google.firebase.database.Query activityQuery;

    public HoatDongViewModel() {
        invitationRepository = new InvitationRepositoryImpl();
        mInvitations = new MutableLiveData<>();
        mActivities = new MutableLiveData<>();
        mMessage = new MutableLiveData<>();

        // Tự động kích hoạt lắng nghe khi ViewModel được tạo
        startListening();
    }

    public LiveData<List<Invitation>> getInvitations() {
        return mInvitations;
    }

    public LiveData<List<Activity>> getActivities() { // Getter cho Activity
        return mActivities;
    }

    public LiveData<String> getMessage() {
        return mMessage;
    }

    public void clearMessage() {
        mMessage.setValue(null);
    }

    public void startListening() {
        // 1. Lắng nghe Lời mời (Logic cũ)
        invitationRepository.getMyInvitations(new InvitationRepository.GetInvitationsCallback() {
            @Override
            public void onSuccess(List<Invitation> invitations) {
                mInvitations.setValue(invitations);
            }

            @Override
            public void onError(String message) {
                mMessage.setValue("Lỗi tải lời mời: " + message);
            }
        });

        // 2. Lắng nghe Hoạt động (Logic mới)
        String currentUserId = FirebaseUtils.getCurrentUserId();
        if (currentUserId != null) {
            // Lắng nghe node activities/{userId}
            activityQuery = FirebaseUtils.getRootRef().child("activities").child(currentUserId).orderByKey().limitToLast(50); // Lấy 50 hoạt động gần nhất

            activityListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Activity> list = new ArrayList<>();
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Activity activity = snap.getValue(Activity.class);
                        if (activity != null) {
                            list.add(activity);
                        }
                    }
                    // Đảo ngược danh sách để hiển thị hoạt động mới nhất lên đầu
                    Collections.reverse(list);
                    mActivities.setValue(list);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Có thể log lỗi nếu cần
                }
            };
            activityQuery.addValueEventListener(activityListener);
        }
    }

    public void acceptInvite(Invitation invitation) {
        invitationRepository.acceptInvitation(invitation, new InvitationRepository.InviteCallback() {
            @Override
            public void onSuccess() {
                mMessage.setValue("Đã tham gia bảng: " + invitation.getBoardName());
                // Không cần gọi load lại, Listener Activity sẽ tự cập nhật
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
            }

            @Override
            public void onError(String message) {
                mMessage.setValue("Lỗi khi từ chối: " + message);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        invitationRepository.removeListener();

        // Hủy lắng nghe Activity
        if (activityQuery != null && activityListener != null) {
            activityQuery.removeEventListener(activityListener);
        }
    }
}