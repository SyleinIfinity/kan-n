package com.kan_n.ui.fragments.hoatdong;

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

import com.kan_n.data.models.Invitation;
import com.kan_n.databinding.FragmentHoatdongBinding;
import com.kan_n.ui.adapters.adapter.InvitationAdapter;

import java.util.ArrayList;

public class HoatDongFragment extends Fragment implements InvitationAdapter.OnInvitationActionListener {

    private FragmentHoatdongBinding binding;
    private HoatDongViewModel hoatDongViewModel;
    private InvitationAdapter invitationAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        hoatDongViewModel = new ViewModelProvider(this).get(HoatDongViewModel.class);
        binding = FragmentHoatdongBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupInvitationRecyclerView();
        setupObservers();

        binding.btnFilterAll.setOnClickListener(v -> Toast.makeText(getContext(), "Lọc tất cả", Toast.LENGTH_SHORT).show());
        binding.btnFilterUnread.setOnClickListener(v -> Toast.makeText(getContext(), "Lọc chưa đọc", Toast.LENGTH_SHORT).show());
    }

    private void setupInvitationRecyclerView() {
        invitationAdapter = new InvitationAdapter(new ArrayList<>(), this);
        binding.rcvInvitations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rcvInvitations.setAdapter(invitationAdapter);
    }

    private void setupObservers() {
        // 1. Quan sát danh sách lời mời (Realtime)
        hoatDongViewModel.getInvitations().observe(getViewLifecycleOwner(), invitations -> {
            if (invitations != null && !invitations.isEmpty()) {
                invitationAdapter.setData(invitations);
                binding.tvTitleLoimoi.setVisibility(View.VISIBLE);
                binding.rcvInvitations.setVisibility(View.VISIBLE);
                binding.viewDividerLoimoi.setVisibility(View.VISIBLE);
            } else {
                binding.tvTitleLoimoi.setVisibility(View.GONE);
                binding.rcvInvitations.setVisibility(View.GONE);
                binding.viewDividerLoimoi.setVisibility(View.GONE);
            }
        });

        // 2. Quan sát thông báo và xử lý lỗi lặp lại
        hoatDongViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                //Xóa thông báo ngay sau khi hiện để không bị lặp lại lần sau
                hoatDongViewModel.clearMessage();
            }
        });
    }

    @Override
    public void onAccept(Invitation invitation) {
        hoatDongViewModel.acceptInvite(invitation);
    }

    @Override
    public void onDecline(Invitation invitation) {
        hoatDongViewModel.declineInvite(invitation.getUid());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Không cần gọi load thủ công nữa
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}