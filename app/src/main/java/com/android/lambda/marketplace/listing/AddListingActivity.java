package com.android.lambda.marketplace.listing;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.android.lambda.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AddListingActivity extends AppCompatActivity {

    private TextInputEditText itemNameEditText, descriptionEditText, otherCategoryEditText, priceEditText;
    private TextInputLayout otherCategoryLayout;
    private Spinner categorySpinner;
    private ImageView itemImageView;
    private Button addListingButton;
    private Uri itemImageUri;
    private DatabaseReference listingsDatabaseReference;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_listing);

        itemNameEditText = findViewById(R.id.itemNameEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        otherCategoryEditText = findViewById(R.id.otherCategoryEditText);
        otherCategoryLayout = findViewById(R.id.otherCategoryLayout);
        priceEditText = findViewById(R.id.priceEditText);
        itemImageView = findViewById(R.id.itemImageView);
        addListingButton = findViewById(R.id.addListingButton);

        listingsDatabaseReference = FirebaseDatabase.getInstance().getReference("Marketplace");
        storageReference = FirebaseStorage.getInstance().getReference("ListingImages");
        mAuth = FirebaseAuth.getInstance();

        setupCategorySpinner();

        itemImageView.setOnClickListener(v -> openFileChooser());

        addListingButton.setOnClickListener(v -> addListing());
    }

    private void setupCategorySpinner() {
        String[] categories = {"Electronics", "Books", "Clothing", "Home", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (categories[position].equals("Other")) {
                    otherCategoryLayout.setVisibility(View.VISIBLE);
                } else {
                    otherCategoryLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                otherCategoryLayout.setVisibility(View.GONE);
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            itemImageUri = data.getData();
            itemImageView.setImageURI(itemImageUri);
        }
    }

    private void addListing() {
        String itemName = itemNameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String category = selectedCategory.equals("Other") ? otherCategoryEditText.getText().toString().trim() : selectedCategory;
        String priceStr = priceEditText.getText().toString().trim();

        if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(description) || TextUtils.isEmpty(category) || TextUtils.isEmpty(priceStr) || itemImageUri == null) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to add a listing", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");

        fileReference.putFile(itemImageUri).addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
            String itemImageUrl = uri.toString();
            String listingId = listingsDatabaseReference.push().getKey();

            if (listingId == null) {
                Toast.makeText(this, "Failed to create listing", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> listingData = new HashMap<>();
            listingData.put("listingId", listingId);
            listingData.put("userId", userId);
            listingData.put("itemName", itemName);
            listingData.put("description", description);
            listingData.put("category", category);
            listingData.put("price", price);
            listingData.put("timestamp", System.currentTimeMillis());
            listingData.put("itemImageUrl", itemImageUrl);

            listingsDatabaseReference.child(listingId).setValue(listingData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Listing added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to add listing", Toast.LENGTH_SHORT).show();
                }
            });
        })).addOnFailureListener(e -> Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show());
    }
}
