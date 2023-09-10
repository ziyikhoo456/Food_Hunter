package com.example.groupassignment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

//Done by Khoo Zi Yi
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>{
    private List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList){
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant_detail, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.usernameTextView.setText(review.getName());
        holder.relativeTimeTextView.setText(review.getRelativeTime());
        holder.ratingTextView.setText("Rating: "+Double.toString(review.getRating()));
        holder.textTextView.setText(review.getText());

        // Load and display the photo using Glide
        if(review.getProfilePicURL() != null){
            Glide.with(holder.itemView.getContext())
                    .load(review.getProfilePicURL())
                    .into(holder.userIcon);
        }

    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, relativeTimeTextView, textTextView, ratingTextView;
        ImageView userIcon;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameTextView = itemView.findViewById(R.id.usernameTV);
            relativeTimeTextView = itemView.findViewById(R.id.relativeTimeTV);
            ratingTextView = itemView.findViewById(R.id.ratingTV);
            textTextView = itemView.findViewById(R.id.textTV);
            userIcon = itemView.findViewById(R.id.userIcon);

        }
    }
}
