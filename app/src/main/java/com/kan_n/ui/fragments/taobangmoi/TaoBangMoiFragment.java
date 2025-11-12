package com.kan_n.ui.fragments.taobangmoi; // Tạo package mới

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.kan_n.databinding.FragmentTaobangMoiBinding; // Binding được tạo tự động

public class TaoBangMoiFragment extends Fragment {

    private FragmentTaobangMoiBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTaobangMoiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Thêm logic cho fragment này ở đây (ví dụ: nút Save, EditText...)
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}