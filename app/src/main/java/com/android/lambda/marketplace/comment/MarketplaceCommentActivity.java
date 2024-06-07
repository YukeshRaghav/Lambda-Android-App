package com.android.lambda.marketplace.comment;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.lambda.R;
import com.android.lambda.home.comment.Comment;
import com.android.lambda.home.comment.CommentsAdapter;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketplaceCommentActivity extends AppCompatActivity {

    private TextView itemNameTextView, itemUserNameTextView, itemCategoryTextView, itemPriceTextView, itemDateTextView, itemDescriptionTextView;
    private ImageView itemUserImageView, itemImageView;
    private EditText commentEditText;
    private Button postCommentButton;
    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private List<Comment> commentList;
    private DatabaseReference commentsDatabaseReference;
    private DatabaseReference marketplaceDatabaseReference;
    private String listingId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketplace_comment);

        listingId = getIntent().getStringExtra("listingId");
        String itemName = getIntent().getStringExtra("itemName");
        String itemDescription = getIntent().getStringExtra("itemDescription");
        String itemCategory = getIntent().getStringExtra("itemCategory");
        double itemPrice = getIntent().getDoubleExtra("itemPrice", 0.0);
        long listingTimestamp = getIntent().getLongExtra("listingTimestamp", 0);
        String listingUserName = getIntent().getStringExtra("listingUserName");
        String listingUserProfileImageUrl = getIntent().getStringExtra("listingUserProfileImageUrl");

        itemNameTextView = findViewById(R.id.itemNameTextView);
        itemUserNameTextView = findViewById(R.id.itemUserNameTextView);
        itemCategoryTextView = findViewById(R.id.itemCategoryTextView);
        itemPriceTextView = findViewById(R.id.itemPriceTextView);
        itemDateTextView = findViewById(R.id.itemDateTextView);
        itemDescriptionTextView = findViewById(R.id.itemDescriptionTextView);
        itemUserImageView = findViewById(R.id.itemUserImageView);
        itemImageView = findViewById(R.id.itemImageView);
        commentEditText = findViewById(R.id.commentEditText);
        postCommentButton = findViewById(R.id.postCommentButton);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);

        itemNameTextView.setText(itemName);
        itemUserNameTextView.setText(listingUserName);
        itemCategoryTextView.setText(itemCategory);
        itemPriceTextView.setText(String.valueOf(itemPrice));
        itemDateTextView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(listingTimestamp)));
        itemDescriptionTextView.setText(itemDescription);
        Glide.with(this).load(listingUserProfileImageUrl).into(itemUserImageView);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser != null ? currentUser.getUid() : null;

        marketplaceDatabaseReference = FirebaseDatabase.getInstance().getReference("Marketplace").child(listingId);
        marketplaceDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String itemImageUrl = snapshot.child("itemImageUrl").getValue(String.class);
                    Glide.with(MarketplaceCommentActivity.this).load(itemImageUrl).into(itemImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MarketplaceCommentActivity.this, "Failed to load item image", Toast.LENGTH_SHORT).show();
            }
        });

        commentsDatabaseReference = FirebaseDatabase.getInstance().getReference("MarketplaceComments").child(listingId);
        commentList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(this, commentList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);

        loadComments();

        postCommentButton.setOnClickListener(v -> postComment());
    }

    private void loadComments() {
        commentsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MarketplaceCommentActivity.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment() {
        String commentContent = commentEditText.getText().toString().trim();
        if (TextUtils.isEmpty(commentContent)) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentId = commentsDatabaseReference.push().getKey();
        long timestamp = System.currentTimeMillis();
        Comment comment = new Comment(commentId, listingId, currentUserId, commentContent, timestamp);

        commentsDatabaseReference.child(commentId).setValue(comment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MarketplaceCommentActivity.this, "Comment posted", Toast.LENGTH_SHORT).show();
                    commentEditText.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(MarketplaceCommentActivity.this, "Failed to post comment", Toast.LENGTH_SHORT).show());
    }
}
