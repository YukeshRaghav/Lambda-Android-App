package com.android.lambda.home.post;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.lambda.R;
import com.android.lambda.home.comment.CommentActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private final String currentUserId;
    private List<Post> postList;
    private Context context;
    private DatabaseReference postsDatabaseReference;

    public PostsAdapter(Context context, List<Post> postList, String currentUserId) {
        this.context = context;
        this.postList = postList;
        this.currentUserId = currentUserId;
        this.postsDatabaseReference = FirebaseDatabase.getInstance().getReference("Posts");
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView userImageView;
        TextView nameTextView, titleTextView, topicTextView, dateTextView, contentTextView, upvoteCountTextView, downvoteCountTextView;
        ImageButton upvoteButton, downvoteButton, shareButton, commentButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            userImageView = itemView.findViewById(R.id.userImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            topicTextView = itemView.findViewById(R.id.topicTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            upvoteCountTextView = itemView.findViewById(R.id.upvoteCountTextView);
            downvoteCountTextView = itemView.findViewById(R.id.downvoteCountTextView);

            upvoteButton = itemView.findViewById(R.id.upvoteButton);
            downvoteButton = itemView.findViewById(R.id.downvoteButton);
            shareButton = itemView.findViewById(R.id.shareButton);
            commentButton = itemView.findViewById(R.id.commentButton);
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.dateTextView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(post.timestamp)));
        holder.titleTextView.setText(post.title);
        holder.topicTextView.setText(post.topic);

        holder.contentTextView.setMaxLines(2);
        holder.contentTextView.setEllipsize(TextUtils.TruncateAt.END);
        holder.contentTextView.setText(post.content);

        holder.upvoteCountTextView.setText(String.valueOf(post.upvotes));
        holder.downvoteCountTextView.setText(String.valueOf(post.downvotes));
        holder.nameTextView.setText(post.name);
        Glide.with(context).load(post.profileImageUrl).into(holder.userImageView);

        DatabaseReference userPreferenceRef = postsDatabaseReference.child(post.postId).child("userPref").child(currentUserId);
        userPreferenceRef.get().addOnSuccessListener(dataSnapshot -> {
            String userVote = dataSnapshot.getValue(String.class);

            boolean upvoted = "u".equals(userVote);
            boolean downvoted = "d".equals(userVote);

            holder.upvoteButton.setImageResource(upvoted ? R.drawable.ic_upvote_selected : R.drawable.ic_upvote);
            holder.downvoteButton.setImageResource(downvoted ? R.drawable.ic_downvote_selected : R.drawable.ic_downvote);

            holder.upvoteButton.setOnClickListener(v -> {
                if (downvoted) {
                    post.downvotes--;
                    updateUserPreferenceInFirebase(post.postId, post.downvotes, post.upvotes, null);
                }

                if (!upvoted) {
                    post.upvotes++;
                    updateUserPreferenceInFirebase(post.postId, post.downvotes, post.upvotes, "u");
                } else {
                    post.upvotes--;
                    updateUserPreferenceInFirebase(post.postId, post.downvotes, post.upvotes, null);
                }

                holder.upvoteCountTextView.setText(String.valueOf(post.upvotes));
                holder.downvoteCountTextView.setText(String.valueOf(post.downvotes));
                notifyItemChanged(holder.getAdapterPosition());  // Update only this item
            });

            holder.downvoteButton.setOnClickListener(v -> {
                if (upvoted) {
                    post.upvotes--;
                    updateUserPreferenceInFirebase(post.postId, post.downvotes, post.upvotes, null);
                }

                if (!downvoted) {
                    post.downvotes++;
                    updateUserPreferenceInFirebase(post.postId, post.downvotes, post.upvotes, "d");
                } else {
                    post.downvotes--;
                    updateUserPreferenceInFirebase(post.postId, post.downvotes, post.upvotes, null);
                }

                holder.upvoteCountTextView.setText(String.valueOf(post.upvotes));
                holder.downvoteCountTextView.setText(String.valueOf(post.downvotes));
                notifyItemChanged(holder.getAdapterPosition());  // Update only this item
            });
        });

        holder.shareButton.setOnClickListener(v -> {
            String shareMessage = "Check out this post from Lambda, Title: " + post.title + "Content: " + post.content;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            context.startActivity(Intent.createChooser(shareIntent, "Share Post"));
        });

        holder.commentButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("postId", post.postId);
            intent.putExtra("postTitle", post.title);
            intent.putExtra("postContent", post.content);
            intent.putExtra("postUserName", post.name);
            intent.putExtra("postUserDepartmentYear", post.department + ", " + post.year);
            intent.putExtra("postUserImageUrl", post.profileImageUrl);
            intent.putExtra("postTimestamp", post.timestamp);
            context.startActivity(intent);
        });
    }

    private void updateUserPreferenceInFirebase(String postId, int downvotes, int upvotes, String preference) {
        DatabaseReference postRef = postsDatabaseReference.child(postId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("downvotes", downvotes);
        updates.put("upvotes", upvotes);
        if (preference == null) {
            updates.put("userPref/" + currentUserId, null);
        } else {
            updates.put("userPref/" + currentUserId, preference);
        }
        postRef.updateChildren(updates);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}
