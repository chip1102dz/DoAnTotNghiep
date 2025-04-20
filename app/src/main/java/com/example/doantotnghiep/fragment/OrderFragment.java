package com.example.doantotnghiep.fragment;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.OrderAdapter;

import com.example.doantotnghiep.databinding.FragmentOderBinding;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderFragment extends Fragment {

    FragmentOderBinding binding;
    RecyclerView rcv;

    OrderAdapter orderAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOderBinding.inflate(inflater, container, false);

        return binding.getRoot();

    }
}