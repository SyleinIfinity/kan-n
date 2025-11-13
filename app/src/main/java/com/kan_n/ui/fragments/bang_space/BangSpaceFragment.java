package com.kan_n.ui.fragments.bang_space;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.kan_n.R;
import com.kan_n.data.models.ListModel;
import com.kan_n.databinding.FragmentBangSpaceBinding;
import com.kan_n.ui.adapters.adapter.ListModelAdapter;

public class BangSpaceFragment extends Fragment {

    private FragmentBangSpaceBinding binding;
    private BangSpaceViewModel viewModel;
    private ListModelAdapter listModelAdapter;

    private String boardId;
    private String boardTitle; // ✨ Bien de luu title

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BangSpaceViewModel.class);

        // ✨ 1. Nhan tham so
        if (getArguments() != null) {
            boardId = getArguments().getString("boardId");
            boardTitle = getArguments().getString("boardTitle");
        } else {
            // Xu ly loi neu khong co tham so
            boardId = null;
            boardTitle = "Lỗi";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBangSpaceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupRecyclerView();

        // Kiem tra boardId truoc khi lang nghe
        if (boardId != null) {
            listenForLists(boardId);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID Bảng", Toast.LENGTH_LONG).show();
            // Quay lai man hinh truoc
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }

    private void setupToolbar() {
        // ✨ 2. Hien thi tieu de
        if (boardTitle != null) {
            binding.tvBoardTitleToolbar.setText(boardTitle);
        }
        binding.ivBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        // (Gan listener cho nut thong bao, menu...)
    }

    private void setupRecyclerView() {
        listModelAdapter = new ListModelAdapter(getContext(), viewModel);
        // ID tu fragment_bang_space.xml la rv_lists
        binding.rvLists.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvLists.setAdapter(listModelAdapter);
    }

    private void listenForLists(String boardId) {
        viewModel.listenForLists(boardId, new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ListModel list = snapshot.getValue(ListModel.class);
                if (list != null) {
                    list.setUid(snapshot.getKey());
                    listModelAdapter.addList(list);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ListModel list = snapshot.getValue(ListModel.class);
                if (list != null) {
                    list.setUid(snapshot.getKey());
                    listModelAdapter.updateList(list);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                listModelAdapter.removeList(snapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Loi tai danh sach: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}