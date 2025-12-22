package com.kan_n.ui.fragments.menu_bang;

import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.kan_n.data.interfaces.BoardRepository;
import com.kan_n.data.interfaces.InvitationRepository;
import com.kan_n.data.models.User;
import com.kan_n.data.repository.BoardRepositoryImpl;
import com.kan_n.data.repository.InvitationRepositoryImpl;

import java.util.List;

public class MenuBangViewModel extends ViewModel {
    private final BoardRepository boardRepository;
    // Sửa kiểu dữ liệu LiveData
    private final MutableLiveData<List<Pair<User, String>>> membersList;

    private InvitationRepository invitationRepository = new InvitationRepositoryImpl();

    public MenuBangViewModel() {
        boardRepository = new BoardRepositoryImpl();
        membersList = new MutableLiveData<>();
    }

    public LiveData<List<Pair<User, String>>> getMembersList() {
        return membersList;
    }

    public void loadMembers(String boardId) {
        boardRepository.getBoardMembers(boardId, new BoardRepository.BoardMembersCallback() {
            @Override
            public void onSuccess(List<Pair<User, String>> members) {
                membersList.setValue(members);
            }

            @Override
            public void onError(String message) {
                // Xử lý lỗi
            }
        });
    }

    public void sendInvite(String boardId, String boardName, String email, String role) {
        invitationRepository.sendInvitation(boardId, boardName, email, role, new InvitationRepository.InviteCallback() {
            @Override
            public void onSuccess() {
                // Có thể dùng LiveData để báo về UI là thành công
                // Ví dụ: messageLiveData.setValue("Đã gửi lời mời!");
            }
            @Override
            public void onError(String message) {
                // messageLiveData.setValue("Lỗi: " + message);
            }
        });
    }
}