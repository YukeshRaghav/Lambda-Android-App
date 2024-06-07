package com.android.lambda;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.lambda.auth.LoginActivity;
import com.android.lambda.chatbot.ChatbotFragment;
import com.android.lambda.home.HomeFragment;
import com.android.lambda.marketplace.MarketplaceFragment;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ImageView navUserProfileImage;
    private TextView titleTextView, navUserProfileName, navUserMajorYear;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private NavigationView navigationView;
    private DatabaseReference userDatabaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        mAuth = FirebaseAuth.getInstance();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");

        titleTextView = findViewById(R.id.title);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setItemActiveIndicatorColor(ContextCompat.getColorStateList(this, R.color.colorTint));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            } else if (id == R.id.nav_logout) {
                mAuth.signOut();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        View headerView = navigationView.getHeaderView(0);
        navUserProfileImage = headerView.findViewById(R.id.navUserProfileImage);
        navUserProfileName = headerView.findViewById(R.id.navUserProfileName);
        navUserMajorYear = headerView.findViewById(R.id.navUserMajorYear);

        loadUserProfile();

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, new HomeFragment())
                    .commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragment = null;

            if (id == R.id.nav_home) {
                bottomNavigationView.setItemActiveIndicatorColor(ContextCompat.getColorStateList(this, R.color.colorTint));
                titleTextView.setText("Lambda - SkillShare");
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_marketplace) {
                selectedFragment = new MarketplaceFragment();
                titleTextView.setText("Lambda - Shop");
                bottomNavigationView.setItemActiveIndicatorColor(ContextCompat.getColorStateList(this, R.color.colorTint));
            } else if (id == R.id.nav_chatbot) {
                selectedFragment = new ChatbotFragment();
                titleTextView.setText("Lambda - Learn");
                bottomNavigationView.setItemActiveIndicatorColor(ContextCompat.getColorStateList(this, R.color.colorTint));
            }

            if (selectedFragment != null) {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
            }
            return true;
        });
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
                        navUserProfileName.setText(name);
                    }
                    if (year != null && department != null) {
                        navUserMajorYear.setText(year + ", " + department);
                    }
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(HomeActivity.this).load(profileImageUrl).into(navUserProfileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}