package com.example.doantotnghiep.fragment;

import static android.view.View.GONE;
import static com.example.doantotnghiep.utils.GlobalFunction.getTextSearch;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.greenrobot.eventbus.EventBus;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.BannerAdapter;
import com.example.doantotnghiep.adapter.CategoryHomeAdapter;
import com.example.doantotnghiep.adapter.FilterAdapter;
import com.example.doantotnghiep.adapter.HomeProductFeaturedAdapter;
import com.example.doantotnghiep.databinding.FragmentHomeBinding;
import com.example.doantotnghiep.listener.IClickProductListener;
import com.example.doantotnghiep.model.Category;
import com.example.doantotnghiep.model.CategoryHome;
import com.example.doantotnghiep.model.Filter;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.utils.Constant;

import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView rcvFilter;
    private RecyclerView rcvProduct;
    private ViewPager2 mViewPagerProductFeatured;
    private CircleIndicator3 mCircleIndicatorProductFeatured;
    private List<Product> listProductKeyWord;
    private List<Filter> listFilter;
    private List<Product> listProduct;
    private List<Product> listProductDisplay;
    private List<Product> listProductFeatured;
    private FilterAdapter filterAdapter;
    private Filter currentFilter;
    private long categoryId;

    ProgressBar loadingProductFeature;
    private String keyword = "";
    private HomeProductFeaturedAdapter productAdapter;
    private ValueEventListener mCategoryValueEventListener;
    private ValueEventListener mProductValueEventListener;
    private ValueEventListener mValueEventListener;


    private final Handler mHandlerBanner = new Handler();
    private final Runnable mRunnableBanner = new Runnable() {
        @Override
        public void run() {
            if (mViewPagerProductFeatured == null || listProductFeatured == null || listProductFeatured.isEmpty()) {
                return;
            }
            if (mViewPagerProductFeatured.getCurrentItem() == listProductFeatured.size() - 1) {
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
        GridLayoutManager gridLayout = new GridLayoutManager(getContext(), 5);
        recyclerView.setLayoutManager(gridLayout);
        recyclerView.setAdapter(categoryHomeAdapter);


        initUi();
        listProductFeatured = new ArrayList<>();
        getListProductBanner();
        getListFilter();
        getListProduct();
        displayListProduct();
        return binding.getRoot();
    }

    private void initUi() {
        mViewPagerProductFeatured = binding.viewPagerProductFeatured;
        mCircleIndicatorProductFeatured = binding.indicatorProductFeatured;
        rcvFilter = binding.rcvFilter;
        rcvProduct = binding.rcvHomeProductFeatured;

        ViewPager2 viewPagerCategory = binding.viewPagerCategory;
        viewPagerCategory.setUserInputEnabled(false);
        TabLayout tabCategory = binding.tabCategory;
        loadingProductFeature = binding.LoadingHomeProductFeatured;
    }

    private void getListCategory() {
        if (getActivity() == null) return;

    }


    public List<CategoryHome> getListCategoryHome() {
        List<CategoryHome> listCategoryHome = new ArrayList<>();
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

    public void getListProductBanner() {
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
        if (listProductFeatured == null || listProductFeatured.isEmpty()) {
            return;
        }
        BannerAdapter adapter = new BannerAdapter(listProductFeatured, new IClickProductListener() {
            @Override
            public void onClickProductItem(Product product) {
//                Bundle bundle = new Bundle();
//                bundle.putLong(Constant.PRODUCT_ID, product.getId());
//                //GlobalFunction.startActivity(HomeFragment.this.getActivity(), ProductDetailActivity.class, bundle);
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

    private void getListFilter() {
        listFilter = new ArrayList<>();
        listFilter.add(new Filter(Filter.TYPE_FILTER_ALL, getString(R.string.filter_all)));
        listFilter.add(new Filter(Filter.TYPE_FILTER_RATE, getString(R.string.filter_rate)));
        listFilter.add(new Filter(Filter.TYPE_FILTER_PRICE, getString(R.string.filter_price)));
        listFilter.add(new Filter(Filter.TYPE_FILTER_PROMOTION, getString(R.string.filter_promotion)));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        rcvFilter.setLayoutManager(linearLayoutManager);
        currentFilter = listFilter.get(0);
        currentFilter.setSelected(true);
        filterAdapter = new FilterAdapter(getActivity(), listFilter, this::handleClickFilter);
        rcvFilter.setAdapter(filterAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void handleClickFilter(Filter filter) {
        for (Filter filterEntity : listFilter) {
            if (filterEntity.getId() == filter.getId()) {
                filterEntity.setSelected(true);
                setListProductDisplay(filterEntity);
                currentFilter = filterEntity;
            } else {
                filterEntity.setSelected(false);
            }
        }
        if (filterAdapter != null) filterAdapter.notifyDataSetChanged();
    }

    private void getListProduct() {
        if (getActivity() == null) return;
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingProductFeature.setVisibility(GONE);
                if (listProduct != null) {
                    listProduct.clear();
                } else {
                    listProduct = new ArrayList<>();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null) {
                        listProduct.add(0, product);
                    }
                }
                setListProductDisplay(new Filter(Filter.TYPE_FILTER_ALL, getString(R.string.filter_all)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        MyApplication.get(getActivity()).getProductDatabaseReference()
                .orderByChild(Constant.CATEGORY_ID).equalTo(categoryId)
                .addValueEventListener(mValueEventListener);
    }

    private void displayListProduct() {
        if (getActivity() == null) return;
        listProductDisplay = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rcvProduct.setLayoutManager(linearLayoutManager);
        productAdapter = new HomeProductFeaturedAdapter(listProductDisplay, new IClickProductListener() {
            @Override
            public void onClickProductItem(Product product) {
//                Bundle bundle = new Bundle();
//                bundle.putLong(Constant.PRODUCT_ID, product.getId());
//                GlobalFunction.startActivity(getActivity(), ProductDetailActivity.class, bundle);
            }
        });

        rcvProduct.setAdapter(productAdapter);
    }

    private void setListProductDisplay(@NonNull Filter filter) {
        if (listProduct == null || listProduct.isEmpty()) return;
        if (listProductDisplay != null) {
            listProductDisplay.clear();
        } else {
            listProductDisplay = new ArrayList<>();
        }
        switch (filter.getId()) {
            case Filter.TYPE_FILTER_ALL:
                listProductDisplay.addAll(listProduct);
                break;

            case Filter.TYPE_FILTER_RATE:
                listProductDisplay.addAll(listProduct);
                Collections.sort(listProductDisplay,
                        (product1, product2) -> Double.compare(product2.getRate(), product1.getRate()));
                break;

            case Filter.TYPE_FILTER_PRICE:
                listProductDisplay.addAll(listProduct);
                Collections.sort(listProductDisplay,
                        (product1, product2) -> Integer.compare(product1.getRealPrice(), product2.getRealPrice()));
                break;

            case Filter.TYPE_FILTER_PROMOTION:
                for (Product product : listProduct) {
                    if (product.getSale() > 0) listProductDisplay.add(product);
                }
                break;
        }
        reloadListProduct();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void reloadListProduct() {
        if (productAdapter != null) productAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (filterAdapter != null) filterAdapter.release();
        if (getActivity() != null && mValueEventListener != null) {
            MyApplication.get(getActivity()).getProductDatabaseReference()
                    .removeEventListener(mValueEventListener);
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}