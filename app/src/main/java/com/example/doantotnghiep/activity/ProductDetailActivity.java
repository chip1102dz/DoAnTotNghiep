package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.database.ProductDatabase;
import com.example.doantotnghiep.databinding.ActivityProductDetailBinding;
import com.example.doantotnghiep.event.DisplayCartEvent;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlideUtils;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class ProductDetailActivity extends BaseActivity {
    private ImageView imgProduct;
    private TextView tvName;
    private TextView tvPriceSale;
    private TextView tvDescription;
    private TextView tvSub;
    private TextView tvAdd;
    private TextView tvCount;
    private TextView tvRate;
    private TextView tvCountReview;
    private TextView tvInfo;
    private TextView tvTotal;
    private TextView tvAddOrder;
    ActivityProductDetailBinding binding;
    private ValueEventListener mProductValueEventListener;
    private long mProductId;
    private Product mProductOld;
    private Product mProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getDataIntent();
        initUi();
        getProductDetailFromFirebase();
    }
    private void getDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;
        mProductId = bundle.getLong(Constant.PRODUCT_ID);
        if (bundle.get(Constant.PRODUCT_OBJECT) != null) {
            mProductOld = (Product) bundle.get(Constant.PRODUCT_OBJECT);
        }
    }
    private void initUi() {
        imgProduct = binding.imgProduct;
        tvName = binding.tvName;
        tvPriceSale = binding.tvPriceSale;
        tvDescription = binding.tvDescription;
        tvSub = binding.tvSub;
        tvAdd = binding.tvAdd;
        tvCount = binding.tvCount;
        tvCountReview = binding.tvCountReview;
        tvRate = binding.tvRate;
        tvInfo = binding.tvInfo;
        tvTotal = binding.tvTotal;
        tvAddOrder = binding.tvAddOrder;
    }
    private void getProductDetailFromFirebase() {
        showProgressDialog(true);
        mProductValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showProgressDialog(false);
                mProduct = snapshot.getValue(Product.class);
                if (mProduct == null) return;

                initToolbar();
                initData();
                initListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showProgressDialog(false);
                showToastMessage(getString(R.string.msg_get_date_error));
            }
        };
        MyApplication.get(this).getProductDetailDatabaseReference(mProductId)
                .addValueEventListener(mProductValueEventListener);
    }
    private void initToolbar() {
        View toolbarView = findViewById(R.id.toolbar);
        ImageView imgToolbarBack = toolbarView.findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = toolbarView.findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProductDetailActivity.this.finish();
            }
        });
        tvToolbarTitle.setText(mProduct.getName());
    }

    private void initData() {
        if (mProduct == null) return;
        GlideUtils.loadUrl(mProduct.getImage(), imgProduct);
        tvName.setText(mProduct.getName());
        String strPrice = mProduct.getRealPrice() + Constant.CURRENCY;
        tvPriceSale.setText(strPrice);
        tvDescription.setText(mProduct.getDescription());
        if (mProductOld != null) {
            mProduct.setCount(mProductOld.getCount());
        } else {
            mProduct.setCount(1);
        }
        tvCount.setText(String.valueOf(mProduct.getCount()));
        tvRate.setText(String.valueOf(mProduct.getRate()));
        String strCountReview = "(" + mProduct.getCountReviews() + ")";
        tvCountReview.setText(strCountReview);

        if (mProduct.getInfo() != null) {
            tvInfo.setText(mProduct.getInfo());
        }

        calculatorTotalPrice();
    }

    private void initListener() {
        tvSub.setOnClickListener(v -> {
            int count = Integer.parseInt(tvCount.getText().toString());
            if (count <= 1) {
                return;
            }
            int newCount = Integer.parseInt(tvCount.getText().toString()) - 1;
            tvCount.setText(String.valueOf(newCount));

            calculatorTotalPrice();
        });

        tvAdd.setOnClickListener(v -> {
            int newCount = Integer.parseInt(tvCount.getText().toString()) + 1;
            tvCount.setText(String.valueOf(newCount));

            calculatorTotalPrice();
        });

//        layoutRatingAndReview.setOnClickListener(v -> {
//            Bundle bundle = new Bundle();
//            RatingReview ratingReview = new RatingReview(RatingReview.TYPE_RATING_REVIEW_PRODUCT,
//                    String.valueOf(mProduct.getId()));
//            bundle.putSerializable(Constant.RATING_REVIEW_OBJECT, ratingReview);
//            GlobalFunction.startActivity(ProductDetailActivity.this,
//                    RatingReviewActivity.class, bundle);
//        });
//
        tvAddOrder.setOnClickListener(view -> {
            if (!isProductInCart()) {
                ProductDatabase.getInstance(ProductDetailActivity.this).productDAO().insertProduct(mProduct);
            } else {
                ProductDatabase.getInstance(ProductDetailActivity.this).productDAO().updateProduct(mProduct);
            }
            GlobalFunction.startActivity(ProductDetailActivity.this, CartActivity.class);
            EventBus.getDefault().post(new DisplayCartEvent());
            finish();
        });
    }

    private void calculatorTotalPrice() {
        int count = Integer.parseInt(tvCount.getText().toString().trim());
        int priceOneProduct = mProduct.getRealPrice();
        int totalPrice = priceOneProduct * count;
        String strTotalPrice = totalPrice + Constant.CURRENCY;
        tvTotal.setText(strTotalPrice);

        mProduct.setCount(count);
        mProduct.setPriceOneProduct(priceOneProduct);
        mProduct.setTotalPrice(totalPrice);
    }

    private boolean isProductInCart() {
        List<Product> list = ProductDatabase.getInstance(this)
                .productDAO().checkProductInCart(mProduct.getId());
        return list != null && !list.isEmpty();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProductValueEventListener != null) {
            MyApplication.get(this).getProductDetailDatabaseReference(mProductId)
                    .removeEventListener(mProductValueEventListener);
        }
    }
}