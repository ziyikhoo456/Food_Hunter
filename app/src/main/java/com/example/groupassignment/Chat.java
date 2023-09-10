package com.example.groupassignment;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

//Done by Kuek Gavin
public class Chat extends AppCompatActivity {

    private ImageButton backButton;
    private Button searchButton;

    private ListView userListView;
    private ArrayAdapter<String> adapter;
    private List<String> userList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        backButton = findViewById(R.id.backButton);
        searchButton = findViewById(R.id.search_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(Chat.this, CommunityActivity.class);
                startActivity(backIntent);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchIntent = new Intent(Chat.this, Search.class);
                startActivity(searchIntent);
            }
        });

        // Initialize UI components
        userListView = findViewById(R.id.userListView);

        // Initialize Firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference("user");

        // Initialize user list and adapter
        userList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        userListView.setAdapter(adapter);

        // Load user list
        loadUserList();

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Get the selected user's username (or any other required data)
                String selectedUsername = userList.get(position);

                // Assuming you have a way to get the recipient's UID and profile image URL
                getRecipientUidAndProfileImage(selectedUsername);
            }
        });
    }

    private void loadUserList() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String id = userSnapshot.child("userId").getValue(String.class);
                    String username = userSnapshot.child("username").getValue(String.class);
                    if (id == null) {
                        userList.add(username);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    // Define a method to get the recipient's UID and profile image URL
    private void getRecipientUidAndProfileImage(String selectedUsername) {
        Query userQuery = usersRef.orderByChild("username").equalTo(selectedUsername);

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String recipientUid = null;
                String recipientProfileImgUrl = null;

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    recipientUid = userSnapshot.getKey();
                    recipientProfileImgUrl = userSnapshot.child("profileImg").getValue(String.class);
                    break; // Exit the loop after finding the first matching user
                }

                if (recipientUid != null) {
                    startChattingPageActivity(recipientUid, selectedUsername, recipientProfileImgUrl);
                } else {
                    // Handle the case where no user with the specified username is found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    // Add this method to start the ChattingPage activity
    private void startChattingPageActivity(String recipientUid, String username, String recipientProfileImgUrl) {
        Intent chattingIntent = new Intent(Chat.this, ChattingPage.class);
        chattingIntent.putExtra("recipientUserId", recipientUid); // Pass recipient's UID
        chattingIntent.putExtra("recipientUsername", username); // Pass recipient's username
        chattingIntent.putExtra("recipientProfileImgUrl", recipientProfileImgUrl); // Pass recipient's profile image URL
        startActivity(chattingIntent);
    }

    private void userQueryCallback(DataSnapshot dataSnapshot, String selectedUsername) {
        String recipientUid = null;
        String recipientProfileImgUrl = null;

        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
            recipientUid = userSnapshot.getKey();
            recipientProfileImgUrl = userSnapshot.child("profileImg").getValue(String.class);
            break; // Exit the loop after finding the first matching user
        }

        if (recipientUid != null) {
            startChattingPageActivity(recipientUid, selectedUsername, recipientProfileImgUrl);
        } else {
            // Handle the case where no user with the specified username is found
        }
    }

    // Define the method to get the recipient's UID
    private void getRecipientUid(String selectedUsername) {
        DatabaseReference userQuery = (DatabaseReference) usersRef.orderByChild("username").equalTo(selectedUsername);

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userQueryCallback(dataSnapshot, selectedUsername);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    // Define the method to get the recipient's profile image URL
    private void getRecipientProfileImgUrl(String selectedUsername) {
        DatabaseReference userQuery = (DatabaseReference) usersRef.orderByChild("username").equalTo(selectedUsername);

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userQueryCallback(dataSnapshot, selectedUsername);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}