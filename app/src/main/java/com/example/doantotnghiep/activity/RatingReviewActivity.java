package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ActivityRatingReviewBinding;
import com.example.doantotnghiep.model.Rating;
import com.example.doantotnghiep.model.RatingReview;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;

import java.util.HashMap;
import java.util.Map;

public class RatingReviewActivity extends BaseActivity {
    ActivityRatingReviewBinding binding;
    private RatingBar ratingBar;
    private EditText edtReview;
    private TextView tvSendReview;

    private RatingReview ratingReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRatingReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getDataIntent();
        initToolbar();
        initUi();
        initListener();
    }

    private void getDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;
        ratingReview = (RatingReview) bundle.get(Constant.RATING_REVIEW_OBJECT);
    }

    private void initUi() {
        ratingBar = binding.ratingbar;
        ratingBar.setRating(5f);
        edtReview = binding.edtReview;
        tvSendReview = binding.tvSendReview;

        TextView tvMessageReview = findViewById(R.id.tv_message_review);
        if (RatingReview.TYPE_RATING_REVIEW_PRODUCT == ratingReview.getType()) {
            tvMessageReview.setText(getString(R.string.label_rating_review_product));
        } else if (RatingReview.TYPE_RATING_REVIEW_ORDER == ratingReview.getType()) {
            tvMessageReview.setText(getString(R.string.label_rating_review_order));
        }
    }

    private void initToolbar() {
        ImageView imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText(getString(R.string.ratings_and_reviews));
    }

    private void initListener() {
        tvSendReview.setOnClickListener(v -> {
            float rate = ratingBar.getRating();
            String review = edtReview.getText().toString().trim();
            Rating rating = new Rating(review, Double.parseDouble(String.valueOf(rate)));
            if (RatingReview.TYPE_RATING_REVIEW_PRODUCT == ratingReview.getType()) {
                sendRatingProduct(rating);
            } else if (RatingReview.TYPE_RATING_REVIEW_ORDER == ratingReview.getType()) {
                sendRatingOrder(rating);
            }
        });
    }

    private void sendRatingProduct(Rating rating) {
        MyApplication.get(this).getRatingProductDatabaseReference(ratingReview.getId())
                .child(String.valueOf(GlobalFunction.encodeEmailUser()))
                .setValue(rating, (error, ref) -> {
                    showToastMessage(getString(R.string.msg_send_review_success));
                    ratingBar.setRating(5f);
                    edtReview.setText("");
                    GlobalFunction.hideSoftKeyboard(RatingReviewActivity.this);
                });
    }

    private void sendRatingOrder(Rating rating) {
        Map<String, Object> map = new HashMap<>();
        map.put("rate", rating.getRate());
        map.put("review", rating.getReview());

        MyApplication.get(this).getOrderDatabaseReference()
                .child(String.valueOf(ratingReview.getId()))
                .updateChildren(map, (error, ref) -> {
                    showToastMessage(getString(R.string.msg_send_review_success));
                    ratingBar.setRating(5f);
                    edtReview.setText("");
                    GlobalFunction.hideSoftKeyboard(RatingReviewActivity.this);
                });
    }
}