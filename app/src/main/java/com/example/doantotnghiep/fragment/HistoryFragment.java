package com.example.doantotnghiep.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.MainActivity;
import com.example.doantotnghiep.adapter.OrderPagerAdapter;
import com.example.doantotnghiep.databinding.FragmentHistoryBinding;
import com.example.doantotnghiep.model.TabOrder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    FragmentHistoryBinding binding;
    private ViewPager2 viewPagerOrder;
    private TabLayout tabOrder;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        initToolbar();
        initUi();
        displayTabsOrder();
        return binding.getRoot();
    }
    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> backToHomeScreen());
        tvToolbarTitle.setText(getString(R.string.nav_history));
    }

    private void backToHomeScreen() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;
        mainActivity.getViewPager2().setCurrentItem(0);
    }

    private void initUi() {
        viewPagerOrder = binding.viewPagerOrder;
        viewPagerOrder.setUserInputEnabled(false);
        tabOrder = binding.tabOrder;
    }

    private void displayTabsOrder() {
        List<TabOrder> list = new ArrayList<>();
        list.add(new TabOrder(TabOrder.TAB_ORDER_PROCESS, getString(R.string.label_process)));
        list.add(new TabOrder(TabOrder.TAB_ORDER_DONE, getString(R.string.label_done)));
        if (getActivity() == null) return;
        viewPagerOrder.setOffscreenPageLimit(list.size());
        OrderPagerAdapter adapter = new OrderPagerAdapter(getActivity(), list);
        viewPagerOrder.setAdapter(adapter);
        new TabLayoutMediator(tabOrder, viewPagerOrder,
                (tab, position) -> tab.setText(list.get(position).getName().toLowerCase()))
                .attach();
    }
}