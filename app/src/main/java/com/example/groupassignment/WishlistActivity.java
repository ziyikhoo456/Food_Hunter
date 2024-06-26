package com.example.groupassignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

//Done by Khoo Zi Yi
public class WishlistActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WishlistAdapter adapter;
    private final List<Restaurant> allRestaurants = new ArrayList<>();
    private SQLiteAdapter resSQLiteAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        recyclerView = findViewById(R.id.wishlistRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter only once
        adapter = new WishlistAdapter(allRestaurants);
        recyclerView.setAdapter(adapter);

        displayRestaurants();
    }

    @Override
    protected void onResume() {
        super.onResume();
        allRestaurants.clear();
        adapter.notifyDataSetChanged();
        displayRestaurants();
    }

    private void displayRestaurants(){
        //open the table to write
        resSQLiteAdapter = new SQLiteAdapter(this);
        resSQLiteAdapter.openToRead();

        if(resSQLiteAdapter.getCount() != 0){
            for(int i = 0; i < resSQLiteAdapter.getCount(); i++){
                String placeID = resSQLiteAdapter.getString("PlaceID",i);
                String name = resSQLiteAdapter.getString("Name",i);
                String photoReference = resSQLiteAdapter.getString("PhotoReference",i);
                boolean openingNow = resSQLiteAdapter.getBoolean("OpeningNow",i);
                double rating = resSQLiteAdapter.getDouble("Rating",i);
                double distance = resSQLiteAdapter.getDouble("Distance",i);
                int priceLevel = resSQLiteAdapter.getInteger("PriceLevel",i);
                int userRatingTotal = resSQLiteAdapter.getInteger("UserRatingTotal",i);
                allRestaurants.add(new Restaurant(name,rating,placeID,photoReference,openingNow,distance,priceLevel,userRatingTotal));
            }

        }
        resSQLiteAdapter.close();
    }
}
