package com.android.lambda.marketplace.listing;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.lambda.R;
import com.android.lambda.marketplace.comment.MarketplaceCommentActivity;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ListingsAdapter extends RecyclerView.Adapter<ListingsAdapter.ListingViewHolder> {

    private final String currentUserId;
    private List<Listing> listingList;
    private Context context;

    public ListingsAdapter(Context context, List<Listing> listingList, String currentUserId) {
        this.context = context;
        this.listingList = listingList;
        this.currentUserId = currentUserId;
    }

    class ListingViewHolder extends RecyclerView.ViewHolder {
        ImageView userImageView, itemImageView;
        TextView nameTextView, itemNameTextView, categoryTextView, dateTextView, descriptionTextView, priceTextView;

        public ListingViewHolder(@NonNull View itemView) {
            super(itemView);

            userImageView = itemView.findViewById(R.id.userImageView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
        }
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listing, parent, false);
        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        Listing listing = listingList.get(position);
        holder.dateTextView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(listing.timestamp)));
        holder.itemNameTextView.setText(listing.itemName);
        holder.categoryTextView.setText(listing.category);
        holder.descriptionTextView.setText(listing.description);
        holder.priceTextView.setText(String.valueOf(listing.price));
        holder.nameTextView.setText(listing.name);

        Glide.with(context).load(listing.profileImageUrl).into(holder.userImageView);
        Glide.with(context).load(listing.itemImageUrl).into(holder.itemImageView);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MarketplaceCommentActivity.class);
            intent.putExtra("listingId", listing.listingId);
            intent.putExtra("itemName", listing.itemName);
            intent.putExtra("itemDescription", listing.description);
            intent.putExtra("itemCategory", listing.category);
            intent.putExtra("itemPrice", listing.price);
            intent.putExtra("listingTimestamp", listing.timestamp);
            intent.putExtra("listingUserName", listing.name);
            intent.putExtra("listingUserProfileImageUrl", listing.profileImageUrl);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listingList.size();
    }
}
