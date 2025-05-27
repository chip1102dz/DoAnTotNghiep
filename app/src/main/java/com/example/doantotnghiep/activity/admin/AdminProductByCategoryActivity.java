package com.example.doantotnghiep.activity.admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.BaseActivity;
import com.example.doantotnghiep.adapter.admin.AdminProductAdapter;
import com.example.doantotnghiep.databinding.ActivityAdminProductByCategoryBinding;
import com.example.doantotnghiep.listener.IOnAdminManagerProductListener;
import com.example.doantotnghiep.model.Category;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminProductByCategoryActivity extends BaseActivity {
    ActivityAdminProductByCategoryBinding binding;
    private List<Product> mListProduct;
    private AdminProductAdapter mAdminProductAdapter;
    private Category mCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProductByCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadDataIntent();
        initView();
        loadListProduct();
    }

    private void loadDataIntent() {
        Bundle bundleReceived = getIntent().getExtras();
        if (bundleReceived != null) {
            mCategory = (Category) bundleReceived.get(Constant.KEY_INTENT_CATEGORY_OBJECT);
        }
    }

    private void initView() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        imgToolbarBack.setOnClickListener(view -> onBackPressed());
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        tvToolbarTitle.setText(mCategory.getName());

        RecyclerView rcvData = binding.rcvData;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvData.setLayoutManager(linearLayoutManager);
        mListProduct = new ArrayList<>();
        mAdminProductAdapter = new AdminProductAdapter(mListProduct, new IOnAdminManagerProductListener() {
            @Override
            public void onClickUpdateProduct(Product product) {
                onClickEditProduct(product);
            }

            @Override
            public void onClickDeleteProduct(Product product) {
                deleteProductItem(product);
            }
        });
        rcvData.setAdapter(mAdminProductAdapter);
    }

    private void onClickEditProduct(Product product) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.KEY_INTENT_PRODUCT_OBJECT, product);
        GlobalFunction.startActivity(this, AdminAddProductActivity.class, bundle);
    }

    private void deleteProductItem(Product product) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok), (dialogInterface, i) -> MyApplication.get(this).getProductDatabaseReference()
                        .child(String.valueOf(product.getId())).removeValue((error, ref) ->
                                Toast.makeText(this,
                                        getString(R.string.msg_delete_product_successfully),
                                        Toast.LENGTH_SHORT).show()))
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private void resetListProduct() {
        if (mListProduct != null) {
            mListProduct.clear();
        } else {
            mListProduct = new ArrayList<>();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadListProduct() {
        MyApplication.get(this).getProductDatabaseReference()
                .orderByChild("category_id").equalTo(mCategory.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        resetListProduct();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Product product = dataSnapshot.getValue(Product.class);
                            if (product == null) return;
                            mListProduct.add(0, product);
                        }
                        if (mAdminProductAdapter != null) mAdminProductAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}