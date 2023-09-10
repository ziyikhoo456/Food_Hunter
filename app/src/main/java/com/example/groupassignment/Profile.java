package com.example.groupassignment;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

//Done by Kuek Gavin
public class Profile extends AppCompatActivity {

    private ImageButton backButton, settingButton;
    private TextView username, emailTextView;
    private CircleImageView profileImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        backButton = findViewById(R.id.backButton);
        settingButton = findViewById(R.id.dehazeButton);
        username = findViewById(R.id.usernameTitle);
        emailTextView = findViewById(R.id.emailTitle);
        profileImg = findViewById(R.id.profileImg);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(Profile.this, RestaurantListActivity.class);
                startActivity(backIntent);
            }
        });

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingIntent =new Intent(Profile.this, EditProfile.class);
                startActivity(settingIntent);
            }
        });

        // Retrieve user profile details from Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            // Set the user's name and email in the TextViews
            username.setText(displayName);
            emailTextView.setText(email);

            // Retrieve and display the profile picture (if available)
            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                String profileImageUrl = photoUrl.toString();
                Picasso.get()
                        .load(profileImageUrl)
                        .placeholder(R.drawable.profile_pic)
                        .error(R.drawable.profile_pic)
                        .into(profileImg, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                // Image loaded successfully
                                Log.d("Profile", "Profile image loaded successfully");
                            }

                            @Override
                            public void onError(Exception e) {
                                // Image loading failed
                                Log.e("Profile", "Error loading profile image: " + e.getMessage());
                            }
                        });
            }
        }
    }
}