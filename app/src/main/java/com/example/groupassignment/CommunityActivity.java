package com.example.groupassignment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

//Done by Chok Xin Yi
public class CommunityActivity extends AppCompatActivity {

    FloatingActionButton addPostButton;
    DatabaseReference databaseReference;
    Toolbar toolbar;

    RecyclerView recyclerView;
    DataClassAdapter adapter;
    List<DataClass> postList;
    List<String> postIds;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Change the database reference to the user's posts
        // Change the database reference to fetch posts from all users
        databaseReference = FirebaseDatabase.getInstance().getReference("post");

        addPostButton = findViewById(R.id.add_post_button);
        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CommunityActivity.this, PublishPostActivity.class);
                startActivity(intent);
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postIds = new ArrayList<>(); // Initialize the list of postIds
        adapter = new DataClassAdapter(this, postList, postIds);
        recyclerView.setAdapter(adapter);

        // Use userId to filter posts
        retrieveAndDisplayPosts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.community_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(CommunityActivity.this, Chat.class);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    private void retrieveAndDisplayPosts() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (postList != null && postIds != null) {
                    postList.clear(); // Clear previous posts
                    postIds.clear(); // Clear previous postIds

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        DataClass dataclass = dataSnapshot.getValue(DataClass.class);
                        if (dataclass != null) {
                            // Add retrieved post to the list
                            postList.add(0,dataclass);

                            // Get the postId for this post and add it to the list
                            postIds.add(0, dataSnapshot.getKey());
                        }

                    }
                    adapter.notifyDataSetChanged(); // Notify the adapter that data has changed
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle database error
                Toast.makeText(CommunityActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
