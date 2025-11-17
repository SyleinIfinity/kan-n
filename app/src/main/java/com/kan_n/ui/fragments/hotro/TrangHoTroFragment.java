package com.kan_n.ui.fragments.hotro;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.kan_n.databinding.FragmentTrangHotroV1Binding;


public class TrangHoTroFragment extends Fragment {
    private FragmentTrangHotroV1Binding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentTrangHotroV1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}