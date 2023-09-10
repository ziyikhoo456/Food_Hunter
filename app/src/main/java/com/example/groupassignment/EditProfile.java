package com.example.groupassignment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.UUID;

//Done by Kuek Gavin
public class EditProfile extends AppCompatActivity {

    private ImageView profileImg;
    private ImageButton backButton, logOutButton;
    private Button profileImgButton, saveButton;
    private TextView editUsername, editPassword, editEmail;
    private SwitchCompat modeSwitch;
    private boolean nightMode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private DatabaseReference userRef;
    private FirebaseUser currentUser;
    private StorageReference storageReference;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        backButton = findViewById(R.id.backButton);
        logOutButton = findViewById(R.id.logOutButton);
        profileImg = findViewById(R.id.profileImg);
        profileImgButton = findViewById(R.id.profileImgButton);
        editUsername = findViewById(R.id.username);
        editPassword = findViewById(R.id.password);
        editEmail = findViewById(R.id.email);
        modeSwitch = findViewById(R.id.modeSwitch);
        saveButton = findViewById(R.id.saveButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(EditProfile.this, Profile.class);
                startActivity(backIntent);
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent logOutIntent = new Intent(EditProfile.this, LoginActivity.class);
                startActivity(logOutIntent);
            }
        });

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // Handle the case where the user is not authenticated
            // You can show a message or redirect to the login screen
            // For now, I'm just printing a log message.
            Log.d("EditProfile", "User not authenticated");
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("user").child(currentUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference();

        // Set the current user's existing details in the EditText fields
        editUsername.setText(currentUser.getDisplayName());
        editEmail.setText(currentUser.getEmail());

        // Handle profile image upload
        profileImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the image picker
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 1);
            }
        });

        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("nightMode", false);

        if (nightMode){
            modeSwitch.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        modeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nightMode){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("nightMode", false);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("nightMode", true);
                }
                editor.apply();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges();
            }
        });
    }

    private void saveChanges() {
        // Get edited values from EditText fields
        String newUsername = editUsername.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();
        String newPassword = editPassword.getText().toString().trim();

        // Update username if changed
        if (!newUsername.isEmpty()) {
            currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername)
                    .build());

            // Update username in the Firebase Realtime Database
            userRef.child("username").setValue(newUsername);
        }

        // Update email if changed
        if (!newEmail.isEmpty()) {
            currentUser.updateEmail(newEmail);

            // Update email in the Firebase Realtime Database
            userRef.child("email").setValue(newEmail);
        }

        // Update password if changed
        if (!newPassword.isEmpty()) {
            currentUser.updatePassword(newPassword);
        }

        // Upload and update profile image if selected
        if (selectedImageUri != null) {
            // Generate a unique filename for the image
            String filename = UUID.randomUUID().toString();
            StorageReference imageRef = storageReference.child("profile_images/" + filename);

            // Upload the image to Firebase Storage
            imageRef.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        // Get the URL of the uploaded image
                        imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();

                                    // Update the user's profile image URL in Firebase Authentication
                                    currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(downloadUri)
                                            .build());

                                    // Update the profile image URL in the Firebase Realtime Database
                                    userRef.child("profileImage").setValue(downloadUri.toString());

                                    // Display the updated profile image using Picasso
                                    Picasso.get()
                                            .load(downloadUri)
                                            .placeholder(R.drawable.profile_pic)
                                            .error(R.drawable.profile_pic)
                                            .into(profileImg);
                                }
                            }
                        });
                    }
                }
            });
        }
        // Finish the activity after saving changes
        finish();
    }

    // Handle the result of selecting an image from the gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profileImg.setImageURI(selectedImageUri);
        }
    }
}