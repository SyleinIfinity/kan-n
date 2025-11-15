package com.kan_n.ui.fragments.thongtin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.kan_n.databinding.FragmentTrangCaidatBinding;

public class TrangCaiDatFragment extends Fragment {
    private FragmentTrangCaidatBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ThongTinViewModel thongTinViewModel =
                new ViewModelProvider(this).get(ThongTinViewModel.class);

        binding = FragmentTrangCaidatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
