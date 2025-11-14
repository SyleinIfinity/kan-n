package com.kan_n.ui.fragments.hoatdong;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.kan_n.databinding.FragmentHoatdongBinding;

public class HoatDongFragment extends Fragment {

    private FragmentHoatdongBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HoatDongViewModel hoatDongViewModel =
                new ViewModelProvider(this).get(HoatDongViewModel.class);

        binding = FragmentHoatdongBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textHoatdong;
//        hoatDongViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}