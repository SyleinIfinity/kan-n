package com.kan_n.ui.adapters.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.kan_n.ui.fragments.bang.BangFragment;
import com.kan_n.ui.fragments.hoatdong.HoatDongFragment;
import com.kan_n.ui.fragments.thongtin.ThongTinFragment;

public class MainViewPagerAdapter extends FragmentStateAdapter {

    public MainViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Trả về Fragment tương ứng với vị trí tab
        switch (position) {
            case 0:
                return new BangFragment();
            case 1:
                return new HoatDongFragment();
            case 2:
                return new ThongTinFragment();
            default:
                return new BangFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Tổng số tab chính (Bảng, Hoạt động, Thông tin)
    }
}