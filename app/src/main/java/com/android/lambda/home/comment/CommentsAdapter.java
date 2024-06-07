package com.android.lambda.home.comment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.lambda.R;
import com.android.lambda.auth.User;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> commentList;

    public CommentsAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView userImageView;
        TextView nameTextView, departmentYearTextView, timestampTextView, commentContentTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userImageView = itemView.findViewById(R.id.userImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            departmentYearTextView = itemView.findViewById(R.id.departmentYearTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            commentContentTextView = itemView.findViewById(R.id.commentContentTextView);
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        // Fetch user data
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(comment.userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    holder.nameTextView.setText(user.name);
                    holder.departmentYearTextView.setText(user.department + ", " + user.year);
                    Glide.with(context).load(user.profileImageUrl).into(holder.userImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        holder.commentContentTextView.setText(comment.content);
        holder.timestampTextView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(comment.timestamp)));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
}
