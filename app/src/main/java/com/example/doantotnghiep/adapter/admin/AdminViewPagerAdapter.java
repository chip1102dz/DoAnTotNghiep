package com.example.doantotnghiep.adapter.admin;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.doantotnghiep.fragment.admin.AdminCategoryFragment;
import com.example.doantotnghiep.fragment.admin.AdminOrderFragment;
import com.example.doantotnghiep.fragment.admin.AdminProductFragment;
import com.example.doantotnghiep.fragment.admin.AdminSettingsFragment;

public class AdminViewPagerAdapter extends FragmentStateAdapter {

    public AdminViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new AdminProductFragment();

            case 2:
                return new AdminOrderFragment();

            case 3:
                return new AdminSettingsFragment();

            default:
                return new AdminCategoryFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
