package com.kan_n.ui.fragments.taobangmoi;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kan_n.data.interfaces.BackgroundRepository;
import com.kan_n.data.interfaces.BoardRepository; // ✨ Bổ sung
import com.kan_n.data.models.Background;
import com.kan_n.data.models.Workspace; // ✨ Bổ sung
import com.kan_n.data.repository.BackgroundRepositoryImpl;
import com.kan_n.data.repository.BoardRepositoryImpl; // ✨ Bổ sung
import com.kan_n.utils.FirebaseUtils; // ✨ Bổ sung

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList; // ✨ Bổ sung
import java.util.Map;
import java.util.stream.Collectors;

public class TaoBangMoiViewModel extends ViewModel {

    // Repositories
    private final BackgroundRepository backgroundRepository;
    private final BoardRepository boardRepository;

    // --- Backgrounds (Đã có từ trước) ---
    private List<Background> allBackgrounds = new ArrayList<>();
    private final MutableLiveData<List<Background>> _filteredBackgroundList = new MutableLiveData<>();
    public LiveData<List<Background>> getFilteredBackgroundList() { return _filteredBackgroundList; }
    private final MutableLiveData<Background> _selectedBackground = new MutableLiveData<>();
    public LiveData<Background> getSelectedBackground() { return _selectedBackground; }
    private boolean hasDataLoaded = false;
    private final MutableLiveData<List<Workspace>> _workspaces = new MutableLiveData<>();
    public LiveData<List<Workspace>> getWorkspaces() { return _workspaces; }
    public enum CreateBoardStatus { IDLE, LOADING, SUCCESS, ERROR }
    private final MutableLiveData<CreateBoardStatus> _createStatus = new MutableLiveData<>(CreateBoardStatus.IDLE);
    public LiveData<CreateBoardStatus> getCreateStatus() { return _createStatus; }


    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() { return _error; }


    public TaoBangMoiViewModel() {
        this.backgroundRepository = new BackgroundRepositoryImpl();
        this.boardRepository = new BoardRepositoryImpl();
        // Đặt phông nền mặc định
        _selectedBackground.setValue(new Background("color", "#0079BF"));
    }

    // --- Logic Backgrounds (Giữ nguyên) ---
    public void loadAllBackgrounds() {
        if (hasDataLoaded) return;
        backgroundRepository.getAllBackgrounds(new BackgroundRepository.BackgroundsCallback() {
            @Override
            public void onSuccess(List<Background> backgrounds) {
                allBackgrounds = backgrounds;
                hasDataLoaded = true;
            }
            @Override
            public void onError(String message) {
                _error.postValue(message);
            }
        });
    }

    public void filterBackgrounds(String type) {
        if (!hasDataLoaded) {
            loadAllBackgrounds(); // Tải nếu chưa có
        }
        List<Background> filteredList = allBackgrounds.stream()
                .filter(bg -> type.equalsIgnoreCase(bg.getType()))
                .collect(Collectors.toList());
        _filteredBackgroundList.setValue(filteredList);
    }

    public void selectBackground(Background background) {
        _selectedBackground.setValue(background);
    }

    public void resetCreateStatus() {
        _createStatus.setValue(CreateBoardStatus.IDLE);
    }

    // --- Logic Workspaces ---
    public void loadWorkspaces() {
        String currentUserId = FirebaseUtils.getCurrentUserId();
        if (currentUserId == null) {
            _error.postValue("Người dùng chưa đăng nhập.");
            return;
        }

        // Sử dụng lại hàm repository giống như BangViewModel
        boardRepository.getWorkspacesWithBoards(currentUserId, new BoardRepository.WorkspacesWithBoardsCallback() {
            @Override
            public void onSuccess(List<Workspace> workspaces) {
                _workspaces.postValue(workspaces); // Cập nhật LiveData
            }
            @Override
            public void onError(String message) {
                _error.postValue("Lỗi tải Workspaces: " + message);
            }
        });
    }

    // --- Logic Tạo Bảng ---
    public void createBoard(String boardName, String workspaceId, String visibility, Background background) {
        // 1. Kiểm tra đầu vào
        if (boardName == null || boardName.trim().isEmpty()) {
            _error.postValue("Vui lòng nhập tên bảng.");
            _createStatus.postValue(CreateBoardStatus.IDLE); // Reset
            return;
        }
        if (workspaceId == null) {
            _error.postValue("Vui lòng chọn không gian làm việc.");
            _createStatus.postValue(CreateBoardStatus.IDLE);
            return;
        }

        _createStatus.postValue(CreateBoardStatus.LOADING);

        // 2. Gọi Repository
        boardRepository.createBoard(workspaceId, boardName.trim(), visibility, background, new BoardRepository.GeneralCallback() {
            @Override
            public void onSuccess() {
                _createStatus.postValue(CreateBoardStatus.SUCCESS);
            }
            @Override
            public void onError(String message) {
                _error.postValue(message);
                _createStatus.postValue(CreateBoardStatus.ERROR);
            }
        });
    }
    public void updateBoardBackground(String boardId, Background background, BoardRepository.GeneralCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("background", background); // Firebase sẽ tự động serialize đối tượng Background
        boardRepository.updateBoard(boardId, updates, callback);
    }
}