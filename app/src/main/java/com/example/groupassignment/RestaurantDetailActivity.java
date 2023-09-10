package com.example.groupassignment;

import static android.Manifest.permission.SEND_SMS;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//Done by Khoo Zi Yi
public class RestaurantDetailActivity extends AppCompatActivity {
    private RecyclerView reviewRecyclerView, photoRecyclerView;
    private ReviewAdapter reviewAdapter;
    private PhotoAdapter photoAdapter;
    private String placeID, address, phoneNum, name, weekdayText, photoReference, website;
    private double rating;
    private int userRateTotal;
    private boolean isAddedWishlist = false;
    private SQLiteAdapter resSQLiteAdapter;
    private List<Review> reviewList = new ArrayList<>();
    private List<String> photoReferenceList = new ArrayList<>();
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        FloatingActionButton wishlistBtn = (FloatingActionButton) findViewById(R.id.WishListBtn);
        ImageView photoImgView = (ImageView) findViewById(R.id.RDresImage);
        TextView RDresNameTV = (TextView) findViewById(R.id.RDresNameTV);
        TextView RDaddressTV = (TextView) findViewById(R.id.RDAddressTV);
        TextView RDphoneNumTV = (TextView) findViewById(R.id.RDphoneNumTV);
        TextView RDratingTV = (TextView) findViewById(R.id.RDratingTV);
        TextView RDwebsiteTV = (TextView) findViewById(R.id.RDwebsiteTV);
        TextView RDweekdayTextTV = (TextView) findViewById(R.id.RDweekdayTextTV);


