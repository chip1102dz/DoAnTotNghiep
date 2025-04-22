package com.example.doantotnghiep.fragment;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.HomeProductRatingAdapter;
import com.example.doantotnghiep.adapter.ProductAdapter;
import com.example.doantotnghiep.databinding.FragmentProductBinding;
import com.example.doantotnghiep.listener.IClickProductListener;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.utils.Constant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


public class ProductFragment extends Fragment {
    private RecyclerView rcvProduct;
    private List<Product> listProduct;
    private List<Product> listProductDisplay;
    private ProductAdapter productAdapter;
    private long categoryId;

    private ValueEventListener mValueProductListener;
    FragmentProductBinding binding;

    public static ProductFragment newInstance(long categoryId) {
        ProductFragment productFragment = new ProductFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(Constant.CATEGORY_ID, categoryId);
        productFragment.setArguments(bundle);
        return productFragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);
        getDataArguments();
        initUi();
        getListProduct();
        return binding.getRoot();
    }

    private void initUi() {
        rcvProduct = binding.rcvProduct;
    }

    private void getDataArguments() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        categoryId = bundle.getLong(Constant.CATEGORY_ID);
    }
    private void getListProduct() {
        if (getActivity() == null) return;
        mValueProductListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                displayListProduct();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi rồi", Toast.LENGTH_SHORT).show();
            }
        };
        MyApplication.get(getActivity()).getProductDatabaseReference()
                .orderByChild(Constant.CATEGORY_ID).equalTo(categoryId)
                .addValueEventListener(mValueProductListener);
    }
    private void displayListProduct() {
        if (getActivity() == null) return;
        listProductDisplay = new ArrayList<>();
        listProductDisplay.addAll(listProduct);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rcvProduct.setLayoutManager(linearLayoutManager);
        productAdapter = new ProductAdapter(listProductDisplay, new IClickProductListener() {
            @Override
            public void onClickProductItem(Product product) {
//                Bundle bundle = new Bundle();
//                bundle.putLong(Constant.PRODUCT_ID, product.getId());
//                GlobalFunction.startActivity(getActivity(), ProductDetailActivity.class, bundle);
            }
        });
        rcvProduct.setAdapter(productAdapter);
        reloadListProductRating();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void reloadListProductRating() {
        if (productAdapter != null) productAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null && mValueProductListener != null) {
            MyApplication.get(getActivity()).getProductDatabaseReference()
                    .removeEventListener(mValueProductListener);
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}