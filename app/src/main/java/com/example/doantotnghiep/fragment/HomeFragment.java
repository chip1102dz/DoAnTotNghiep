package com.example.doantotnghiep.fragment;

import static android.view.View.GONE;


import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.ChatBotAiActivity;
import com.example.doantotnghiep.activity.ProductDetailActivity;
import com.example.doantotnghiep.activity.SearchActivity;
import com.example.doantotnghiep.adapter.BannerAdapter;
import com.example.doantotnghiep.adapter.CategoryHomeAdapter;
import com.example.doantotnghiep.adapter.CustomTabPagerAdapter;
import com.example.doantotnghiep.adapter.FilterAdapter;
import com.example.doantotnghiep.adapter.HomeProductFeaturedAdapter;
import com.example.doantotnghiep.adapter.HomeProductRatingAdapter;
import com.example.doantotnghiep.adapter.SearchFeatureAdapter;
import com.example.doantotnghiep.databinding.FragmentHomeBinding;
import com.example.doantotnghiep.listener.IClickProductListener;

import com.example.doantotnghiep.listener.IClickSearchListener;
import com.example.doantotnghiep.model.Category;
import com.example.doantotnghiep.model.CategoryHome;
import com.example.doantotnghiep.model.Filter;
import com.example.doantotnghiep.model.Product;

