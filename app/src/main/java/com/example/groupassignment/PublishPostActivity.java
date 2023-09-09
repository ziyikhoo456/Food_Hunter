package com.example.groupassignment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.Nullable;

public class PublishPostActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MY_PERMISSION_REQUEST_CODE = 1001;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    FirebaseAuth firebaseAuth;

    EditText addDesc;
    ImageView addImage;
    ProgressBar progressBar;
    Button addPost;
    Uri selectedImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_post);

        addDesc = findViewById(R.id.editTextCaption);
        addImage = findViewById(R.id.addImage);
        progressBar = findViewById(R.id.progressBar);
        addPost = findViewById(R.id.buttonPost);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("post");
        storageReference = FirebaseStorage.getInstance().getReference();

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishPost();
            }
        });

        // Check if the permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it from the user
            requestPermission();
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, choose an image
                chooseImage();
            } else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "We need this permission to access your photos.", Toast.LENGTH_LONG).show();
                    requestPermission();
                } else {
                    Toast.makeText(this, "Permission is denied. You can grant it from App Settings.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void chooseImage() {
        // Open image chooser intent
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            // An image is selected, get the URI and display it
            selectedImageUri = data.getData();
            addImage.setImageURI(selectedImageUri);
        }
    }

    private void publishPost() {
        // Get current user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not signed in, handle accordingly
            return;
        }

        String userId = currentUser.getUid();

        //Generate unique Key
        String postId = databaseReference.push().getKey();

        // Get caption and image URL
        String caption = addDesc.getText().toString();
        String imageUrl = "";

        // Check if image is selected
        if (selectedImageUri != null) {
            // upload image to Firebase Storage and get the image URL
            uploadImageToStorage(selectedImageUri, postId);
        } else {
            // No image selected, proceed to publish with caption only
            createAndPublishPost(userId, postId, caption, imageUrl);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadImageToStorage(Uri selectedImageUri, String postId) {
        // Create a reference to the Firebase Storage location
        StorageReference imageRef = storageReference.child(String.valueOf(System.currentTimeMillis())).child(postId + "." + getFileExtension(selectedImageUri));

        // Upload the image to Firebase Storage
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Image uploaded successfully, get download URL
                        imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    String imageUrl = downloadUri.toString();
                                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                    if (currentUser != null) {
                                        String userId = currentUser.getUid();
                                        String caption = addDesc.getText().toString();
                                        createAndPublishPost(userId, postId, caption, imageUrl);
                                    }
                                } else {
                                    Toast.makeText(PublishPostActivity.this, "Failed to get image URL.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(PublishPostActivity.this,
                                "Image upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void createAndPublishPost(String userId, String postId, String caption, String imageUrl) {
        DataClass dataClass = new DataClass();
        dataClass.setDataDesc(caption);
        dataClass.setDataImage(imageUrl);

        // Store the username along with the post data
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){
            String username = currentUser.getDisplayName();
            dataClass.setUsername(username);
        }

        // Upload post to Firebase Realtime Database
        databaseReference.child(postId).setValue(dataClass);

        // Clear the input fields and reset selectedImageUri
        addDesc.setText("");
        addImage.setImageResource(android.R.color.transparent);
        selectedImageUri = null;

        // Show a success message
        Toast.makeText(PublishPostActivity.this, "Post published successfully", Toast.LENGTH_SHORT).show();
    }

}