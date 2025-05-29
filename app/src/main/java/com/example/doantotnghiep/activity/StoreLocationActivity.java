package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ActivityStoreLocationBinding;
import com.example.doantotnghiep.model.StoreLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class StoreLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private ActivityStoreLocationBinding binding;
    private GoogleMap mMap;
    private StoreLocation storeLocation;
    private ValueEventListener mValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoreLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initMap();
        loadStoreLocationFromFirebase();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText(getString(R.string.store_location_title));
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void loadStoreLocationFromFirebase() {
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storeLocation = snapshot.getValue(StoreLocation.class);
                if (storeLocation != null && mMap != null) {
                    updateMapLocation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        };

        MyApplication.get(this).getStoreLocationDatabaseReference()
                .addValueEventListener(mValueEventListener);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        if (storeLocation != null) {
            updateMapLocation();
        } else {
            // Default location (Hanoi) if no location is set
            LatLng defaultLocation = new LatLng(21.0285, 105.8542);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));
        }
    }

    private void updateMapLocation() {
        if (mMap != null && storeLocation != null) {
            mMap.clear();
            LatLng storeLatLng = new LatLng(storeLocation.getLatitude(), storeLocation.getLongitude());

            mMap.addMarker(new MarkerOptions()
                    .position(storeLatLng)
                    .title("Cửa hàng Bobibi")
                    .snippet(storeLocation.getAddress()));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLatLng, 15));

            // Update address text
            binding.tvStoreAddress.setText(storeLocation.getAddress());
            binding.tvStorePhone.setText(storeLocation.getPhone());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mValueEventListener != null) {
            MyApplication.get(this).getStoreLocationDatabaseReference()
                    .removeEventListener(mValueEventListener);
        }
    }
}