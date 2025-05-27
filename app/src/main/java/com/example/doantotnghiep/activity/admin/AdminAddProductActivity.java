package com.example.doantotnghiep.activity.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.BaseActivity;
import com.example.doantotnghiep.adapter.admin.AdminSelectAdapter;
import com.example.doantotnghiep.databinding.ActivityAdminAddProductBinding;
import com.example.doantotnghiep.model.Category;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAddProductActivity extends BaseActivity {
    ActivityAdminAddProductBinding binding;
    private TextView tvToolbarTitle;
    private EditText edtName, edtDescription, edtPrice, edtPromotion, edtImage, edtImageBanner, edtInfo;
    private CheckBox chbFeatured;
    private Spinner spnCategory;
    private Button btnAddOrEdit;

    private boolean isUpdate;
    private Product mProduct;
    private Category mCategorySelected;
    private ValueEventListener mValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadDataIntent();
        initUi();
        initData();
    }

    private void loadDataIntent() {
        Bundle bundleReceived = getIntent().getExtras();
        if (bundleReceived != null) {
            isUpdate = true;
            mProduct = (Product) bundleReceived.get(Constant.KEY_INTENT_PRODUCT_OBJECT);
        }
    }

    private void initUi() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        edtName = binding.edtName;
        edtDescription = binding.edtDescription;
        edtInfo = binding.edtInfo;
        edtPrice = binding.edtPrice;
        edtPromotion = binding.edtPromotion;
        edtImage = binding.edtImage;
        edtImageBanner = binding.edtImageBanner;
        chbFeatured = binding.chbFeatured;
        btnAddOrEdit = binding.btnAddOrEdit;
        spnCategory = binding.spnCategory;
        imgToolbarBack.setOnClickListener(view -> onBackPressed());
        btnAddOrEdit.setOnClickListener(v -> addOrEditProduct());
    }

    private void initData() {
        if (isUpdate) {
            tvToolbarTitle.setText(getString(R.string.label_update_product));
            btnAddOrEdit.setText(getString(R.string.action_edit));

            edtName.setText(mProduct.getName());
            edtDescription.setText(mProduct.getDescription());
            edtInfo.setText(mProduct.getInfo());
            edtPrice.setText(String.valueOf(mProduct.getPrice()));
            edtPromotion.setText(String.valueOf(mProduct.getSale()));
            edtImage.setText(mProduct.getImage());
            edtImageBanner.setText(mProduct.getBanner());
            chbFeatured.setChecked(mProduct.isFeatured());
        } else {
            tvToolbarTitle.setText(getString(R.string.label_add_product));
            btnAddOrEdit.setText(getString(R.string.action_add));
        }
        loadListCategory();
    }

    private void loadListCategory() {
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Category> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Category category = dataSnapshot.getValue(Category.class);
                    if (category == null) return;
                    list.add(0, category);
                }
                AdminSelectAdapter adapter = new AdminSelectAdapter(AdminAddProductActivity.this,
                        R.layout.item_choose_option, list);
                spnCategory.setAdapter(adapter);
                spnCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        mCategorySelected = adapter.getItem(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

                if (mProduct != null && mProduct.getCategory_id() > 0) {
                    spnCategory.setSelection(getPositionSelected(list, mProduct.getCategory_id()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        MyApplication.get(this).getCategoryDatabaseReference()
                .addValueEventListener(mValueEventListener);
    }

    private int getPositionSelected(List<Category> list, long id) {
        int position = 0;
        for (int i = 0; i < list.size(); i++) {
            if (id == list.get(i).getId()) {
                position = i;
                break;
            }
        }
        return position;
    }

    private void addOrEditProduct() {
        String strName = edtName.getText().toString().trim();
        String strDescription = edtDescription.getText().toString().trim();
        String strInfo = edtInfo.getText().toString().trim();
        String strPrice = edtPrice.getText().toString().trim();
        String strPromotion = edtPromotion.getText().toString().trim();
        String strImage = edtImage.getText().toString().trim();
        String strImageBanner = edtImageBanner.getText().toString().trim();

        if (StringUtil.isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_name_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strDescription)) {
            Toast.makeText(this, getString(R.string.msg_description_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strPrice)) {
            Toast.makeText(this, getString(R.string.msg_price_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strImage)) {
            Toast.makeText(this, getString(R.string.msg_image_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strImageBanner)) {
            Toast.makeText(this, getString(R.string.msg_image_banner_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strInfo)) {
            Toast.makeText(this, getString(R.string.msg_product_info), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strPromotion)) {
            strPromotion = "0";
        }

        // Update product
        if (isUpdate) {
            showProgressDialog(true);
            Map<String, Object> map = new HashMap<>();
            map.put("name", strName);
            map.put("description", strDescription);
            map.put("info", strInfo);
            map.put("price", Integer.parseInt(strPrice));
            map.put("sale", Integer.parseInt(strPromotion));
            map.put("image", strImage);
            map.put("banner", strImageBanner);
            map.put("featured", chbFeatured.isChecked());
            map.put("category_id", mCategorySelected.getId());
            map.put("category_name", mCategorySelected.getName());

            MyApplication.get(this).getProductDatabaseReference()
                    .child(String.valueOf(mProduct.getId())).updateChildren(map, (error, ref) -> {
                        showProgressDialog(false);
                        Toast.makeText(this,
                                getString(R.string.msg_edit_product_success), Toast.LENGTH_SHORT).show();
                        GlobalFunction.hideSoftKeyboard(this);
                    });
            return;
        }

        // Add product
        showProgressDialog(true);
        long productId = System.currentTimeMillis();
        Product product = new Product();
        product.setId(productId);
        product.setName(strName);
        product.setDescription(strDescription);
        product.setInfo(strInfo);
        product.setPrice(Integer.parseInt(strPrice));
        product.setSale(Integer.parseInt(strPromotion));
        product.setImage(strImage);
        product.setBanner(strImageBanner);
        product.setFeatured(chbFeatured.isChecked());

        product.setCategory_id(mCategorySelected.getId());
        product.setCategory_name(mCategorySelected.getName());

        MyApplication.get(this).getProductDatabaseReference()
                .child(String.valueOf(productId)).setValue(product, (error, ref) -> {
                    showProgressDialog(false);
                    edtName.setText("");
                    edtDescription.setText("");
                    edtInfo.setText("");
                    edtPrice.setText("");
                    edtPromotion.setText("0");
                    edtImage.setText("");
                    edtImageBanner.setText("");
                    chbFeatured.setChecked(false);
                    spnCategory.setSelection(0);
                    GlobalFunction.hideSoftKeyboard(this);
                    Toast.makeText(this, getString(R.string.msg_add_product_success), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mValueEventListener != null) {
            MyApplication.get(this).getCategoryDatabaseReference()
                    .removeEventListener(mValueEventListener);
        }
    }
}