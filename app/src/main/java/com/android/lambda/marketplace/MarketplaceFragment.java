package com.android.lambda.marketplace;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.lambda.R;
import com.android.lambda.marketplace.listing.AddListingActivity;
import com.android.lambda.marketplace.listing.Listing;
import com.android.lambda.marketplace.listing.ListingsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceFragment extends Fragment {

    private RecyclerView marketRecyclerView;
    private FloatingActionButton fabAddListing;
    private DatabaseReference listingsDatabaseReference;
    private List<Listing> listingList;
    private ListingsAdapter listingsAdapter;
    private String currentUserId;

    public MarketplaceFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_marketplace, container, false);

        listingsDatabaseReference = FirebaseDatabase.getInstance().getReference("Marketplace");

        marketRecyclerView = view.findViewById(R.id.marketRecyclerView);
        fabAddListing = view.findViewById(R.id.fabAddListing);

        marketRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listingList = new ArrayList<>();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        currentUserId = currentUser != null ? currentUser.getUid() : null;

        listingsAdapter = new ListingsAdapter(getContext(), listingList, currentUserId);
        marketRecyclerView.setAdapter(listingsAdapter);

        loadListings();

        fabAddListing.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddListingActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadListings() {
        listingsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listingList.clear();
                List<Listing> tempListingList = new ArrayList<>();

                for (DataSnapshot listingSnapshot : snapshot.getChildren()) {
                    String listingId = listingSnapshot.getKey();
                    String userId = listingSnapshot.child("userId").getValue(String.class);
                    String itemName = listingSnapshot.child("itemName").getValue(String.class);
                    String description = listingSnapshot.child("description").getValue(String.class);
                    long timestamp = listingSnapshot.child("timestamp").getValue(long.class);
                    String category = listingSnapshot.child("category").getValue(String.class);
                    double price = listingSnapshot.child("price").getValue(double.class);
                    String itemImageUrl = listingSnapshot.child("itemImageUrl").getValue(String.class);

                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            String name = userSnapshot.child("name").getValue(String.class);
                            String profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String.class);

                            Listing listing = new Listing(listingId, userId, name, itemName, timestamp, description, category, price, itemImageUrl, profileImageUrl);
                            tempListingList.add(listing);

                            if (tempListingList.size() == snapshot.getChildrenCount()) {
                                listingList.clear();
                                listingList.addAll(tempListingList);

                                if (listingList.isEmpty()) {
                                    marketRecyclerView.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "No listings available", Toast.LENGTH_SHORT).show();
                                } else {
                                    marketRecyclerView.setVisibility(View.VISIBLE);
                                    listingsAdapter.notifyDataSetChanged();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load listings", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