import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rcvFilter;
    private RecyclerView rcvSearchHomeFeature;
    private RecyclerView rcvProductFeatured, rcvProductRating;
    private ViewPager2 mViewPagerProductFeatured;
    private CircleIndicator3 mCircleIndicatorProductFeatured;
    private List<Filter> listFilter;
    private List<Category> listCategory;

    private Button btnSearch;
    private FloatingActionButton fabChatBot; // Thêm FloatingActionButton
    private TabLayout tabCategory;
    private ViewPager2 viewPagerCategory;
    private List<Product> listProductFeatured, listProductRating, listProductBanner;
    private List<Product> listProductDisplay;
    private FilterAdapter filterAdapter;
    private Filter currentFilter;
    private long categoryId;
    ProgressBar loadingProductFeature, loadingProductRating;
    private HomeProductFeaturedAdapter productFeaturedAdapter;
    private ValueEventListener mCategoryValueEventListener;
    private ValueEventListener mValueProductFeaturedListener;
    private ValueEventListener mValueProductRatingListener;

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

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GlobalFunction.startActivity(HomeFragment.this.getActivity(), SearchActivity.class);
            }
        });

        // Thêm listener cho FloatingActionButton
        fabChatBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GlobalFunction.startActivity(HomeFragment.this.getActivity(), ChatBotAiActivity.class);
            }
        });

        displayListProductFeatured();
        getListProductBanner();
        getListFilter();
        getListProductFeatured();
        setListSearchFeatureDisplay();
        getListProductRating();
        displayTabsCategory();
        setLoadMoreAction();
        return binding.getRoot();
    }

    private void initUi() {
        btnSearch = binding.homeSearch;
        fabChatBot = binding.fabChatBot; // Khởi tạo FloatingActionButton
        mViewPagerProductFeatured = binding.viewPagerProductFeatured;
        mCircleIndicatorProductFeatured = binding.indicatorProductFeatured;
        rcvFilter = binding.rcvFilter;
        rcvSearchHomeFeature = binding.rcvHomeSearchFeature;
        rcvProductFeatured = binding.rcvHomeProductFeatured;
        rcvProductRating = binding.rcvHomeProductRating;
        viewPagerCategory = binding.viewPagerCategory;
        viewPagerCategory.setUserInputEnabled(false);
        tabCategory = binding.tabCategory;
        loadingProductFeature = binding.LoadingHomeProductFeatured;
        loadingProductRating = binding.LoadingHomeRating;
        swipeRefreshLayout = binding.refreshLayout;
    }

    public List<CategoryHome> getListCategoryHome() {
        List<CategoryHome> listCategoryHomes = new ArrayList<>();
        listCategoryHomes.add(new CategoryHome(R.drawable.danh_muc, "Danh mục"));
        listCategoryHomes.add(new CategoryHome(R.drawable.chicken, "Gà rán"));
        listCategoryHomes.add(new CategoryHome(R.drawable.rice, "Cơm"));
        listCategoryHomes.add(new CategoryHome(R.drawable.pho, "Phở"));
        listCategoryHomes.add(new CategoryHome(R.drawable.pizza, "Pizza"));
        listCategoryHomes.add(new CategoryHome(R.drawable.orange_juice, "Nước ép"));
        listCategoryHomes.add(new CategoryHome(R.drawable.bubble_tea, "Trà sữa"));
        listCategoryHomes.add(new CategoryHome(R.drawable.cupcake, "Kem"));
        listCategoryHomes.add(new CategoryHome(R.drawable.fruit, "Trà"));
        listCategoryHomes.add(new CategoryHome(R.drawable.egg_rolls, "Món cuốn"));
        return listCategoryHomes;

    }

    public void getListProductBanner() {
        if (getActivity() == null) {
            return;
        }
        ValueEventListener mBannerValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listProductBanner != null) {
                    listProductBanner.clear();
                } else {
                    listProductBanner = new ArrayList<>();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null && product.isFeatured()) {
                        listProductBanner.add(product);
                    }
                }
                displayListBanner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        MyApplication.get(getActivity()).getProductDatabaseReference()
                .addValueEventListener(mBannerValueEventListener);
    }

    private void displayListBanner() {
        if (listProductBanner == null || listProductBanner.isEmpty()) {
            return;
        }
        BannerAdapter adapter = new BannerAdapter(listProductBanner, new IClickProductListener() {
            @Override
            public void onClickProductItem(Product product) {
                Bundle bundle = new Bundle();
                bundle.putLong(Constant.PRODUCT_ID, product.getId());
                GlobalFunction.startActivity(HomeFragment.this.getActivity(), ProductDetailActivity.class, bundle);
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
        filterAdapter = new FilterAdapter(getContext(), listFilter, this::handleClickFilter);
        rcvFilter.setAdapter(filterAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void handleClickFilter(Filter filter) {
        for (Filter filterEntity : listFilter) {
            if (filterEntity.getId() == filter.getId()) {
                filterEntity.setSelected(true);
                setListProductDisplayFeatured(filterEntity);
                currentFilter = filterEntity;
            } else {
                filterEntity.setSelected(false);
            }
        }
        if (filterAdapter != null) filterAdapter.notifyDataSetChanged();
    }

    private void getListProductFeatured() {
        if (getActivity() == null) return;
        mValueProductFeaturedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingProductFeature.setVisibility(GONE);
                if (listProductFeatured != null) {
                    listProductFeatured.clear();
                } else {
                    listProductFeatured = new ArrayList<>();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null) {
                        listProductFeatured.add(0, product);
                    }
                }
                setListProductDisplayFeatured(new Filter(Filter.TYPE_FILTER_ALL, getString(R.string.filter_all)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi rồi", Toast.LENGTH_SHORT).show();
            }
        };
        MyApplication.get(getActivity()).getProductDatabaseReference()
                .addValueEventListener(mValueProductFeaturedListener);
    }

    private void displayListProductFeatured() {
        if (getActivity() == null) return;
        listProductDisplay = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rcvProductFeatured.setLayoutManager(linearLayoutManager);
        productFeaturedAdapter = new HomeProductFeaturedAdapter(listProductDisplay, new IClickProductListener() {
            @Override
            public void onClickProductItem(Product product) {
                Bundle bundle = new Bundle();
                bundle.putLong(Constant.PRODUCT_ID, product.getId());
                GlobalFunction.startActivity(getActivity(), ProductDetailActivity.class, bundle);
            }
        });

        rcvProductFeatured.setAdapter(productFeaturedAdapter);
    }

    private void setListProductDisplayFeatured(@NonNull Filter filter) {
        if (listProductFeatured == null || listProductFeatured.isEmpty()) return;
        if (listProductDisplay != null) {
            listProductDisplay.clear();
        } else {
            listProductDisplay = new ArrayList<>();
        }
        switch (filter.getId()) {
            case Filter.TYPE_FILTER_ALL:
                listProductDisplay.addAll(listProductFeatured);
                break;

            case Filter.TYPE_FILTER_RATE:
                listProductDisplay.addAll(listProductFeatured);
                Collections.sort(listProductDisplay,
                        (product1, product2) -> Double.compare(product2.getRate(), product1.getRate()));
                break;

            case Filter.TYPE_FILTER_PRICE:
                listProductDisplay.addAll(listProductFeatured);
                Collections.sort(listProductDisplay,
                        (product1, product2) -> Integer.compare(product1.getRealPrice(), product2.getRealPrice()));
                break;

            case Filter.TYPE_FILTER_PROMOTION:
                for (Product product : listProductFeatured) {
                    if (product.getSale() > 0) listProductDisplay.add(product);
                }
                break;
        }
        reloadListProductFeatured();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void reloadListProductFeatured() {
        if (productFeaturedAdapter != null) productFeaturedAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void setListSearchFeatureDisplay(){
        List<Category> listSearchFeature = new ArrayList<>();
        listSearchFeature.add(new Category("Tất cả"));
        listSearchFeature.add(new Category("Bim Bim"));
        listSearchFeature.add(new Category("Coca"));
        listSearchFeature.add(new Category("Tăm cay"));
        listSearchFeature.add(new Category("Kem"));
        listSearchFeature.add(new Category("Bán mỳ"));

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rcvSearchHomeFeature = binding.rcvHomeSearchFeature;
        rcvSearchHomeFeature.setLayoutManager(layoutManager2);

        // Thêm listener để xử lý click
        SearchFeatureAdapter searchFeatureAdapter = new SearchFeatureAdapter(listSearchFeature, getContext(), new IClickSearchListener() {
            @Override
            public void onClickSearchItem(Category category) {
                // Chuyển sang SearchActivity với keyword
                Bundle bundle = new Bundle();
                // Nếu là "Tất cả" thì không truyền keyword
                if (!"Tất cả".equals(category.getName())) {
                    bundle.putString(Constant.SEARCH_KEYWORD, category.getName());
                }
                GlobalFunction.startActivity(getActivity(), SearchActivity.class, bundle);
            }
        });
        rcvSearchHomeFeature.setAdapter(searchFeatureAdapter);
    }

    private void getListProductRating() {
        if (getActivity() == null) return;
        mValueProductRatingListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingProductRating.setVisibility(GONE);
                if (listProductRating != null) {
                    listProductRating.clear();
                } else {
                    listProductRating = new ArrayList<>();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null) {
                        listProductRating.add(0, product);
                    }
                }
                displayListProductRating();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi rồi", Toast.LENGTH_SHORT).show();
            }
        };
        MyApplication.get(getActivity()).getProductDatabaseReference()
                .addValueEventListener(mValueProductRatingListener);
    }
    private void displayListProductRating() {
        if (getActivity() == null) return;
        List<Product> listProductRatingDisplay = new ArrayList<>();
        listProductRatingDisplay.addAll(listProductRating);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rcvProductRating.setLayoutManager(linearLayoutManager);
        HomeProductRatingAdapter productRatingAdapter = new HomeProductRatingAdapter(listProductRatingDisplay, new IClickProductListener() {
            @Override
            public void onClickProductItem(Product product) {
                Bundle bundle = new Bundle();
                bundle.putLong(Constant.PRODUCT_ID, product.getId());
                GlobalFunction.startActivity(getActivity(), ProductDetailActivity.class, bundle);
            }
        });
        rcvProductRating.setAdapter(productRatingAdapter);
        reloadListProductRating();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void reloadListProductRating() {
        if (productFeaturedAdapter != null) productFeaturedAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void displayTabsCategory() {
        if (getActivity() == null) return;

        viewPagerCategory.setOffscreenPageLimit(3);
        CustomTabPagerAdapter customTabPagerAdapter = new CustomTabPagerAdapter(getActivity());
        viewPagerCategory.setAdapter(customTabPagerAdapter);

        new TabLayoutMediator(tabCategory, viewPagerCategory,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0:
                                tab.setText("Bán chạy");
                                break;
                            case 1:
                                tab.setText("Mới nhất");
                                break;
                            case 2:
                                tab.setText("Giảm giá");
                                break;
                        }
                    }
                })
                .attach();
    }
    private void setLoadMoreAction() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getListProductFeatured();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (filterAdapter != null) filterAdapter.release();
        if (getActivity() != null && mValueProductFeaturedListener != null) {
            MyApplication.get(getActivity()).getProductDatabaseReference()
                    .removeEventListener(mValueProductFeaturedListener);
        }
        if (getActivity() != null && mValueProductRatingListener != null) {
            MyApplication.get(getActivity()).getProductDatabaseReference()
                    .removeEventListener(mValueProductRatingListener);
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}