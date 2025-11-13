package com.kan_n.ui.fragments.bang;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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

    public BangViewModel() {
        this.boardRepository = new BoardRepositoryImpl();
        loadWorkspaces();
    }

    public LiveData<List<Workspace>> getWorkspaces() {
        return workspacesLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }


    /**
     * Phương thức tải dữ liệu thật từ Firebase.
     */
    public void loadWorkspaces() {
        String currentUserId = FirebaseUtils.getCurrentUserId(); // Lấy UID của user đang đăng nhập
        if (currentUserId == null) {
            errorLiveData.setValue("Người dùng chưa đăng nhập.");
            workspacesLiveData.setValue(new ArrayList<>());
            return;
        }

        // Gọi Repository để lấy dữ liệu
        boardRepository.getWorkspacesWithBoards(currentUserId, new BoardRepository.WorkspacesWithBoardsCallback() {
            @Override
            public void onSuccess(List<Workspace> workspaces) {
                // Sử dụng postValue vì đây là callback từ Firebase (background thread)
                workspacesLiveData.postValue(workspaces);
            }

            @Override
            public void onError(String message) {
                errorLiveData.postValue("Lỗi tải dữ liệu: " + message);
                workspacesLiveData.postValue(new ArrayList<>());
            }
        });
    }
}