package com.example.caffix;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DatabaseReference mDatabase;
    private Spinner cafeSpinner;
    private Spinner categorySpinner;
    private Spinner menuSpinner;
    private Spinner sizeSpinner;
    private Spinner optionSpinner;
    private TextView caffeineContent;
    private TextView dailyCaffeineIntake;
    private AppCompatImageButton infoButton;
    private AppCompatImageButton profileButton;
    private int totalCaffeine = 0;
    private Calendar lastResetCalendar;
    private EditText caffeineInput;
    private Button addButton;
    private TextView caffeineRecord;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Handler handler = new Handler();
    private Runnable dailyResetRunnable;
    private FirebaseFirestore db;
    private static final String COLLECTION_USERS = "users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 데이터베이스 참조 초기화
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getSharedPreferences("CaffixPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        db = FirebaseFirestore.getInstance();

        // UI 요소 초기화
        cafeSpinner = findViewById(R.id.cafeSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        menuSpinner = findViewById(R.id.menuSpinner);
        sizeSpinner = findViewById(R.id.sizeSpinner);
        optionSpinner = findViewById(R.id.optionSpinner);
        caffeineContent = findViewById(R.id.caffeineContent);
        infoButton = findViewById(R.id.infoButton);
        profileButton = findViewById(R.id.profileButton);
        caffeineInput = findViewById(R.id.caffeineInput);
        addButton = findViewById(R.id.addButton);
        caffeineRecord = findViewById(R.id.caffeineRecord);

        // Firestore에서 totalCaffeine 불러오기
        loadTotalCaffeineFromFirestore();

        // 카페인 초기화에 대한 공유 환경 설정 초기화
        SharedPreferences sharedPreferences = getSharedPreferences("CaffeineSettings", MODE_PRIVATE);
        long lastResetMillis = sharedPreferences.getLong("lastResetMillis", 0);
        lastResetCalendar = Calendar.getInstance();
        lastResetCalendar.setTimeInMillis(lastResetMillis);
        Calendar currentCalendar = Calendar.getInstance();
        if (currentCalendar.get(Calendar.DAY_OF_YEAR) != lastResetCalendar.get(Calendar.DAY_OF_YEAR)) {
            // 하루가 지났을 경우 카페인 초기화
            totalCaffeine = 0;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("lastResetMillis", currentCalendar.getTimeInMillis());
            editor.apply();
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String caffeineStr = caffeineInput.getText().toString();
                if (!caffeineStr.isEmpty()) {
                    int caffeineAmount = Integer.parseInt(caffeineStr);
                    totalCaffeine += caffeineAmount;
                    updateCaffeineRecord();
                    showCaffeineDialog(caffeineAmount);
                }
            }
        });

        updateCafeSpinner();

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InformationActivity.class);
                startActivity(intent);
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadTotalCaffeineFromFirestore() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            DocumentReference userRef = db.collection(COLLECTION_USERS).document(userId);
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Long caffeineAmount = document.getLong("totalCaffeine");
                            if (caffeineAmount != null) {
                                totalCaffeine = caffeineAmount.intValue();
                                updateCaffeineRecord();
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    private void updateCaffeineRecord() {
        caffeineRecord.setText("총 카페인 섭취량: " + totalCaffeine + "mg");
        caffeineRecord.requestLayout();
        updateTotalCaffeineInFirestore(totalCaffeine); // Firestore에 totalCaffeine 업데이트
    }

    private void showCaffeineDialog(int caffeineAmount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("카페인 섭취 정보");

        if (caffeineAmount < 250) {
            builder.setMessage("적당한 양의 카페인을 섭취했습니다.");
        } else if (caffeineAmount < 300) {
            builder.setMessage("많은 양의 카페인을 섭취했습니다. 주의하세요!");
        } else {
            builder.setMessage("매우 많은 양의 카페인을 섭취했습니다. 주의가 필요합니다!");
        }

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void updateCafeSpinner() {
        DatabaseReference cafeRef = mDatabase;

        cafeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> cafeList = new ArrayList<>();

                for (DataSnapshot cafeSnapshot : dataSnapshot.getChildren()) {
                    String cafe = cafeSnapshot.getKey();
                    cafeList.add(cafe);
                }

                ArrayAdapter<String> cafeAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, cafeList);
                cafeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                cafeSpinner.setAdapter(cafeAdapter);

                cafeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedCafe = cafeList.get(position);
                        updateCategorySpinner(selectedCafe);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void updateCategorySpinner(String selectedCafe) {
        DatabaseReference cafeRef = mDatabase.child(selectedCafe);

        cafeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> categoryList = new ArrayList<>();

                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    String category = categorySnapshot.getKey();
                    categoryList.add(category);
                }

                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, categoryList);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(categoryAdapter);

                categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedCategory = categoryList.get(position);
                        updateMenuSpinner(selectedCafe, selectedCategory);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void updateMenuSpinner(String selectedCafe, String selectedCategory) {
        DatabaseReference menuRef = mDatabase.child(selectedCafe).child(selectedCategory);

        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> menuList = new ArrayList<>();

                for (DataSnapshot menuSnapshot : dataSnapshot.getChildren()) {
                    String menu = menuSnapshot.getKey();
                    menuList.add(menu);
                }

                ArrayAdapter<String> menuAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, menuList);
                menuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                menuSpinner.setAdapter(menuAdapter);

                menuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedMenu = menuList.get(position);
                        updateSizeSpinner(selectedCafe, selectedCategory, selectedMenu);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void updateSizeSpinner(String selectedCafe, String selectedCategory, String selectedMenu) {
        DatabaseReference sizeRef = mDatabase.child(selectedCafe).child(selectedCategory).child(selectedMenu);

        sizeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> sizeList = new ArrayList<>();

                for (DataSnapshot sizeSnapshot : dataSnapshot.getChildren()) {
                    String size = sizeSnapshot.getKey();
                    sizeList.add(size);
                }

                ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, sizeList);
                sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sizeSpinner.setAdapter(sizeAdapter);

                sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedSize = sizeList.get(position);
                        updateOptionSpinner(selectedCafe, selectedCategory, selectedMenu, selectedSize);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void updateOptionSpinner(String selectedCafe, String selectedCategory, String selectedMenu, String selectedSize) {
        DatabaseReference optionRef = mDatabase.child(selectedCafe).child(selectedCategory).child(selectedMenu).child(selectedSize);

        optionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> optionList = new ArrayList<>();

                for (DataSnapshot optionSnapshot : dataSnapshot.getChildren()) {
                    String option = optionSnapshot.getKey();
                    optionList.add(option);
                }

                ArrayAdapter<String> optionAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, optionList);
                optionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                optionSpinner.setAdapter(optionAdapter);

                optionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedOption = optionList.get(position);
                        DatabaseReference caffeineRef = optionRef.child(selectedOption).child("카페인함량");

                        caffeineRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Integer caffeine = dataSnapshot.getValue(Integer.class);
                                if (caffeine != null) {
                                    caffeineContent.setText("카페인 함량: " + caffeine + "mg");
                                } else {
                                    caffeineContent.setText("카페인 함량: 정보 없음");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(TAG, "Failed to read value.", databaseError.toException());
                            }
                        });
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void updateTotalCaffeineInFirestore(int totalCaffeine) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            DocumentReference userRef = db.collection(COLLECTION_USERS).document(userId);
            userRef.update("totalCaffeine", totalCaffeine)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Total caffeine successfully updated!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating total caffeine", e);
                        }
                    });
        }
    }
}
