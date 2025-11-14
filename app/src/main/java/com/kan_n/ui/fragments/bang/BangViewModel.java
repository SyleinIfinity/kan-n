package com.kan_n.ui.fragments.bang;

import androidx.annotation.NonNull; // ✨ Thêm
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// ✨ Thêm các import này
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
// ---
import com.kan_n.data.interfaces.BoardRepository;
import com.kan_n.data.models.Workspace;
import com.kan_n.data.repository.BoardRepositoryImpl;
import com.kan_n.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class BangViewModel extends ViewModel {

    private final BoardRepository boardRepository;
    private final MutableLiveData<List<Workspace>> workspacesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    // ✨ 1. Thêm các biến để lắng nghe
    private DatabaseReference membershipsRef;
    private ValueEventListener membershipListener;
    private String currentUserId;

    public BangViewModel() {
        this.boardRepository = new BoardRepositoryImpl();
        this.currentUserId = FirebaseUtils.getCurrentUserId(); // Lấy UID 1 lần
        if (this.currentUserId != null) {
            // ✨ 2. Khởi tạo tham chiếu
            this.membershipsRef = FirebaseUtils.getRootRef().child("memberships");
        }
        // Xóa loadWorkspaces(); ở đây
    }

    public LiveData<List<Workspace>> getWorkspaces() {
        return workspacesLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    /**
     * ✨ 3. TẠO HÀM BẮT ĐẦU LẮNG NGHE
     * Fragment sẽ gọi hàm này trong onViewCreated.
     */
    public void startListeningForChanges() {
        if (currentUserId == null) {
            errorLiveData.setValue("Người dùng chưa đăng nhập.");
            return;
        }

        // Chỉ tạo listener 1 lần duy nhất
        if (membershipListener == null) {
            membershipListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Bất cứ khi nào memberships của user thay đổi (thêm/xóa)
                    // HÃY TẢI LẠI TOÀN BỘ DANH SÁCH BẢNG
                    loadWorkspaces();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    errorLiveData.postValue("Lỗi lắng nghe: " + error.getMessage());
                }
            };

            // ✨ 4. Gắn listener vào query (dùng addValueEventListener)
            // Lắng nghe TẤT CẢ memberships CỦA USER NÀY
            membershipsRef.orderByChild("userId").equalTo(currentUserId)
                    .addValueEventListener(membershipListener);
        }
    }


    /**
     * Phương thức tải dữ liệu thật từ Firebase.
     * (Hàm này giờ sẽ được gọi bởi listener ở trên)
     */
    public void loadWorkspaces() {
        if (currentUserId == null) {
            errorLiveData.setValue("Người dùng chưa đăng nhập.");
            workspacesLiveData.setValue(new ArrayList<>());
            return;
        }

        boardRepository.getWorkspacesWithBoards(currentUserId, new BoardRepository.WorkspacesWithBoardsCallback() {
            @Override
            public void onSuccess(List<Workspace> workspaces) {
                workspacesLiveData.postValue(workspaces);
            }

            @Override
            public void onError(String message) {
                errorLiveData.postValue("Lỗi tải dữ liệu: " + message);
                workspacesLiveData.postValue(new ArrayList<>());
            }
        });
    }

    /**
     * ✨ 5. Dọn dẹp listener khi ViewModel bị hủy
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (membershipListener != null && currentUserId != null) {
            membershipsRef.orderByChild("userId").equalTo(currentUserId)
                    .removeEventListener(membershipListener);
        }
    }
}