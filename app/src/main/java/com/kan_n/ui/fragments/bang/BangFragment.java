package com.kan_n.ui.fragments.bang;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.kan_n.databinding.FragmentBangBinding;

public class BangFragment extends Fragment {

    private FragmentBangBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BangViewModel bangViewModel =
                new ViewModelProvider(this).get(BangViewModel.class);

        binding = FragmentBangBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textBang;
        bangViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}