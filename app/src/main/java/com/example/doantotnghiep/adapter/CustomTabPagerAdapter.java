package com.example.doantotnghiep.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.doantotnghiep.fragment.CustomProductFragment;

public class CustomTabPagerAdapter extends FragmentStateAdapter {

    public static final int TAB_BEST_SELLER = 0;
    public static final int TAB_NEWEST = 1;
    public static final int TAB_DISCOUNT = 2;

    public CustomTabPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case TAB_BEST_SELLER:
                return CustomProductFragment.newInstance(TAB_BEST_SELLER);
            case TAB_NEWEST:
                return CustomProductFragment.newInstance(TAB_NEWEST);
            case TAB_DISCOUNT:
                return CustomProductFragment.newInstance(TAB_DISCOUNT);
            default:
                return CustomProductFragment.newInstance(TAB_BEST_SELLER);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}