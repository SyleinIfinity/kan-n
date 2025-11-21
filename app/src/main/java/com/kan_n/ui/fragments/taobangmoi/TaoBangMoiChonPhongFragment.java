package com.kan_n.ui.fragments.taobangmoi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.kan_n.R;
import com.kan_n.databinding.FragmentTaoBangmoiChonphongBinding;

public class TaoBangMoiChonPhongFragment extends Fragment {

    private FragmentTaoBangmoiChonphongBinding binding;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTaoBangmoiChonphongBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        // Nút quay lại
        binding.ivBack.setOnClickListener(v -> navController.popBackStack());

        // Chuyển sang màn hình chọn MÀU SẮC
        binding.cardColors.setOnClickListener(v -> {
            navController.navigate(R.id.action_chonPhongFragment_to_chonMauFragment);
        });

        // Chuyển sang màn hình chọn ẢNH
        binding.cardImage.setOnClickListener(v -> {
            navController.navigate(R.id.action_chonPhongFragment_to_chonAnhFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}