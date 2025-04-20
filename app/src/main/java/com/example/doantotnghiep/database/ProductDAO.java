package com.example.doantotnghiep.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import com.example.doantotnghiep.model.Product;

import java.util.List;

@Dao
public interface ProductDAO {

    @Insert
    void insertProduct(Product product);

    @Query("SELECT * FROM product")
    List<Product> getListProductCart();

    @Query("SELECT * FROM product WHERE id=:id")
    List<Product> checkProductInCart(long id);

    @Delete
    void deleteProduct(Product product);

    @Update
    void updateProduct(Product product);

    @Query("DELETE from product")
    void deleteAllProduct();
}
