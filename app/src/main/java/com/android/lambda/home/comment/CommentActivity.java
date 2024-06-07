package com.android.lambda.home.comment;

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

public class CommentActivity extends AppCompatActivity {

    private TextView postTitleTextView;
    private TextView postUserNameTextView;
    private TextView postUserDepartmentYearTextView;
    private TextView postDateTextView;
    private TextView postDetailsTextView;
    private ImageView postUserImageView;
    private EditText commentEditText;
    private Button postCommentButton;
    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private List<Comment> commentList;
    private DatabaseReference commentsDatabaseReference;
    private String postId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        postId = getIntent().getStringExtra("postId");
        String postTitle = getIntent().getStringExtra("postTitle");
        String postContent = getIntent().getStringExtra("postContent");
        String postUserName = getIntent().getStringExtra("postUserName");
        String postUserDepartmentYear = getIntent().getStringExtra("postUserDepartmentYear");
        String postUserImageUrl = getIntent().getStringExtra("postUserImageUrl");
        long postTimestamp = getIntent().getLongExtra("postTimestamp", 0);

        postTitleTextView = findViewById(R.id.postTitleTextView);
        postUserNameTextView = findViewById(R.id.postUserNameTextView);
        postUserDepartmentYearTextView = findViewById(R.id.postUserDepartmentYearTextView);
        postDateTextView = findViewById(R.id.postDateTextView);
        postDetailsTextView = findViewById(R.id.postDetailsTextView);
        postUserImageView = findViewById(R.id.postUserImageView);
        commentEditText = findViewById(R.id.commentEditText);
        postCommentButton = findViewById(R.id.postCommentButton);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);

        postTitleTextView.setText(postTitle);
        postUserNameTextView.setText(postUserName);
        postUserDepartmentYearTextView.setText(postUserDepartmentYear);
        postDateTextView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(postTimestamp)));
        postDetailsTextView.setText(postContent);
        Glide.with(this).load(postUserImageUrl).into(postUserImageView);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();

        commentsDatabaseReference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
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
                Toast.makeText(CommentActivity.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
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
        Comment comment = new Comment(commentId, postId, currentUserId, commentContent, timestamp);

        commentsDatabaseReference.child(commentId).setValue(comment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CommentActivity.this, "Comment posted", Toast.LENGTH_SHORT).show();
                    commentEditText.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(CommentActivity.this, "Failed to post comment", Toast.LENGTH_SHORT).show());
    }
}
