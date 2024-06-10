package com.example.groupassignment;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;

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
public class RestaurantListActivity extends AppCompatActivity {
    private String apiKey;
    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;
    private final List<Restaurant> allRestaurants = new ArrayList<>();
    private String nextPageToken;
    private String TAG = "restaurant";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean isLoading = false;
    private Location userLocation;
    private int lastVisibleItemPosition = 0; // Variable to store the last visible item position
    private double resLat, resLong, userLatitude, userLongitude;
    private TextInputEditText searchEditText;
    private Button searchButton;




    // [START maps_solutions_android_permission_request]
    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    @SuppressLint("MissingPermission")
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                if (Boolean.TRUE.equals(isGranted.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        && Boolean.TRUE.equals(isGranted.get(ACCESS_WIFI_STATE))) {
                    try {
                        findCurrentPlaceWithPermissions();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // Fallback behavior if user denies permission
                    Log.d(TAG, "User denied permission");
                }
            });
    // [END maps_solutions_android_permission_request]

    // [START maps_solutions_android_location_permissions]
    @SuppressLint("MissingPermission")
    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                findCurrentPlaceWithPermissions();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE});
        }
    }
    // [END maps_solutions_android_location_permissions]

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        apiKey = getString(R.string.API_KEY);

        if (apiKey.equals("")) {
            Toast.makeText(this, "No API key found.", Toast.LENGTH_LONG).show();
            return;
        }

        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        PlacesClient placesClient = Places.createClient(this);

        //setupAutocompleteSupportFragment();
        checkLocationPermissions();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add a scroll listener to the RecyclerView to detect when the user scrolls to the end
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Check if the user has scrolled to the end of the list
                if (!isLoading && isLastItemVisible()) {
                    // Load more data using the next_page_token
                    Log.i("LoadMoreData","Starting to load more data.");
                    loadMoreData();
                }
            }
        });

        // Initialize the adapter only once
        adapter = new RestaurantAdapter(allRestaurants);
        recyclerView.setAdapter(adapter);

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);;

        // Set an action listener for the search button
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

        // Set an action listener for the keyboard's search action
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        Button autoCompleteButton = (Button) findViewById(R.id.autoCompleteButton);
        autoCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAutocompleteActivity();
            }
        });

        CheckBox displayOpeningCB = (CheckBox) findViewById(R.id.displayOpeningcheckBox);
        displayOpeningCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = null;

                // Clear the existing restaurant list
                allRestaurants.clear();

                // Notify the adapter that the data has changed
                runOnUiThread(() -> adapter.notifyDataSetChanged());

                if(displayOpeningCB.isChecked()){
                    //To connect to Google Web Services
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                            "?keyword=restaurant" +
                            "&location=" + userLocation.getLatitude() + "%2C" + userLocation.getLongitude() +
                            "&radius=1500" +
                            "&opennow=true" +
                            "&key="+apiKey;

                } else {
                    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                            "?keyword=restaurant" +
                            "&location=" + userLocation.getLatitude() + "%2C" + userLocation.getLongitude() +
                            "&radius=1500" +
                            "&key="+apiKey;
                }
                MyThread connectingThread = new MyThread(url);
                connectingThread.start();
            }
        });

        setLaunchActivityClickListener(R.id.wishlistBtn, WishlistActivity.class);
        setLaunchActivityClickListener(R.id.robotBtn, ChatbotActivity.class);
        setLaunchActivityClickListener(R.id.communityBtn, CommunityActivity.class);
        setLaunchActivityClickListener(R.id.profileBtn, Profile.class);

    }

    // Method to check if the last item in the RecyclerView is visible
    private boolean isLastItemVisible() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        int totalItemCount = layoutManager.getItemCount();

        // If the last visible item is the last item in the list, return true
        return lastVisibleItemPosition >= totalItemCount - 1;
    }

    private void loadMoreData(){
        if (nextPageToken != null) {
            // Set isLoading to true to prevent duplicate requests
            isLoading = true;

            // Create and start a new thread to fetch more data
            String url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?pagetoken=" + nextPageToken+
                    "&key="+apiKey;
            MyThread connectingThread = new MyThread(url);
            connectingThread.start();
        } else {
            Log.i("nextPageToken","Next Page Token is null!");
        }
    }

    private void setLaunchActivityClickListener(
            int onClickResId, Class<? extends AppCompatActivity> activityClassToLaunch) {
        findViewById(onClickResId)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(RestaurantListActivity.this, activityClassToLaunch);
                    startActivity(intent);
                });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    /**
     * Fetches a list of {@link PlaceLikelihood} instances that represent the Places the user is
     * most
     * likely to be at currently.
     */
    @RequiresPermission(allOf = {ACCESS_FINE_LOCATION, ACCESS_WIFI_STATE})
    private void findCurrentPlaceWithPermissions() throws JSONException {

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {

                    userLocation = task.getResult();
                    //To connect to Google Web Services
                    String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                            "?keyword=restaurant" +
                            "&location=" + userLocation.getLatitude() + "%2C" + userLocation.getLongitude() +
                            "&radius=1500" +
                            "&key="+apiKey;
                    MyThread connectingThread = new MyThread(url);
                    connectingThread.start();

                }
            }
        });

    }

    private void performSearch() {
        // Get the user's search input
        String searchQuery = searchEditText.getText().toString().trim();

        if (!searchQuery.isEmpty()) {
            // Perform the search and update the RecyclerView
            searchRestaurantsByName(searchQuery);
        } else {
            // Handle the case where the search query is empty
            Toast.makeText(this, "Please enter a restaurant name to search.", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchRestaurantsByName(String query) {
        // Construct the URL with the search query
        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json"
                + "?query=" + query
                + "&key=" + apiKey;


        // Clear the existing restaurant list
        allRestaurants.clear();

        // Notify the adapter that the data has changed
        runOnUiThread(() -> adapter.notifyDataSetChanged());

        // Create and start a new thread to fetch search results
        MyThread connectingThread = new MyThread(url);
        connectingThread.start();
    }


    private class MyThread extends Thread {
        private final String searchURL;

        public MyThread(String searchUrl) {
            this.searchURL = searchUrl;
        }

        public void run() {
            isLoading = true;

            try {
                URL url = new URL(searchURL);
                Thread.sleep(1000);
                HttpURLConnection hc = (HttpURLConnection) url.openConnection();

                InputStream input = hc.getInputStream();
                String response = readStream(input);

                //OK response code
                if (hc.getResponseCode() == 200) {
                    Log.i("Success", "Response: " + response);
                    List<Restaurant> restaurants = parseAndRetrieveRestaurants(response);
                    allRestaurants.addAll(restaurants);

                    // Update the adapter with all the results
                    runOnUiThread(() -> {

                        int previousSize = allRestaurants.size();
                        adapter.notifyItemRangeInserted(previousSize, restaurants.size());
                        isLoading = false;

                        // Scroll to the previous last visible item position
                        if (lastVisibleItemPosition > 0) {
                            recyclerView.smoothScrollToPosition(lastVisibleItemPosition);
                            Toast.makeText(RestaurantListActivity.this, "Additional restaurant information is loaded.", Toast.LENGTH_LONG).show();
                        }
                    });

                    //Check for next page token
                    nextPageToken = getNextPageToken(response);
                    if (nextPageToken != null) {Log.i("Page Token Get",nextPageToken);}


                }else{
                    Log.i("Error", "Response code: " + hc.getResponseCode());
                }

                input.close();

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
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

    private String getNextPageToken(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("next_page_token");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Restaurant> parseAndRetrieveRestaurants(String response) {
        List<Restaurant> restaurants = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray resultsArray = jsonObject.getJSONArray("results");

            for (int i = 0; i < resultsArray.length(); i++) {
                //Reset the value in photoReference
                String photoReference = null;

                //Get every restaurant's name, rating and place_id
                JSONObject result = resultsArray.getJSONObject(i);
                String name = result.getString("name");
                double rating = result.getDouble("rating");
                String placeID = result.getString("place_id");
                int priceLevel = result.optInt("price_level",-1);
                int userRatingTotal = result.optInt("user_ratings_total",0);
                JSONObject opening = result.optJSONObject("opening_hours");
                boolean openingNow = false;

                if(opening != null){
                    openingNow = opening.optBoolean("open_now");
                }

                // Get every restaurant's latitude and longitude
                JSONObject resLocation = result.getJSONObject("geometry").getJSONObject("location");
                resLat = resLocation.getDouble("lat");
                resLong = resLocation.getDouble("lng");
                userLatitude = userLocation.getLatitude();
                userLongitude = userLocation.getLongitude();


                //Calculate the distance between the user and the restaurant
                double distance = calculateDistance(userLatitude, userLongitude, resLat,resLong);

                //Get every restaurant's photo reference, remain null if no "photos" attribute
                try {
                    JSONArray photosArray = result.getJSONArray("photos");
                    if (photosArray.length() > 0) {
                        photoReference = photosArray.getJSONObject(0).getString("photo_reference") + "\n";
                    }
                } catch (JSONException e) {
                    // Handle the case where "photos" attribute is not present for this restaurant
                }

                Restaurant restaurant = new Restaurant(name,rating,placeID,photoReference, openingNow, distance, priceLevel, userRatingTotal);
                restaurants.add(restaurant);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return restaurants;
    }

    /*private void setupAutocompleteSupportFragment() {

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                //Start another activity to show place details.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                Intent intent = new Intent(MainActivity.this, RestaurantDetailActivity.class);
                intent.putExtra("PlaceID", place.getId()); // Pass the PlaceID to the next activity
                startActivity(intent);
            }
        });
    }*/

    private void startAutocompleteActivity() {
        List<Place.Field> placeFields = new ArrayList<>();
        placeFields.add(Place.Field.ID);
        placeFields.add(Place.Field.NAME);

        List<String> countries = new ArrayList<>();
        countries.add("MY");

        Intent autocompleteIntent =
                new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, placeFields)
                        .setCountries(countries)
                        .setHint("Search one restaurant")
                        .build(this);

        startActivityForResult(autocompleteIntent, 23487);
    }

    // This is the method that handles the result from the Autocomplete activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 23487) {
            if (resultCode == RESULT_OK) {
                // Get the selected place from the Autocomplete activity result
                Place place = Autocomplete.getPlaceFromIntent(data);

                // Retrieve the Place ID
                String placeId = place.getId();

                // Create a new Intent to start another activity
                Intent newIntent = new Intent(this, RestaurantDetailActivity.class);

                // Pass the Place ID and Name as extras to the new intent
                newIntent.putExtra("PlaceID", placeId);

                // Start the new activity with the provided data
                startActivity(newIntent);
            }
        }
    }

    private double calculateDistance(double userLat, double userLng, double restaurantLat, double restaurantLng) {
        final int R = 6371; // Radius of the Earth in kilometers

        // Convert latitude and longitude from degrees to radians
        double lat1 = Math.toRadians(userLat);
        double lon1 = Math.toRadians(userLng);
        double lat2 = Math.toRadians(restaurantLat);
        double lon2 = Math.toRadians(restaurantLng);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Distance in kilometers

        return Math.round(distance * 100.0) / 100.0;
    }
}
