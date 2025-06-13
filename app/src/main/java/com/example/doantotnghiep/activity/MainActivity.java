package com.example.doantotnghiep.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.ViewPagerAdapter;
import com.example.doantotnghiep.database.ProductDatabase;
import com.example.doantotnghiep.databinding.ActivityMainBinding;
import com.example.doantotnghiep.event.DisplayCartEvent;
import com.example.doantotnghiep.helper.NotificationScheduler;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ViewPager2 mViewPager2;
    private BottomNavigationView mBottomNavigationView;
    private RelativeLayout layoutCartBottom;
    private TextView tvCountItem, tvProductsName, tvAmount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (!DataStoreManager.getUser().isAdmin()) {
            NotificationScheduler scheduler = new NotificationScheduler(this);
            scheduler.startScheduling();
        }
        initUi();
        mViewPager2 = binding.viewpager2;
        mBottomNavigationView = binding.bottomNavigation;

        mBottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.nav_home){
                    mViewPager2.setCurrentItem(0);
                }else if(id == R.id.nav_order){
                    mViewPager2.setCurrentItem(1);
                }else if(id == R.id.nav_notification) {
                    mViewPager2.setCurrentItem(2);
                }else{
                    mViewPager2.setCurrentItem(3);
                }
                return true;
            }
        });

        setUpViewPager2();
        displayLayoutCartBottom();
    }
    private void initUi() {
        layoutCartBottom = binding.layoutCartBottom;
        tvCountItem = binding.tvCountItem;
        tvProductsName = binding.tvProductsName;
        tvAmount = binding.tvAmount;
    }
    private void setUpViewPager2(){
        mViewPager2.setUserInputEnabled(false);
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(this);
        mViewPager2.setAdapter(mViewPagerAdapter);
        mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position){
                    case 0:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
                        break;
                    case 1:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_order).setChecked(true);
                        break;
                    case 2:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_notification).setChecked(true);
                        break;
                    case 3:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_account).setChecked(true);
                        break;
                }
            }
        });
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        showConfirmExitApp();
    }

    private void showConfirmExitApp() {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.app_name))
                .content(getString(R.string.msg_exit_app))
                .positiveText(getString(R.string.action_ok))
                .onPositive((dialog, which) -> finish())
                .negativeText(getString(R.string.action_cancel))
                .cancelable(false)
                .show();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDisplayCartEvent(DisplayCartEvent event) {
        displayLayoutCartBottom();
    }
    private void displayLayoutCartBottom() {
        List<Product> listProduct = ProductDatabase.getInstance(this).productDAO().getListProductCart();
        if (listProduct == null || listProduct.isEmpty()) {
            layoutCartBottom.setVisibility(View.GONE);
        } else {
            layoutCartBottom.setVisibility(View.VISIBLE);
            String strCountItem = listProduct.size() + " " + getString(R.string.label_item);
            tvCountItem.setText(strCountItem);

            String strProductsName = "";
            for (Product product : listProduct) {
                if (StringUtil.isEmpty(strProductsName)) {
                    strProductsName += product.getName();
                } else {
                    strProductsName += ", " + product.getName();
                }
            }
            if (StringUtil.isEmpty(strProductsName)) {
                tvProductsName.setVisibility(View.GONE);
            } else {
                tvProductsName.setVisibility(View.VISIBLE);
                tvProductsName.setText(strProductsName);
            }

            int amount = 0;
            for (Product product : listProduct) {
                amount = amount + product.getTotalPrice();
            }
            String strAmount = amount + Constant.CURRENCY;
            tvAmount.setText(strAmount);
        }
        layoutCartBottom.setOnClickListener(v ->
                GlobalFunction.startActivity(this, CartActivity.class));
    }

    public ViewPager2 getViewPager2() {
        return mViewPager2;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}