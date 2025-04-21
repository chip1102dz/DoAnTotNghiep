package com.example.doantotnghiep.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.doantotnghiep.fragment.AccountFragment;
import com.example.doantotnghiep.fragment.HomeFragment;
import com.example.doantotnghiep.fragment.NotificationFragment;
import com.example.doantotnghiep.fragment.OrderFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 1:
                return new OrderFragment();
            case 2:
                return new NotificationFragment();
            case 3:
                return new AccountFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
