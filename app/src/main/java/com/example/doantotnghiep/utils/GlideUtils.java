package com.example.doantotnghiep.utils;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.doantotnghiep.R;

public class GlideUtils {

    public static void loadUrlBanner(String url, ImageView imageView) {
        if (StringUtil.isEmpty(String.valueOf(url))) {
            imageView.setImageResource(R.drawable.no_image_available);
            return;
        }
        try {
            Glide.with(imageView.getContext())
                    .load(url)
                    .error(R.drawable.no_image_available)
                    .dontAnimate()
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadUrl(String url, ImageView imageView) {
        if (StringUtil.isEmpty(url)) {
            imageView.setImageResource(R.drawable.no_image_available);
            return;
        }
        try {
            Glide.with(imageView.getContext())
                    .load(url)
                    .error(R.drawable.no_image_available)
                    .dontAnimate()
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}