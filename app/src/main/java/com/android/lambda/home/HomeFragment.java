package com.android.lambda.home;

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
import com.android.lambda.home.post.AddPostActivity;
import com.android.lambda.home.post.Post;
import com.android.lambda.home.post.PostsAdapter;
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

public class HomeFragment extends Fragment {

    private RecyclerView postsRecyclerView;
    private FloatingActionButton fabAddPost;
    private DatabaseReference postsDatabaseReference;
    private List<Post> postList;
    private PostsAdapter postsAdapter;
    private String currentUserId;

    public HomeFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        postsDatabaseReference = FirebaseDatabase.getInstance().getReference("Posts");

        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        fabAddPost = view.findViewById(R.id.fabAddPost);

        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        currentUserId = currentUser.getUid();

        postsAdapter = new PostsAdapter(getContext(), postList, currentUserId);  // Pass currentUserId to PostsAdapter
        postsRecyclerView.setAdapter(postsAdapter);

        loadPosts();

        fabAddPost.setOnClickListener(v -> {
            // Handle adding a new post
            Intent intent = new Intent(getContext(), AddPostActivity.class);
            startActivity(intent);
        });


        return view;
    }

    private void loadPosts() {
        postsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                List<Post> tempPostList = new ArrayList<>();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String postId = postSnapshot.getKey();
                    String userId = postSnapshot.child("userId").getValue(String.class);
                    String title = postSnapshot.child("title").getValue(String.class);
                    String content = postSnapshot.child("content").getValue(String.class);
                    long timestamp = postSnapshot.child("timestamp").getValue(long.class);
                    int upvotes = postSnapshot.child("upvotes").getValue(Integer.class);
                    int downvotes = postSnapshot.child("downvotes").getValue(Integer.class);
                    String topic = postSnapshot.child("topic").getValue(String.class);  // Add this line

                    // Fetch user details
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            String department = userSnapshot.child("department").getValue(String.class);
                            String year = userSnapshot.child("year").getValue(String.class);
                            String name = userSnapshot.child("name").getValue(String.class);
                            String profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String.class);

                            // Create and add post with user details
                            Post post = new Post(postId, userId, name, title, timestamp, content, upvotes, downvotes, profileImageUrl, department, year, topic);  // Add topic
                            tempPostList.add(post);

                            // Update the adapter and visibility after all posts are fetched
                            if (tempPostList.size() == snapshot.getChildrenCount()) {
                                postList.clear();
                                postList.addAll(tempPostList);

                                if (postList.isEmpty()) {
                                    postsRecyclerView.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "No posts available", Toast.LENGTH_SHORT).show();
                                } else {
                                    postsRecyclerView.setVisibility(View.VISIBLE);
                                    postsAdapter.notifyDataSetChanged();
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
                Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}