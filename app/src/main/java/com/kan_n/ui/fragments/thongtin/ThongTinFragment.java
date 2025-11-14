package com.kan_n.ui.fragments.thongtin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.kan_n.databinding.FragmentThongtinBinding;

public class ThongTinFragment extends Fragment {

    private FragmentThongtinBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ThongTinViewModel thongTinViewModel =
                new ViewModelProvider(this).get(ThongTinViewModel.class);

        binding = FragmentThongtinBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}