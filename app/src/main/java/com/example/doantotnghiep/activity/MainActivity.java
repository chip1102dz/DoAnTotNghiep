package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.ViewPagerAdapter;
import com.example.doantotnghiep.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ViewPager2 mViewPager2;
    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
}