package com.example.doantotnghiep.listener;


import com.example.doantotnghiep.model.Product;

public interface IOnAdminManagerProductListener {
    void onClickUpdateProduct(Product product);
    void onClickDeleteProduct(Product product);
}
