package com.kan_n.ui.fragments.bang;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kan_n.data.models.Board;
import com.kan_n.data.models.Workspace;

import java.util.ArrayList;
import java.util.List;

public class BangViewModel extends ViewModel {

    // Sử dụng MutableLiveData để giữ danh sách các không gian làm việc
    private final MutableLiveData<List<Workspace>> workspacesLiveData = new MutableLiveData<>();

    public BangViewModel() {
        loadWorkspaces();
    }

    public LiveData<List<Workspace>> getWorkspaces() {
        return workspacesLiveData;
    }

    // Phương thức tải dữ liệu
    // Trong thực tế, bạn sẽ gọi Repository để lấy dữ liệu từ Firebase/API ở đây
    private void loadWorkspaces() {
        // --- DỮ LIỆU GIẢ (DEMO) ---
        // Thay thế phần này bằng logic tải dữ liệu thật
        List<Workspace> demoList = new ArrayList<>();

        // Workspace 1
        List<Board> boards1 = new ArrayList<>();
        boards1.add(new Board("board1", "Bảng dự án A", "https://example.com/image1.jpg", true, null));
        boards1.add(new Board("board2", "Kế hoạch Marketing", "https://example.com/image2.jpg", false, null));
        boards1.add(new Board("board3", "Kế hoạch Marketing", "https://example.com/image2.jpg", false, null));
        demoList.add(new Workspace("ws1", "Không gian làm việc của Kan-n", boards1));

        // Workspace 2
        List<Board> boards2 = new ArrayList<>();
        boards2.add(new Board("board3", "Phát triển App", "https://example.com/image3.jpg", false, null));
        demoList.add(new Workspace("ws2", "Dự án cá nhân", boards2));

        // Cập nhật LiveData
        workspacesLiveData.setValue(demoList);
        // --- KẾT THÚC DỮ LIỆU GIẢ ---
    }
}