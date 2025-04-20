package com.example.doantotnghiep.listener;


import com.example.doantotnghiep.model.Category;

public interface IOnAdminManagerCategoryListener {
    void onClickUpdateCategory(Category category);
    void onClickDeleteCategory(Category category);
    void onClickItemCategory(Category category);
}
