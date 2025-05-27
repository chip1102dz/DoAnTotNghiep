package com.example.doantotnghiep.fragment.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.OrderPagerAdapter;
import com.example.doantotnghiep.databinding.FragmentAdminOrderBinding;
import com.example.doantotnghiep.model.TabOrder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


import java.util.ArrayList;
import java.util.List;

public class AdminOrderFragment extends Fragment {

    FragmentAdminOrderBinding binding;
    private ViewPager2 viewPagerOrder;
    private TabLayout tabOrder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminOrderBinding.inflate(inflater, container, false);

        initUi();
        displayTabsOrder();

        return binding.getRoot();
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
