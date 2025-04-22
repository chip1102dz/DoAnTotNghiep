package com.example.doantotnghiep.activity;

import static com.example.doantotnghiep.utils.GlobalFunction.showToastMessage;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.CartAdapter;
import com.example.doantotnghiep.database.ProductDatabase;
import com.example.doantotnghiep.databinding.ActivityCartBinding;
import com.example.doantotnghiep.event.AddressSelectedEvent;
import com.example.doantotnghiep.event.DisplayCartEvent;
import com.example.doantotnghiep.event.OrderSuccessEvent;
import com.example.doantotnghiep.event.PaymentMethodSelectedEvent;
import com.example.doantotnghiep.event.VoucherSelectedEvent;
import com.example.doantotnghiep.model.Address;
import com.example.doantotnghiep.model.Order;
import com.example.doantotnghiep.model.PaymentMethod;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.model.ProductOrder;
import com.example.doantotnghiep.model.Voucher;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends BaseActivity {
    ActivityCartBinding binding;
    private RecyclerView rcvCart;
    private LinearLayout layoutAddOrder;
    private RelativeLayout layoutPaymentMethod;
    private TextView tvPaymentMethod;

    private RelativeLayout layoutAddress;
    private TextView tvAddress;
    private RelativeLayout layoutVoucher;
    private TextView tvVoucher;
    private TextView tvNameVoucher;
    private TextView tvPriceProduct;
    private TextView tvCountItem;
    private TextView tvAmount;
    private TextView tvPriceVoucher;
    private TextView tvCheckout;

    private List<Product> listProductCart;
    private CartAdapter cartAdapter;
    private int priceProduct;
    private int mAmount;
    private PaymentMethod paymentMethodSelected;
    private Address addressSelected;
    private Voucher voucherSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initUi();
        initData();
        initListener();
        initToolbar();
    }
    private void initToolbar() {
        ImageView imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText(getString(R.string.label_cart));
    }
    private void initUi() {
        rcvCart = binding.rcvCart;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvCart.setLayoutManager(linearLayoutManager);
        layoutAddOrder = binding.layoutAddOrder;
        layoutPaymentMethod = binding.layoutPaymentMethod;
        tvPaymentMethod = binding.tvPaymentMethod;
        layoutAddress = binding.layoutAddress;
        tvAddress = binding.tvAddress;
        layoutVoucher = binding.layoutVoucher;
        tvVoucher = binding.tvVoucher;
        tvNameVoucher = binding.tvNameVoucher;
        tvCountItem = binding.tvCountItem;
        tvPriceProduct = binding.tvPriceProduct;
        tvAmount = binding.tvAmount;
        tvPriceVoucher = binding.tvPriceVoucher;
        tvCheckout = binding.tvCheckout;
    }
    private void initListener() {
        layoutAddOrder.setOnClickListener(v -> finish());
        layoutPaymentMethod.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            if (paymentMethodSelected != null) {
                bundle.putInt(Constant.PAYMENT_METHOD_ID, paymentMethodSelected.getId());
            }
            GlobalFunction.startActivity(CartActivity.this, PaymentMethodActivity.class, bundle);
        });

        layoutAddress.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            if (addressSelected != null) {
                bundle.putLong(Constant.ADDRESS_ID, addressSelected.getId());
            }
            GlobalFunction.startActivity(CartActivity.this, AddressActivity.class, bundle);
        });

        layoutVoucher.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putInt(Constant.AMOUNT_VALUE, priceProduct);
            if (voucherSelected != null) {
                bundle.putLong(Constant.VOUCHER_ID, voucherSelected.getId());
            }
            GlobalFunction.startActivity(CartActivity.this, VoucherActivity.class, bundle);
        });

        tvCheckout.setOnClickListener(view -> {
            if (listProductCart == null || listProductCart.isEmpty()) return;
            if (paymentMethodSelected == null) {
                showToastMessage(getString(R.string.label_choose_payment_method));
                return;
            }
            if (addressSelected == null) {
                showToastMessage(getString(R.string.label_choose_address));
                return;
            }
            Order orderBooking = new Order();
            orderBooking.setId(System.currentTimeMillis());
            orderBooking.setUserEmail(DataStoreManager.getUser().getEmail());
            orderBooking.setDateTime(String.valueOf(System.currentTimeMillis()));
            List<ProductOrder> products = new ArrayList<>();
            for (Product product : listProductCart) {
                products.add(new ProductOrder(product.getId(), product.getName(),
                        product.getDescription(), product.getCount(),
                        product.getPriceOneProduct(), product.getImage()));
            }
            orderBooking.setProducts(products);
            orderBooking.setPrice(priceProduct);
            if (voucherSelected != null) {
                orderBooking.setVoucher(voucherSelected.getPriceDiscount(priceProduct));
            }
            orderBooking.setTotal(mAmount);
            orderBooking.setPaymentMethod(paymentMethodSelected.getName());
            orderBooking.setAddress(addressSelected);
            orderBooking.setStatus(Order.STATUS_NEW);

            Bundle bundle = new Bundle();
            bundle.putSerializable(Constant.ORDER_OBJECT, orderBooking);
            GlobalFunction.startActivity(CartActivity.this, PaymentActivity.class, bundle);
        });
    }
    private void initData() {
        listProductCart = new ArrayList<>();
        listProductCart = ProductDatabase.getInstance(this).productDAO().getListProductCart();
        if (listProductCart == null || listProductCart.isEmpty()) {
            return;
        }
        cartAdapter = new CartAdapter(listProductCart, new CartAdapter.IClickCartListener() {
            @Override
            public void onClickDeleteItem(Product product, int position) {
                ProductDatabase.getInstance(CartActivity.this).productDAO().deleteProduct(product);
                listProductCart.remove(position);
                cartAdapter.notifyItemRemoved(position);

                displayCountItemCart();
                calculateTotalPrice();
                EventBus.getDefault().post(new DisplayCartEvent());
            }

            @Override
            public void onClickUpdateItem(Product product, int position) {
                ProductDatabase.getInstance(CartActivity.this).productDAO().updateProduct(product);
                cartAdapter.notifyItemChanged(position);

                calculateTotalPrice();
                EventBus.getDefault().post(new DisplayCartEvent());
            }

            @Override
            public void onClickEditItem(Product product) {
                Bundle bundle = new Bundle();
                bundle.putLong(Constant.PRODUCT_ID, product.getId());
                bundle.putSerializable(Constant.PRODUCT_OBJECT, product);
                GlobalFunction.startActivity(CartActivity.this, ProductDetailActivity.class, bundle);
            }
        });
        rcvCart.setAdapter(cartAdapter);
        calculateTotalPrice();
        displayCountItemCart();
    }

    private void displayCountItemCart() {
        String strCountItem = "(" + listProductCart.size() + " " + getString(R.string.label_item) + ")";
        tvCountItem.setText(strCountItem);
    }

    private void calculateTotalPrice() {
        if (listProductCart == null || listProductCart.isEmpty()) {
            String strZero = 0 + Constant.CURRENCY;
            priceProduct = 0;
            tvPriceProduct.setText(strZero);

            mAmount = 0;
            tvAmount.setText(strZero);
            return;
        }

        int totalPrice = 0;
        for (Product product : listProductCart) {
            totalPrice = totalPrice + product.getTotalPrice();
        }

        priceProduct = totalPrice;
        String strPriceProduct = priceProduct + Constant.CURRENCY;
        tvPriceProduct.setText(strPriceProduct);

        mAmount = totalPrice;
        if (voucherSelected != null) {
            String strPriceVoucher = "-" + voucherSelected.getPriceDiscount(priceProduct)
                    + Constant.CURRENCY;
            tvPriceVoucher.setText(strPriceVoucher);

            mAmount = mAmount - voucherSelected.getPriceDiscount(priceProduct);
        }
        String strAmount = mAmount + Constant.CURRENCY;
        tvAmount.setText(strAmount);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPaymentMethodSelectedEvent(PaymentMethodSelectedEvent event) {
        if (event.getPaymentMethod() != null) {
            paymentMethodSelected = event.getPaymentMethod();
            tvPaymentMethod.setText(paymentMethodSelected.getName());
        } else {
            tvPaymentMethod.setText(getString(R.string.label_no_payment_method));
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onAddressSelectedEvent(AddressSelectedEvent event) {
//        if (event.getAddress() != null) {
//            addressSelected = event.getAddress();
//            tvAddress.setText(addressSelected.getAddress());
//        } else {
//            tvAddress.setText(getString(R.string.label_no_address));
//        }
//    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onVoucherSelectedEvent(VoucherSelectedEvent event) {
//        if (event.getVoucher() != null) {
//            voucherSelected = event.getVoucher();
//            tvVoucher.setText(voucherSelected.getTitle());
//            tvNameVoucher.setText(voucherSelected.getTitle());
//            String strPriceVoucher = "-" + voucherSelected.getPriceDiscount(priceProduct)
//                    + Constant.CURRENCY;
//            tvPriceVoucher.setText(strPriceVoucher);
//        } else {
//            tvVoucher.setText(getString(R.string.label_no_voucher));
//            tvNameVoucher.setText(getString(R.string.label_no_voucher));
//            String strPriceVoucher = "-0" + Constant.CURRENCY;
//            tvPriceVoucher.setText(strPriceVoucher);
//        }
//        calculateTotalPrice();
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderSuccessEvent(OrderSuccessEvent event) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}