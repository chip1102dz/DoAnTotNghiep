package com.example.doantotnghiep.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.BannerAdapter;
import com.example.doantotnghiep.adapter.CategoryHomeAdapter;
import com.example.doantotnghiep.databinding.FragmentHomeBinding;
import com.example.doantotnghiep.listener.IClickProductListener;
import com.example.doantotnghiep.model.Category;
import com.example.doantotnghiep.model.CategoryHome;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.model.Test.Banner;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private View mView;
    private ViewPager2 mViewPagerProductFeatured;
    private CircleIndicator3 mCircleIndicatorProductFeatured;
    private ViewPager2 viewPagerCategory;
    private TabLayout tabCategory;
    private EditText edtSearchName;
    private ImageView imgSearch;

    private List<Product> listProductFeatured;
    private List<Category> listCategory;

    private List<CategoryHome> listCategoryHome;

    private List<Banner> listBanner;
    private ValueEventListener mCategoryValueEventListener;
    private ValueEventListener mProductValueEventListener;


    private final Handler mHandlerBanner = new Handler();
    private final Runnable mRunnableBanner = new Runnable() {
        @Override
        public void run() {
            if (mViewPagerProductFeatured == null || listBanner == null || listBanner.isEmpty()) {
                return;
            }
            if (mViewPagerProductFeatured.getCurrentItem() == listBanner.size() - 1) {
                mViewPagerProductFeatured.setCurrentItem(0);
                return;
            }
            mViewPagerProductFeatured.setCurrentItem(mViewPagerProductFeatured.getCurrentItem() + 1);
        }
    };
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);


        //Set Category Home
        CategoryHomeAdapter categoryHomeAdapter = new CategoryHomeAdapter(getListCategoryHome());
        RecyclerView recyclerView = binding.rcvCategoryHome;
        GridLayoutManager gridLayout = new GridLayoutManager (getContext(),5 );
        recyclerView.setLayoutManager(gridLayout);
        recyclerView.setAdapter(categoryHomeAdapter);

        getListCategoryHome();
        displayListBanner();

        return binding.getRoot();
    }
    private void initUi() {
        mViewPagerProductFeatured = binding.viewPagerProductFeatured;
        mCircleIndicatorProductFeatured = binding.indicatorProductFeatured;
        viewPagerCategory = binding.viewPagerCategory;
        viewPagerCategory.setUserInputEnabled(false);
        tabCategory = binding.tabCategory;
    }

    private void getListCategory() {
        if (getActivity() == null) return;

    }



    public List<CategoryHome> getListCategoryHome(){
        listCategoryHome = new ArrayList<>();
        listCategoryHome.add(new CategoryHome(R.drawable.danh_muc, "Danh mục"));
        listCategoryHome.add(new CategoryHome(R.drawable.chicken, "Gà rán"));
        listCategoryHome.add(new CategoryHome(R.drawable.rice, "Cơm"));
        listCategoryHome.add(new CategoryHome(R.drawable.pho, "Phở"));
        listCategoryHome.add(new CategoryHome(R.drawable.pizza, "Pizza"));
        listCategoryHome.add(new CategoryHome(R.drawable.danh_muc, "Danh mục"));
        listCategoryHome.add(new CategoryHome(R.drawable.danh_muc, "Danh mục"));
        listCategoryHome.add(new CategoryHome(R.drawable.danh_muc, "Danh mục"));
        listCategoryHome.add(new CategoryHome(R.drawable.danh_muc, "Danh mục"));
        listCategoryHome.add(new CategoryHome(R.drawable.danh_muc, "Danh mục"));
        return listCategoryHome;

    }
    public void getListProductBanner(){
        if (getActivity() == null) {
            return;
        }
        mProductValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listProductFeatured != null) {
                    listProductFeatured.clear();
                } else {
                    listProductFeatured = new ArrayList<>();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null && product.isFeatured()) {
                        listProductFeatured.add(product);
                    }
                }
                displayListBanner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        MyApplication.get(getActivity()).getProductDatabaseReference()
                .addValueEventListener(mProductValueEventListener);
    }
    private void displayListBanner() {
        BannerAdapter adapter = new BannerAdapter(listProductFeatured, new IClickProductListener() {
            @Override
            public void onClickProductItem(Product product) {
                Bundle bundle = new Bundle();
                bundle.putLong(Constant.PRODUCT_ID, product.getId());
                //GlobalFunction.startActivity(HomeFragment.this.getActivity(), ProductDetailActivity.class, bundle);
            }
        });
        mViewPagerProductFeatured.setAdapter(adapter);
        mCircleIndicatorProductFeatured.setViewPager(mViewPagerProductFeatured);

        mViewPagerProductFeatured.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mHandlerBanner.removeCallbacks(mRunnableBanner);
                mHandlerBanner.postDelayed(mRunnableBanner, 3000);
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}