        //Declare and initialize the review recycler view
        reviewRecyclerView = findViewById(R.id.RDRecyclerView);
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviewList);
        reviewRecyclerView.setAdapter(reviewAdapter);

        //Declare and initialize the photo recycler view
        photoRecyclerView = findViewById(R.id.RDPhotoRecyclerView);
        photoRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        photoAdapter = new PhotoAdapter(photoReferenceList);
        photoRecyclerView.setAdapter(photoAdapter);

        Intent intent = getIntent();
        placeID = intent.getStringExtra("PlaceID");
        photoReference = intent.getStringExtra("PhotoReference");
        String wishlistName = intent.getStringExtra("Name");
        boolean wishlistOpeningNow = intent.getBooleanExtra("OpeningNow",false);
        double wishlistRating = intent.getDoubleExtra("Rating",0.0);
        double wishlistDistance = intent.getDoubleExtra("Distance",0.0);
        int wishlistPriceLevel = intent.getIntExtra("PriceLevel",-1);
        int wishlistUserRatingTotal = intent.getIntExtra("UserRatingTotal",0);

        //initialize the database
        resSQLiteAdapter = new SQLiteAdapter(this);
        resSQLiteAdapter.openToRead();

        //check if this restaurant is added into WishList
        for(int i = 0; i < resSQLiteAdapter.getCount(); i++){
            if(placeID.compareTo(resSQLiteAdapter.getString("PlaceID",i)) == 0){
                //wishlistBtn.setImageResource(R.drawable.filled_wishlist_icon);
                wishlistBtn.setForeground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.filled_wishlist_icon));
                isAddedWishlist = true;
                break;
            }
        }
        resSQLiteAdapter.close();

        new Thread() {
            public void run() {
                try {
                    URL url = new URL("https://maps.googleapis.com/maps/api/place/details/json" +
                            "?fields=name%2C" +
                            "geometry%2C"+
                            "formatted_address%2C" +
                            "address_components%2C"+
                            "international_phone_number%2C" +
                            "opening_hours/weekday_text%2C" +
                            "icon%2C"+
                            "photos%2C" +
                            "rating%2C" +
                            "reviews%2C" +
                            "url%2C" +
                            "user_ratings_total%2C" +
                            "website" +
                            "&place_id=" + placeID +
                            "&key=AIzaSyC1n_2xoJGwMsbKK43o-hYAnGdbEjpZq1w");

                    HttpURLConnection hc = (HttpURLConnection) url.openConnection();

                    InputStream input = hc.getInputStream();
                    String response = readStream(input);

                    //OK response code
                    if (hc.getResponseCode() == 200) {
                        Log.i("Success", "Response: " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject result = jsonObject.getJSONObject("result");

                            //Get every restaurant's name, rating
                            address = result.optString("formatted_address", "-");
                            phoneNum = result.optString("international_phone_number", "-");
                            name = result.optString("name","-");

                            JSONArray photosArray = result.optJSONArray("photos");

                            if(photosArray != null){

                                if(photoReference == null){
                                    photoReference = photosArray.getJSONObject(0).optString("photo_reference",null);
                                }

                                for(int i = 0; i < photosArray.length(); i++){
                                    String photoReference = photosArray.getJSONObject(i).optString("photo_reference",null);
                                    photoReferenceList.add(photoReference);
                                }
                            }


                            /*JSONObject location = result.getJSONObject("geometry").getJSONObject("location");
                            latitude = location.getDouble("lat");
                            longitude = location.getDouble("lng");*/

                            JSONObject openingHours = result.optJSONObject("opening_hours");

                            if(openingHours != null){

                                JSONArray weekdayTextArray = openingHours.optJSONArray("weekday_text");
                                weekdayText = "";

                                if(openingHours != null){
                                //To combine the text in array.
                                for(int i = 0; i < weekdayTextArray.length(); i++){
                                    weekdayText += weekdayTextArray.getString(i)+"\n";
                                }
                                weekdayText += "\n";
                                } else {
                                    weekdayText = "-";
                                }
                            }

                            rating = result.optDouble("rating",0.0);

                            JSONArray reviewArray = result.optJSONArray("reviews");

                            if(reviewArray != null){
                                reviewList.clear();

                                for(int i = 0; i < reviewArray.length();i++){
                                    JSONObject review = reviewArray.getJSONObject(i);
                                    String name = review.optString("author_name","Anonymous");
                                    String profilePicURL = review.optString("profile_photo_url",null);
                                    double rating = review.optDouble("rating",0.0);
                                    String relativeTime = review.optString("relative_time_description","");
                                    String text = review.optString("text","");

                                    Review newReview = new Review(name,profilePicURL,rating,relativeTime,text);
                                    reviewList.add(newReview);
                                }
                                userRateTotal = result.optInt("user_ratings_total",0);
                                website = result.optString("website","-");
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(() -> {

                            RDresNameTV.setText(name);

                            if(photoReference != null){
                                String photoUrl = getPhotoUrl(photoReference);
                                Glide.with(RestaurantDetailActivity.this)
                                        .load(photoUrl)
                                        .into(photoImgView);
                            }

                            RDaddressTV.setText(address);
                            RDphoneNumTV.setText(phoneNum);
                            RDratingTV.setText("Rating ("+userRateTotal+"): "+Double.toString(rating));
                            RDweekdayTextTV.setText(weekdayText);
                            RDwebsiteTV.setText(website);
                        });



                    } else{
                        Log.i("Error", "Response code: " + hc.getResponseCode());
                    }

                    input.close();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                    //Log.i("Error", ex.getMessage());
                }

            }
        }.start();

        // Inside your onCreate method
        Button navigateBtn = findViewById(R.id.NagivateBtn);
        navigateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Create a Uri with the restaurant's location
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + name+", "+address);

                // Create an Intent to open Google Maps with the specified location
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps"); // Specify the Google Maps package

                // Check if the Google Maps app is installed on the device
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent); // Open Google Maps
                } else {
                    // If Google Maps is not installed, you can handle this case here
                    // For example, you can show a message or prompt the user to install it
                    Toast.makeText(RestaurantDetailActivity.this,"Fail to open Google Map for navigation.",Toast.LENGTH_SHORT).show();
                }
            }
        });



        RDphoneNumTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(RestaurantDetailActivity.this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted, request it
                    ActivityCompat.requestPermissions(RestaurantDetailActivity.this, new String[]{android.Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
                } else {
                    // Permission is already granted, proceed with opening WhatsApp
                    openWhatsApp();
                }

            }
        });

        RDwebsiteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the URL of the website
                String websiteUrl = website;

                try {
                    // Check if the website URL is not empty or equal to "-"
                    if (websiteUrl != null && !websiteUrl.equals("-")) {
                        // Create an Intent to open the web browser with the website URL
                        Uri webPage = Uri.parse(websiteUrl);
                        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);

                        // Check if a web browser is available on the device
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent); // Open the web browser
                        } else {
                            // If no web browser is available, show a message to the user
                            Toast.makeText(RestaurantDetailActivity.this, "No web browser found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle the case where the website URL is empty or "-"
                        // Show a message to the user indicating that there is no website available
                        Toast.makeText(RestaurantDetailActivity.this, "No website available for this restaurant.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    // Handle any exceptions that may occur
                    Log.e("Website Error", "Failed to open website: " + e.getMessage());
                    Toast.makeText(RestaurantDetailActivity.this, "Failed to open website.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        wishlistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isAddedWishlist){
                    isAddedWishlist = false;
                    wishlistBtn.setForeground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.addwishlist_icon));
                    //wishlistBtn.setImageResource(R.drawable.addwishlist_icon);
                    resSQLiteAdapter.openToWrite();
                    resSQLiteAdapter.deleteRow(placeID);
                } else{
                    isAddedWishlist = true;
                    wishlistBtn.setForeground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.filled_wishlist_icon));
                    //wishlistBtn.setImageResource(R.drawable.filled_wishlist_icon);
                    resSQLiteAdapter.openToWrite();
                    resSQLiteAdapter.insert(placeID,wishlistName,photoReference,wishlistOpeningNow,wishlistRating,wishlistDistance,wishlistPriceLevel,wishlistUserRatingTotal);
                }
                resSQLiteAdapter.close();

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, proceed with opening WhatsApp
                openWhatsApp();
            } else {
                // Permission denied, handle it (e.g., show a message to the user)
                Toast.makeText(this, "Permission denied. You cannot open WhatsApp.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openWhatsApp() {
        try {
            // Format the phone number properly for WhatsApp
            String formattedPhoneNumber = phoneNum.replace(" ", "").replace("-", "");
            if (!formattedPhoneNumber.startsWith("+")) {
                // Add the country code if it's missing
                formattedPhoneNumber = "+60" + formattedPhoneNumber;
            }

            // Create a Uri for opening WhatsApp with the formatted phone number
            Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=" + formattedPhoneNumber);

            // Create an Intent to open WhatsApp with the specified phone number
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);

            // Check if WhatsApp is installed on the device
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent); // Open WhatsApp
            } else {
                // If WhatsApp is not installed, you can handle this case here
                // For example, you can show a message or prompt the user to install it
                Toast.makeText(RestaurantDetailActivity.this, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // Handle any exceptions that may occur
            Log.e("WhatsApp Error", "Failed to open WhatsApp: " + e.getMessage());
            Toast.makeText(RestaurantDetailActivity.this, "Failed to open WhatsApp.", Toast.LENGTH_SHORT).show();
        }
    }


    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new
                    ByteArrayOutputStream();
            int i = is.read();
            while (i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    private String getPhotoUrl(String photoReference) {
        // Construct the URL using your API key and the photo reference
        String apiKey = "AIzaSyC1n_2xoJGwMsbKK43o-hYAnGdbEjpZq1w";
        return "https://maps.googleapis.com/maps/api/place/photo"+
                "?maxwidth=400"+
                "&photo_reference=" + photoReference +
                "&key=" + apiKey;
    }
}