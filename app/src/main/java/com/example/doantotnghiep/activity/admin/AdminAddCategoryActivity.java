package com.example.doantotnghiep.activity.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.BaseActivity;
import com.example.doantotnghiep.databinding.ActivityAdminAddCategoryBinding;
import com.example.doantotnghiep.model.Category;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class AdminAddCategoryActivity extends BaseActivity {

    ActivityAdminAddCategoryBinding binding;
    private TextView tvToolbarTitle;
    private EditText edtName;
    private Button btnAddOrEdit;

    private boolean isUpdate;
    private Category mCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadDataIntent();
        initUi();
        initView();
    }

    private void loadDataIntent() {
        Bundle bundleReceived = getIntent().getExtras();
        if (bundleReceived != null) {
            isUpdate = true;
            mCategory = (Category) bundleReceived.get(Constant.KEY_INTENT_CATEGORY_OBJECT);
        }
    }

    private void initUi() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        edtName = binding.edtName;
        btnAddOrEdit = binding.btnAddOrEdit;

        imgToolbarBack.setOnClickListener(view -> onBackPressed());
        btnAddOrEdit.setOnClickListener(v -> addOrEditCategory());
    }

    private void initView() {
        if (isUpdate) {
            tvToolbarTitle.setText(getString(R.string.label_update_category));
            btnAddOrEdit.setText(getString(R.string.action_edit));

            edtName.setText(mCategory.getName());
        } else {
            tvToolbarTitle.setText(getString(R.string.label_add_category));
            btnAddOrEdit.setText(getString(R.string.action_add));
        }
    }

    private void addOrEditCategory() {
        String strName = edtName.getText().toString().trim();

        if (StringUtil.isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_name_require), Toast.LENGTH_SHORT).show();
            return;
        }

        // Update category
        if (isUpdate) {
            showProgressDialog(true);
            Map<String, Object> map = new HashMap<>();
            map.put("name", strName);

            MyApplication.get(this).getCategoryDatabaseReference()
                    .child(String.valueOf(mCategory.getId())).updateChildren(map, (error, ref) -> {
                        showProgressDialog(false);
                        Toast.makeText(this,
                                getString(R.string.msg_edit_category_success), Toast.LENGTH_SHORT).show();
                        GlobalFunction.hideSoftKeyboard(this);
                    });
            return;
        }

        // Add category
        showProgressDialog(true);
        long categoryId = System.currentTimeMillis();
        Category category = new Category(categoryId, strName);
        MyApplication.get(this).getCategoryDatabaseReference()
                .child(String.valueOf(categoryId)).setValue(category, (error, ref) -> {
                    showProgressDialog(false);
                    edtName.setText("");
                    GlobalFunction.hideSoftKeyboard(this);
                    Toast.makeText(this, getString(R.string.msg_add_category_success), Toast.LENGTH_SHORT).show();
                });
    }
}