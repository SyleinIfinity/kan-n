package com.kan_n.ui.fragments.bang;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kan_n.R;
import com.kan_n.data.models.Board; // <-- Them import
import com.kan_n.databinding.FragmentBangBinding;
import com.kan_n.ui.adapters.adapter.BoardAdapter; // <-- Them import
import com.kan_n.ui.adapters.adapter.WorkspaceAdapter;

import java.util.ArrayList;

// ✨ 1. Implement interface
public class BangFragment extends Fragment implements BoardAdapter.OnBoardClickListener {

    private FragmentBangBinding binding;

    private BangViewModel bangViewModel;
    private WorkspaceAdapter workspaceAdapter;
    private RecyclerView rvWorkspaces;
    private NavController navController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bangViewModel = new ViewModelProvider(this).get(BangViewModel.class);
        binding = FragmentBangBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        rvWorkspaces = binding.rvWorkspaces;

        // Thiet lap Adapter
        setupRecyclerView();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);

        binding.btnTaoBangMoi.setOnClickListener(v -> {
            navController.navigate(R.id.action_bangFragment_to_taoBangMoiFragment);
        });

        bangViewModel.getWorkspaces().observe(getViewLifecycleOwner(), workspaces -> {
            if (workspaces != null) {
                workspaceAdapter.updateData(workspaces);
            }
        });
    }

    private void setupRecyclerView() {
        // ✨ 2. Truyen "this" (vi fragment nay da implement interface)
        workspaceAdapter = new WorkspaceAdapter(getContext(), new ArrayList<>(), this);
        rvWorkspaces.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWorkspaces.setAdapter(workspaceAdapter);
    }

    // ✨ 3. Implement phuong thuc cua interface
    @Override
    public void onBoardClick(Board board) {
        if (navController != null && board != null) {
            // Chuan bi cac tham so de truyen
            Bundle args = new Bundle();
            args.putString("boardId", board.getUid());
            args.putString("boardTitle", board.getName());

            // Thuc hien dieu huong, dung action ID tu nav graph
            navController.navigate(R.id.action_bangFragment_to_bangSpaceFragment, args);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}