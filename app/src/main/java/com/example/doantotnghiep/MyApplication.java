package com.example.doantotnghiep;

import android.app.Application;
import android.content.Context;

import com.example.doantotnghiep.prefs.DataStoreManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    private static final String FIREBASE_URL = "https://doantotnghiep-330ab-default-rtdb.firebaseio.com/";
    private FirebaseDatabase mFirebaseDatabase;

    public static MyApplication get(Context context) {
        return (MyApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_URL);
        DataStoreManager.init(getApplicationContext());
    }

    public DatabaseReference getAdminDatabaseReference() {
        return mFirebaseDatabase.getReference("admin");
    }

    public DatabaseReference getVoucherDatabaseReference() {
        return mFirebaseDatabase.getReference("voucher");
    }

    public DatabaseReference getAddressDatabaseReference() {
        return mFirebaseDatabase.getReference("address");
    }

    public DatabaseReference getCategoryDatabaseReference() {
        return mFirebaseDatabase.getReference("category");
    }

    public DatabaseReference getProductDatabaseReference() {
        return mFirebaseDatabase.getReference("product");
    }

    public DatabaseReference getProductDetailDatabaseReference(long productId) {
        return mFirebaseDatabase.getReference("product/" + productId);
    }

    public DatabaseReference getFeedbackDatabaseReference() {
        return mFirebaseDatabase.getReference("/feedback");
    }

    public DatabaseReference getOrderDatabaseReference() {
        return mFirebaseDatabase.getReference("order");
    }

    public DatabaseReference getRatingProductDatabaseReference(String productId) {
        return mFirebaseDatabase.getReference("/product/" + productId + "/rating");
    }

    public DatabaseReference getOrderDetailDatabaseReference(long orderId) {
        return mFirebaseDatabase.getReference("order/" + orderId);
    }
}
