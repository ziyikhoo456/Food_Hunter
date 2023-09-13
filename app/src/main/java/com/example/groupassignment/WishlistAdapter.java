package com.example.groupassignment;

import android.content.Intent;
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
public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.RestaurantViewHolder> {
    private final String apiKey = "Enter your API key here";

    private List<Restaurant> restaurantList;

    public WishlistAdapter(List<Restaurant> restaurantList) {
        this.restaurantList = restaurantList;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wishlist, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);
        holder.restaurantNameTextView.setText(restaurant.getName());
        holder.restaurantRatingTextView.setText("Rating: " + restaurant.getRating()+" ("+restaurant.getUserRatingTotal()+")");
        //holder.restaurantOpenNowTextView.setText("Opening: "+ restaurant.isOpening_now());
        //holder.restaurantDistanceTextView.setText("Distance: "+restaurant.getDistance()+" km");
        holder.restaurantPriceLevelTextView.setText("Price: "+this.getStringPriceLevel(restaurant.getPriceLevel()));

        holder.placeID = restaurant.getPlaceID();
        holder.name = restaurant.getName();
        holder.rating = restaurant.getRating();
        holder.openingNow = restaurant.isOpening_now();
        holder.distance = restaurant.getDistance();
        holder.priceLevel = restaurant.getPriceLevel();
        holder.userRatingTotal = restaurant.getUserRatingTotal();
        holder.photoReference = restaurant.getPhotoReference();


        // Load and display the photo using Glide
        if(restaurant.getPhotoReference() != null){
            String photoUrl = getPhotoUrl(restaurant.getPhotoReference());
            Glide.with(holder.itemView.getContext())
                    .load(photoUrl)
                    .into(holder.restaurantImageView);
        }

    }

    private String getPhotoUrl(String photoReference) {
        // Construct the URL using your API key and the photo reference
        return "https://maps.googleapis.com/maps/api/place/photo"+
                "?maxwidth=500"+
                "&photo_reference=" + photoReference +
                "&key=" + apiKey;
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView restaurantNameTextView, restaurantRatingTextView, restaurantOpenNowTextView, restaurantDistanceTextView, restaurantPriceLevelTextView;
        ImageView restaurantImageView;
        String placeID, photoReference, name;
        double rating, distance;
        boolean openingNow;
        int priceLevel, userRatingTotal;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurantNameTextView = itemView.findViewById(R.id.resNameTV);
            restaurantRatingTextView = itemView.findViewById(R.id.resRatingTV);
            restaurantOpenNowTextView = itemView.findViewById(R.id.resOpenNowTV);
            restaurantImageView = itemView.findViewById(R.id.resImageView);
            restaurantDistanceTextView = itemView.findViewById(R.id.resDistanceTV);
            restaurantPriceLevelTextView = itemView.findViewById(R.id.resPriceLevelTV);

            itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), RestaurantDetailActivity.class);
                    intent.putExtra("PlaceID", placeID); // Pass the PlaceID to the next activity
                    intent.putExtra("Name",name);
                    intent.putExtra("PhotoReference",photoReference);
                    intent.putExtra("OpeningNow", openingNow);
                    intent.putExtra("Rating", rating);
                    intent.putExtra("Distance",distance);
                    intent.putExtra("PriceLevel", priceLevel);
                    intent.putExtra("UserRatingTotal",userRatingTotal);
                    view.getContext().startActivity(intent);
                }
            });
        }
    }

    public String getStringPriceLevel(int priceLevel){
        switch(priceLevel){
            case 0:
                return "Free";

            case 1:
                return "Inexpensive";

            case 2:
                return "Moderate";

            case 3:
                return "Expensive";

            case 4:
                return "Very Expensive";

            default:
                return "-";
        }
    }
}
