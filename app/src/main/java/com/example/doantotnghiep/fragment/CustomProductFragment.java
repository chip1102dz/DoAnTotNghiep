package com.example.doantotnghiep.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.activity.ProductDetailActivity;
import com.example.doantotnghiep.adapter.ProductAdapter;
import com.example.doantotnghiep.databinding.FragmentCustomProductBinding;
import com.example.doantotnghiep.listener.IClickProductListener;
import com.example.doantotnghiep.model.Order;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.model.ProductOrder;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomProductFragment extends Fragment {

    private static final String ARG_TAB_TYPE = "tab_type";

    // Tab types
    public static final int TAB_BEST_SELLER = 0;
    public static final int TAB_NEWEST = 1;
    public static final int TAB_DISCOUNT = 2;

    private FragmentCustomProductBinding binding;
    private RecyclerView rcvProduct;
    private List<Product> listProduct;
    private ProductAdapter productAdapter;
    private int tabType;
    private ValueEventListener mValueEventListener;

    public static CustomProductFragment newInstance(int tabType) {
        CustomProductFragment fragment = new CustomProductFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_TYPE, tabType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCustomProductBinding.inflate(inflater, container, false);

        getDataArguments();
        initUi();
        loadDataFromFirebase();

        return binding.getRoot();
    }

    private void getDataArguments() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            tabType = bundle.getInt(ARG_TAB_TYPE, TAB_BEST_SELLER);
        }
    }

    private void initUi() {
        rcvProduct = binding.rcvProduct;
        listProduct = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rcvProduct.setLayoutManager(linearLayoutManager);

        productAdapter = new ProductAdapter(listProduct, new IClickProductListener() {
            @Override
            public void onClickProductItem(Product product) {
                Bundle bundle = new Bundle();
                bundle.putLong(Constant.PRODUCT_ID, product.getId());
                GlobalFunction.startActivity(getActivity(), ProductDetailActivity.class, bundle);
            }
        });

        rcvProduct.setAdapter(productAdapter);
    }

    private void loadDataFromFirebase() {
        if (getActivity() == null) return;

        if (tabType == TAB_BEST_SELLER) {
            // Đối với tab bán chạy, cần lấy dữ liệu từ orders
            loadBestSellerData();
        } else {
            // Các tab khác vẫn lấy từ products
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Product> allProducts = new ArrayList<>();

                    // Lấy tất cả sản phẩm từ Firebase
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Product product = dataSnapshot.getValue(Product.class);
                        if (product != null) {
                            allProducts.add(product);
                        }
                    }

                    // Lọc và sắp xếp theo từng tab
                    filterAndSortProducts(allProducts);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Lỗi khi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };

            MyApplication.get(getActivity()).getProductDatabaseReference()
                    .addValueEventListener(mValueEventListener);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterAndSortProducts(List<Product> allProducts) {
        listProduct.clear();

        switch (tabType) {
            case TAB_BEST_SELLER:
                // Tab bán chạy sẽ được xử lý riêng trong loadBestSellerData()
                break;

            case TAB_NEWEST:
                // Lọc 5 sản phẩm mới nhất (dựa theo ID cao nhất - giả sử ID cao hơn = mới hơn)
                Collections.sort(allProducts, (p1, p2) -> Long.compare(p2.getId(), p1.getId()));
                for (int i = 0; i < Math.min(5, allProducts.size()); i++) {
                    listProduct.add(allProducts.get(i));
                }
                break;

            case TAB_DISCOUNT:
                // Lọc sản phẩm có giảm giá, sắp xếp theo % giảm giá cao nhất
                List<Product> discountProducts = new ArrayList<>();
                for (Product product : allProducts) {
                    if (product.getSale() > 0) {
                        discountProducts.add(product);
                    }
                }

                // Sắp xếp theo % giảm giá giảm dần
                Collections.sort(discountProducts, (p1, p2) -> Integer.compare(p2.getSale(), p1.getSale()));

                // Lấy top 5 sản phẩm giảm giá nhiều nhất
                for (int i = 0; i < Math.min(5, discountProducts.size()); i++) {
                    listProduct.add(discountProducts.get(i));
                }
                break;
        }

        // Cập nhật adapter
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }
    }

    private void loadBestSellerData() {
        if (getActivity() == null) return;

        // Lấy dữ liệu từ orders để tính sản phẩm bán chạy
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Danh sách để tính tổng số lượng bán của từng sản phẩm
                List<ProductOrder> mListProductOrder = new ArrayList<>();

                // Duyệt qua tất cả orders đã hoàn thành
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order != null && order.getStatus() == Order.STATUS_COMPLETE) {
                        // Lấy danh sách sản phẩm trong order
                        List<ProductOrder> productsInOrder = order.getProducts();
                        if (productsInOrder != null) {
                            for (ProductOrder productOrder : productsInOrder) {
                                // Kiểm tra xem sản phẩm đã tồn tại trong danh sách chưa
                                boolean found = false;
                                for (ProductOrder existingProduct : mListProductOrder) {
                                    if (existingProduct.getId() == productOrder.getId()) {
                                        // Cộng dồn số lượng
                                        existingProduct.setCount(existingProduct.getCount() + productOrder.getCount());
                                        found = true;
                                        break;
                                    }
                                }

                                // Nếu chưa tồn tại, thêm mới
                                if (!found) {
                                    ProductOrder newProductOrder = new ProductOrder(
                                            productOrder.getId(),
                                            productOrder.getName(),
                                            productOrder.getDescription(),
                                            productOrder.getCount(),
                                            productOrder.getPrice(),
                                            productOrder.getImage()
                                    );
                                    mListProductOrder.add(newProductOrder);
                                }
                            }
                        }
                    }
                }

                // Sắp xếp theo số lượng bán từ cao xuống thấp (như code bạn gửi)
                List<ProductOrder> listProductOrderDisplay = new ArrayList<>(mListProductOrder);
                Collections.sort(listProductOrderDisplay, (productOrder1, productOrder2)
                        -> productOrder2.getCount() - productOrder1.getCount());

                // Chuyển đổi từ ProductOrder sang Product và lấy top 5
                convertToProductList(listProductOrderDisplay);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi tải dữ liệu bán chạy: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        // Lấy dữ liệu từ orders
        MyApplication.get(getActivity()).getOrderDatabaseReference()
                .addValueEventListener(mValueEventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void convertToProductList(List<ProductOrder> sortedProductOrders) {
        if (getActivity() == null) return;

        // Lấy thông tin chi tiết sản phẩm từ Firebase
        ValueEventListener productListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listProduct.clear();

                // Lấy top 5 sản phẩm bán chạy nhất
                int count = 0;
                for (ProductOrder productOrder : sortedProductOrders) {
                    if (count >= 5) break;

                    // Tìm sản phẩm tương ứng trong Firebase
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Product product = dataSnapshot.getValue(Product.class);
                        if (product != null && product.getId() == productOrder.getId()) {
                            listProduct.add(product);
                            count++;
                            break;
                        }
                    }
                }

                // Cập nhật adapter
                if (productAdapter != null) {
                    productAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi lấy thông tin sản phẩm: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        MyApplication.get(getActivity()).getProductDatabaseReference()
                .addListenerForSingleValueEvent(productListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Remove listener để tránh memory leak
        if (getActivity() != null && mValueEventListener != null) {
            MyApplication.get(getActivity()).getProductDatabaseReference()
                    .removeEventListener(mValueEventListener);
        }

        binding = null;
    }
}