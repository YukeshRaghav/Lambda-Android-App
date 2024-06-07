package com.android.lambda.home.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.lambda.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private TextInputEditText titleEditText, otherTopicEditText, contentEditText;
    private TextInputLayout otherTopicLayout;
    private Spinner topicSpinner;
    private Button postButton;
    private DatabaseReference postsDatabaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        titleEditText = findViewById(R.id.titleEditText);
        topicSpinner = findViewById(R.id.topicSpinner);
        otherTopicEditText = findViewById(R.id.otherTopicEditText);
        otherTopicLayout = findViewById(R.id.otherTopicLayout);
        contentEditText = findViewById(R.id.contentEditText);
        postButton = findViewById(R.id.postButton);

        postsDatabaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        mAuth = FirebaseAuth.getInstance();

        setupTopicSpinner();

        postButton.setOnClickListener(v -> addNewPost());
    }

    private void setupTopicSpinner() {
        String[] topics = {"Programming", "Class", "Game", "Music", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, topics);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        topicSpinner.setAdapter(adapter);

        topicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (topics[position].equals("Other")) {
                    otherTopicLayout.setVisibility(View.VISIBLE);
                } else {
                    otherTopicLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                otherTopicLayout.setVisibility(View.GONE);
            }
        });
    }

    private void addNewPost() {
        String title = titleEditText.getText().toString().trim();
        String selectedTopic = topicSpinner.getSelectedItem().toString();
        String topic = selectedTopic.equals("Other") ? otherTopicEditText.getText().toString().trim() : selectedTopic;
        String content = contentEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(topic) || TextUtils.isEmpty(content) || selectedTopic.equals("Select Topic")) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String postId = postsDatabaseReference.push().getKey();
            long timestamp = System.currentTimeMillis();

            Map<String, Object> postMap = new HashMap<>();
            postMap.put("postId", postId);
            postMap.put("userId", userId);
            postMap.put("title", title);
            postMap.put("topic", topic);
            postMap.put("content", content);
            postMap.put("timestamp", timestamp);
            postMap.put("upvotes", 0);
            postMap.put("downvotes", 0);

            if (postId != null) {
                postsDatabaseReference.child(postId).setValue(postMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Post added successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity and go back to the previous one
                    } else {
                        Toast.makeText(this, "Failed to add post", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
