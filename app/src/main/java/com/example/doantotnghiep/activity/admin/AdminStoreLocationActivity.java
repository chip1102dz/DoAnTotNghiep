package com.example.doantotnghiep.activity.admin;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.BaseActivity;
import com.example.doantotnghiep.databinding.ActivityAdminStoreLocationBinding;
import com.example.doantotnghiep.model.StoreLocation;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminStoreLocationActivity extends BaseActivity implements OnMapReadyCallback {

    private ActivityAdminStoreLocationBinding binding;
    private GoogleMap mMap;
    private AutoCompleteTextView edtAddress;
    private EditText edtPhone, edtLatitude, edtLongitude;
    private Button btnUpdateLocation;
    private StoreLocation currentStoreLocation;
    private Marker storeMarker;
    private ValueEventListener mValueEventListener;

    private Geocoder geocoder;
    private ExecutorService executorService;
    private Handler mainHandler;
    private ArrayAdapter<String> addressAdapter;
    private List<Address> suggestedAddresses;
    private boolean isUpdatingFromMap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminStoreLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initGeocoder();
        initToolbar();
        initUi();
        initMap();
        initListener();
        loadStoreLocationFromFirebase();
    }

    private void initGeocoder() {
        geocoder = new Geocoder(this, Locale.getDefault());
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        suggestedAddresses = new ArrayList<>();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText(getString(R.string.admin_store_location_title));
    }

    private void initUi() {
        edtAddress = binding.edtAddress;
        edtPhone = binding.edtPhone;
        edtLatitude = binding.edtLatitude;
        edtLongitude = binding.edtLongitude;
        btnUpdateLocation = binding.btnUpdateLocation;

        // Setup AutoCompleteTextView for address suggestions
        addressAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        edtAddress.setAdapter(addressAdapter);
        edtAddress.setThreshold(3); // Start suggesting after 3 characters
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initListener() {
        btnUpdateLocation.setOnClickListener(v -> updateStoreLocation());

        // Address text watcher for suggestions
        edtAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingFromMap && s.length() >= 3) {
                    searchAddressSuggestions(s.toString());
                }
                isUpdatingFromMap = false;
            }
        });

        // Address item selection
        edtAddress.setOnItemClickListener((parent, view, position, id) -> {
            if (position < suggestedAddresses.size()) {
                Address selectedAddress = suggestedAddresses.get(position);
                updateFromSelectedAddress(selectedAddress);
            }
        });

        // Coordinate text watchers
        edtLatitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateMapFromCoordinates();
            }
        });

        edtLongitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateMapFromCoordinates();
            }
        });
    }

    private void loadStoreLocationFromFirebase() {
        showProgressDialog(true);
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showProgressDialog(false);
                currentStoreLocation = snapshot.getValue(StoreLocation.class);
                if (currentStoreLocation != null) {
                    displayStoreLocationData();
                    if (mMap != null) {
                        updateMapLocation();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showProgressDialog(false);
                showToastMessage(getString(R.string.msg_get_date_error));
            }
        };

        MyApplication.get(this).getStoreLocationDatabaseReference()
                .addValueEventListener(mValueEventListener);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        // Set default location (Hanoi)
        LatLng defaultLocation = new LatLng(21.0285, 105.8542);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));

        // Set map click listener to update location
        mMap.setOnMapClickListener(latLng -> {
            updateMarkerPosition(latLng);
            edtLatitude.setText(String.valueOf(latLng.latitude));
            edtLongitude.setText(String.valueOf(latLng.longitude));

            // Get address from coordinates
            getAddressFromCoordinates(latLng);
        });

        if (currentStoreLocation != null) {
            updateMapLocation();
        }
    }

    private void displayStoreLocationData() {
        if (currentStoreLocation != null) {
            edtAddress.setText(currentStoreLocation.getAddress());
            edtPhone.setText(currentStoreLocation.getPhone());
            edtLatitude.setText(String.valueOf(currentStoreLocation.getLatitude()));
            edtLongitude.setText(String.valueOf(currentStoreLocation.getLongitude()));
        }
    }

    private void updateMapLocation() {
        if (mMap != null && currentStoreLocation != null) {
            LatLng storeLatLng = new LatLng(currentStoreLocation.getLatitude(), currentStoreLocation.getLongitude());
            updateMarkerPosition(storeLatLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLatLng, 15));
        }
    }

    private void updateMarkerPosition(LatLng latLng) {
        if (storeMarker != null) {
            storeMarker.remove();
        }

        storeMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Cửa hàng Bobibi")
                .snippet("Vị trí cửa hàng"));
    }

    private void updateStoreLocation() {
        String address = edtAddress.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String strLatitude = edtLatitude.getText().toString().trim();
        String strLongitude = edtLongitude.getText().toString().trim();

        if (StringUtil.isEmpty(address)) {
            showToastMessage(getString(R.string.msg_address_require));
            return;
        }

        if (StringUtil.isEmpty(phone)) {
            showToastMessage(getString(R.string.msg_phone_require));
            return;
        }

        if (StringUtil.isEmpty(strLatitude) || StringUtil.isEmpty(strLongitude)) {
            showToastMessage(getString(R.string.msg_location_require));
            return;
        }

        try {
            double latitude = Double.parseDouble(strLatitude);
            double longitude = Double.parseDouble(strLongitude);

            showProgressDialog(true);
            StoreLocation storeLocation = new StoreLocation(address, phone, latitude, longitude);

            MyApplication.get(this).getStoreLocationDatabaseReference()
                    .setValue(storeLocation, (error, ref) -> {
                        showProgressDialog(false);
                        if (error == null) {
                            Toast.makeText(this, getString(R.string.msg_update_location_success),
                                    Toast.LENGTH_SHORT).show();
                            GlobalFunction.hideSoftKeyboard(this);
                        } else {
                            showToastMessage(getString(R.string.msg_update_location_error));
                        }
                    });

        } catch (NumberFormatException e) {
            showToastMessage(getString(R.string.msg_invalid_coordinates));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mValueEventListener != null) {
            MyApplication.get(this).getStoreLocationDatabaseReference()
                    .removeEventListener(mValueEventListener);
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void searchAddressSuggestions(String query) {
        executorService.execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(query + ", Vietnam", 5);
                List<String> addressStrings = new ArrayList<>();
                suggestedAddresses.clear();

                for (Address address : addresses) {
                    String addressLine = address.getAddressLine(0);
                    if (addressLine != null) {
                        addressStrings.add(addressLine);
                        suggestedAddresses.add(address);
                    }
                }

                mainHandler.post(() -> {
                    addressAdapter.clear();
                    addressAdapter.addAll(addressStrings);
                    addressAdapter.notifyDataSetChanged();
                });

            } catch (IOException e) {
                e.printStackTrace();
                mainHandler.post(() -> showToastMessage("Lỗi khi tìm kiếm địa chỉ"));
            }
        });
    }

    private void updateFromSelectedAddress(Address address) {
        isUpdatingFromMap = true;
        edtLatitude.setText(String.valueOf(address.getLatitude()));
        edtLongitude.setText(String.valueOf(address.getLongitude()));

        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
        updateMarkerPosition(latLng);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    private void getAddressFromCoordinates(LatLng latLng) {
        executorService.execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressLine = address.getAddressLine(0);

                    mainHandler.post(() -> {
                        isUpdatingFromMap = true;
                        edtAddress.setText(addressLine);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                mainHandler.post(() -> showToastMessage("Không thể lấy địa chỉ từ tọa độ"));
            }
        });
    }

    private void updateMapFromCoordinates() {
        try {
            String strLatitude = edtLatitude.getText().toString().trim();
            String strLongitude = edtLongitude.getText().toString().trim();

            if (!StringUtil.isEmpty(strLatitude) && !StringUtil.isEmpty(strLongitude)) {
                double latitude = Double.parseDouble(strLatitude);
                double longitude = Double.parseDouble(strLongitude);

                // Validate coordinates
                if (latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180) {
                    LatLng latLng = new LatLng(latitude, longitude);
                    updateMarkerPosition(latLng);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            }
        } catch (NumberFormatException e) {
            // Ignore invalid number format
        }
    }
}