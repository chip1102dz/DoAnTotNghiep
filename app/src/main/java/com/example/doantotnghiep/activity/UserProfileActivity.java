package com.example.doantotnghiep.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ActivityUserProfileBinding;
import com.example.doantotnghiep.model.User;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.GlideUtils;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends BaseActivity {

    private ActivityUserProfileBinding binding;

    private CircleImageView imgAvatar;
    private EditText edtFullName;
    private EditText edtPhone;
    private EditText edtAddress;
    private EditText edtDateOfBirth;
    private RadioGroup radioGroupGender;
    private Button btnSave;
    private Button btnCancel;
    private TextView tvChangeAvatar;

    private User currentUser;
    private Uri selectedImageUri;
    private boolean isImageChanged = false;

    private static final int PERMISSION_REQUEST_CODE = 1000;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initUi();
        initListener();
        loadUserData();
        setupImagePicker();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText("Thông tin cá nhân");
    }

    private void initUi() {
        imgAvatar = binding.imgAvatar;
        edtFullName = binding.edtFullName;
        edtPhone = binding.edtPhone;
        edtAddress = binding.edtAddress;
        edtDateOfBirth = binding.edtDateOfBirth;
        radioGroupGender = binding.radioGroupGender;
        btnSave = binding.btnSave;
        btnCancel = binding.btnCancel;
        tvChangeAvatar = binding.tvChangeAvatar;
    }

    private void initListener() {
        btnSave.setOnClickListener(v -> saveUserProfile());
        btnCancel.setOnClickListener(v -> finish());
        tvChangeAvatar.setOnClickListener(v -> openImagePicker());

        edtDateOfBirth.setOnClickListener(v -> {
            GlobalFunction.showDatePicker(this, edtDateOfBirth.getText().toString(),
                    date -> edtDateOfBirth.setText(date));
        });
    }

    private void loadUserData() {
        currentUser = DataStoreManager.getUser();
        if (currentUser == null) return;

        // Load avatar
        if (!StringUtil.isEmpty(currentUser.getProfileImageUrl())) {
            GlideUtils.loadUrl(currentUser.getProfileImageUrl(), imgAvatar);
        }

        // Load basic info
        if (!StringUtil.isEmpty(currentUser.getFullName())) {
            edtFullName.setText(currentUser.getFullName());
        }

        if (!StringUtil.isEmpty(currentUser.getPhoneNumber())) {
            edtPhone.setText(currentUser.getPhoneNumber());
        }

        if (!StringUtil.isEmpty(currentUser.getAddress())) {
            edtAddress.setText(currentUser.getAddress());
        }

        if (!StringUtil.isEmpty(currentUser.getDateOfBirth())) {
            edtDateOfBirth.setText(currentUser.getDateOfBirth());
        }

        // Load gender
        if (!StringUtil.isEmpty(currentUser.getGender())) {
            if ("Nam".equals(currentUser.getGender())) {
                ((RadioButton) binding.radioMale).setChecked(true);
            } else if ("Nữ".equals(currentUser.getGender())) {
                ((RadioButton) binding.radioFemale).setChecked(true);
            } else {
                ((RadioButton) binding.radioOther).setChecked(true);
            }
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            imgAvatar.setImageURI(selectedImageUri);
                            isImageChanged = true;
                        }
                    }
                }
        );
    }

    private void openImagePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveUserProfile() {
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String dateOfBirth = edtDateOfBirth.getText().toString().trim();

        String gender = "";
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedGenderId == R.id.radio_male) {
            gender = "Nam";
        } else if (selectedGenderId == R.id.radio_female) {
            gender = "Nữ";
        } else if (selectedGenderId == R.id.radio_other) {
            gender = "Khác";
        }

        // Validation
        if (StringUtil.isEmpty(fullName)) {
            showToastMessage("Vui lòng nhập họ tên");
            edtFullName.requestFocus();
            return;
        }

        if (!StringUtil.isEmpty(phone) && !isValidPhoneNumber(phone)) {
            showToastMessage("Số điện thoại không hợp lệ");
            edtPhone.requestFocus();
            return;
        }

        showProgressDialog(true);

        if (isImageChanged && selectedImageUri != null) {
            uploadImageAndSaveProfile(fullName, phone, address, dateOfBirth, gender);
        } else {
            saveProfileToFirebase(fullName, phone, address, dateOfBirth, gender, currentUser.getProfileImageUrl());
        }
    }

    private void uploadImageAndSaveProfile(String fullName, String phone, String address,
                                           String dateOfBirth, String gender) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = "avatars/" + currentUser.getEmail().replace(".", "_") + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference avatarRef = storageRef.child(fileName);

        avatarRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveProfileToFirebase(fullName, phone, address, dateOfBirth, gender, imageUrl);
                    }).addOnFailureListener(e -> {
                        showProgressDialog(false);
                        showToastMessage("Lỗi khi tải ảnh lên: " + e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    showProgressDialog(false);
                    showToastMessage("Lỗi khi tải ảnh lên: " + e.getMessage());
                });
    }

    private void saveProfileToFirebase(String fullName, String phone, String address,
                                       String dateOfBirth, String gender, String imageUrl) {

        // Update user object
        currentUser.setFullName(fullName);
        currentUser.setPhoneNumber(phone);
        currentUser.setAddress(address);
        currentUser.setDateOfBirth(dateOfBirth);
        currentUser.setGender(gender);
        if (!StringUtil.isEmpty(imageUrl)) {
            currentUser.setProfileImageUrl(imageUrl);
        }

        // Save to SharedPreferences
        DataStoreManager.setUser(currentUser);

        // Save to Firebase
        String userKey = String.valueOf(GlobalFunction.encodeEmailUser());
        DatabaseReference userRef = MyApplication.get(this).getAdminDatabaseReference()
                .getParent().child("users").child(userKey);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", currentUser.getEmail());
        userMap.put("fullName", fullName);
        userMap.put("phoneNumber", phone);
        userMap.put("address", address);
        userMap.put("dateOfBirth", dateOfBirth);
        userMap.put("gender", gender);
        userMap.put("balance", currentUser.getBalance());
        if (!StringUtil.isEmpty(imageUrl)) {
            userMap.put("profileImageUrl", imageUrl);
        }

        userRef.setValue(userMap)
                .addOnSuccessListener(aVoid -> {
                    showProgressDialog(false);
                    showToastMessage("Cập nhật thông tin thành công!");
                    finish();
                })
                .addOnFailureListener(e -> {
                    showProgressDialog(false);
                    showToastMessage("Lỗi khi cập nhật: " + e.getMessage());
                });
    }

    private boolean isValidPhoneNumber(String phone) {
        // Basic phone number validation for Vietnam
        return phone.matches("^(\\+84|0)[3|5|7|8|9][0-9]{8}$");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                showToastMessage("Cần cấp quyền để chọn ảnh");
            }
        }
    }
}