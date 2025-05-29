package com.example.doantotnghiep.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.ProductAdapter;
import com.example.doantotnghiep.databinding.ActivitySearchBinding;
import com.example.doantotnghiep.listener.IClickProductListener;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private ActivitySearchBinding binding;
    private EditText edtSearch;
    private RecyclerView rcvProduct;
    private LinearLayout layoutEmptyState;
    private TextView tvEmptyMessage;

    private List<Product> mListProduct;
    private List<Product> mListProductDisplay;
    private ProductAdapter mProductAdapter;
    private ValueEventListener mValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initUi();
        initListener();
        loadAllProductsFromFirebase();


    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.imgToolbarBack;
        imgToolbarBack.setOnClickListener(view -> onBackPressed());
    }

    private void initUi() {
        edtSearch = binding.edtSearch;
        rcvProduct = binding.rcvProduct;
        layoutEmptyState = binding.tvNoResult;
        tvEmptyMessage = binding.tvEmptyMessage;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvProduct.setLayoutManager(linearLayoutManager);

        mListProduct = new ArrayList<>();
        mListProductDisplay = new ArrayList<>();
        mProductAdapter = new ProductAdapter(mListProductDisplay, new IClickProductListener() {
            @Override
            public void onClickProductItem(Product product) {
                Bundle bundle = new Bundle();
                bundle.putLong(Constant.PRODUCT_ID, product.getId());
                GlobalFunction.startActivity(SearchActivity.this, ProductDetailActivity.class, bundle);
            }
        });
        rcvProduct.setAdapter(mProductAdapter);
    }

    private void initListener() {
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchProduct();
                return true;
            }
            return false;
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString().trim();
                if (StringUtil.isEmpty(keyword)) {
                    displayRandomProducts();
                } else {
                    searchProduct();
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadAllProductsFromFirebase() {

        showProgressDialog(true);
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showProgressDialog(false);
                mListProduct.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null) {
                        mListProduct.add(product);
                    }
                }
                displayRandomProducts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showProgressDialog(false);
                showToastMessage(getString(R.string.msg_get_date_error));
            }
        };
        MyApplication.get(this).getProductDatabaseReference()
                .addValueEventListener(mValueEventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void displayRandomProducts() {
        if (mListProduct == null || mListProduct.isEmpty()) {
            showEmptyState(getString(R.string.msg_no_search_results));
            return;
        }

        mListProductDisplay.clear();
        mListProductDisplay.addAll(mListProduct);
        Collections.shuffle(mListProductDisplay);

        showProductList();
        if (mProductAdapter != null) {
            mProductAdapter.notifyDataSetChanged();
        }
    }

    private void searchProduct() {

        String keyword = edtSearch.getText().toString().trim();
        if (StringUtil.isEmpty(keyword)) {
            displayRandomProducts();
            return;
        }

        searchProductByKeyword(keyword);
        GlobalFunction.hideSoftKeyboard(this);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void searchProductByKeyword(String keyword) {
        if (mListProduct == null || mListProduct.isEmpty()) {
            showEmptyState(getString(R.string.msg_no_search_results));
            return;
        }

        mListProductDisplay.clear();
        String searchKeyword = GlobalFunction.getTextSearch(keyword).toLowerCase().trim();

        for (Product product : mListProduct) {
            if (product.getName() != null &&
                    GlobalFunction.getTextSearch(product.getName()).toLowerCase().trim()
                            .contains(searchKeyword)) {
                mListProductDisplay.add(product);
            } else if (product.getDescription() != null &&
                    GlobalFunction.getTextSearch(product.getDescription()).toLowerCase().trim()
                            .contains(searchKeyword)) {
                mListProductDisplay.add(product);
            }
        }

        if (mListProductDisplay.isEmpty()) {
            showEmptyState(getString(R.string.msg_no_search_results, keyword));
        } else {
            showProductList();
        }

        if (mProductAdapter != null) {
            mProductAdapter.notifyDataSetChanged();
        }
    }

    private void showProductList() {
        rcvProduct.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState(String message) {
        rcvProduct.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mValueEventListener != null) {
            MyApplication.get(this).getProductDatabaseReference()
                    .removeEventListener(mValueEventListener);
        }
    }
}