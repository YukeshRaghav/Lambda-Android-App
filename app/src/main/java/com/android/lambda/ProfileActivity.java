package com.android.lambda;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private EditText nameEditText, departmentEditText, yearEditText;
    private Button updateButton;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImageView = findViewById(R.id.profileImageView);
        nameEditText = findViewById(R.id.nameEditText);
        departmentEditText = findViewById(R.id.departmentEditText);
        yearEditText = findViewById(R.id.yearEditText);
        updateButton = findViewById(R.id.updateButton);

        mAuth = FirebaseAuth.getInstance();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");

        loadUserProfile();

        updateButton.setOnClickListener(v -> updateUserProfile());
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = userDatabaseReference.child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                    String name = userSnapshot.child("name").getValue(String.class);
                    String year = userSnapshot.child("year").getValue(String.class);
                    String department = userSnapshot.child("department").getValue(String.class);
                    String profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String.class);

                    if (name != null) {
                        nameEditText.setText(name);
                    }
                    if (year != null) {
                        yearEditText.setText(year);
                    }
                    if (department != null) {
                        departmentEditText.setText(department);
                    }
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(ProfileActivity.this).load(profileImageUrl).into(profileImageView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateUserProfile() {
        String name = nameEditText.getText().toString().trim();
        String year = yearEditText.getText().toString().trim();
        String department = departmentEditText.getText().toString().trim();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = userDatabaseReference.child(userId);

            userRef.child("name").setValue(name);
            userRef.child("year").setValue(year);
            userRef.child("department").setValue(department).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
