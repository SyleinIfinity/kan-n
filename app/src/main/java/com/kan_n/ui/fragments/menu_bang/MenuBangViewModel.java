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
import com.kan_n.utils.FirebaseUtils;

import java.util.List;

public class MenuBangViewModel extends ViewModel {
    private final BoardRepository boardRepository;
    private String currentUserRole = "member";
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
            }
            @Override
            public void onError(String message) {
            }
        });
    }
    // Cập nhật tên bảng
    public void updateBoardName(String boardId, String newName, BoardRepository.GeneralCallback callback) {
        boardRepository.updateBoard(boardId, newName, callback);
    }

    public void deleteBoard(String boardId, BoardRepository.GeneralCallback callback) {
        boardRepository.deleteBoard(boardId, callback);
    }

    // Lấy danh sách thành viên bảng
    public void getBoardMembers(String boardId, BoardRepository.BoardMembersCallback callback) {
        boardRepository.getBoardMembers(boardId, callback);
    }

    // Lấy vai trò của user hiện tại trong bảng
    public void fetchCurrentUserRole(String boardId) {
        String currentUserId = FirebaseUtils.getCurrentUserId();
        boardRepository.getBoardMembers(boardId, new BoardRepository.BoardMembersCallback() {
            @Override
            public void onSuccess(List<Pair<User, String>> members) {
                for (Pair<User, String> member : members) {
                    if (member.first.getUid().equals(currentUserId)) {
                        currentUserRole = member.second;
                        break;
                    }
                }
            }
            @Override
            public void onError(String message) {}
        });
    }

    public String getCurrentUserRole() {
        return currentUserRole;
    }

    public void setCurrentUserRole(String role) {
        this.currentUserRole = role;
    }
    public void updateMemberPermission(String boardId, String userId, String newPermission, BoardRepository.GeneralCallback callback) {
        ((BoardRepositoryImpl) boardRepository).updateMemberPermission(boardId, userId, newPermission, callback);
    }
    public void leaveOrRemoveMember(String boardId, String userId, BoardRepository.GeneralCallback callback) {
        boardRepository.removeMemberFromBoard(boardId, userId, callback);
    }
}