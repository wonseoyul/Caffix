package com.example.caffix;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView textViewEmail;
    private EditText editTextName;
    private Button buttonChangePassword, buttonUpdateProfile, buttonLogout;
    private ImageView imageViewProfilePicture;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");

        // Firebase 인증 및 Firestore, Storage 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // 뷰 초기화
        textViewEmail = findViewById(R.id.textViewEmail);
        editTextName = findViewById(R.id.editTextName);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonUpdateProfile = findViewById(R.id.buttonUpdateProfile);
        buttonLogout = findViewById(R.id.buttonLogout);
        imageViewProfilePicture = findViewById(R.id.imageViewProfilePicture);

        // 현재 사용자 가져오기
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // 사용자의 이메일 주소 설정
            String email = currentUser.getEmail();
            textViewEmail.setText(email);

            userId = currentUser.getUid();
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    editTextName.setText(name);

                    String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                    if (profileImageUrl != null) {
                        Picasso.get().load(profileImageUrl).into(imageViewProfilePicture);
                    } else {
                        imageViewProfilePicture.setImageResource(R.drawable.default_profile);
                    }
                }
            });

            // 프로필 사진 로드
            StorageReference profilePicRef = storage.getReference().child("profile_pictures/" + userId + ".jpg");
            profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Picasso를 사용하여 이미지 로드
                Picasso.get().load(uri).into(imageViewProfilePicture);
            }).addOnFailureListener(exception -> {
                // 에러 처리
                imageViewProfilePicture.setImageResource(R.drawable.default_profile);
            });
        }

        // 프로필 업데이트 버튼 클릭 리스너 설정
        buttonUpdateProfile.setOnClickListener(v -> updateProfile());

        // 비밀번호 변경 버튼 클릭 리스너 설정
        buttonChangePassword.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class)));

        // 로그아웃 버튼 클릭 리스너 설정
        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(ProfileActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });

        // 프로필 사진 클릭 리스너 설정
        imageViewProfilePicture.setOnClickListener(v -> openFileChooser());
    }

    private void openFileChooser() {
        // 파일 선택기 열기
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // Firebase Storage에 이미지 업로드
        StorageReference fileRef = storage.getReference().child("profile_pictures/" + userId + ".jpg");

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = fileRef.putBytes(data);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    saveImageUrlToFirestore(downloadUri.toString());
                } else {
                    Toast.makeText(ProfileActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        // Firestore에 이미지 URL 저장
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update("profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "프로필 이미지 업데이트 완료", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "프로필 이미지 업데이트 실패", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfile() {
        // Firestore에 사용자 정보 업데이트
        String name = editTextName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            editTextName.setError("이름을 입력하세요.");
            return;
        }

        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update("name", name)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "프로필 업데이트 완료", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "프로필 업데이트 실패", Toast.LENGTH_SHORT).show();
                });
    }
}
