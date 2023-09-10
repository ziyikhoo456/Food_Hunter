package com.example.groupassignment;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignment.Model.Message;
import com.example.groupassignment.Adapter.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.List;

//Done by Kuek Gavin
public class ChattingPage extends AppCompatActivity {

    private Toolbar chatToolbar;
    private RecyclerView messageRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;

    private DatabaseReference messagesDatabaseRef;
    private FirebaseAuth auth;
    private String currentUserId;
    private String recipientUserId;

    private List<Message> messageList;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_page);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        messagesDatabaseRef = FirebaseDatabase.getInstance().getReference().child("messages");

        // Get the recipient's information from the intent
        recipientUserId = getIntent().getStringExtra("recipientUserId");
        String recipientUsername = getIntent().getStringExtra("recipientUsername");
        String recipientProfileImgUrl = getIntent().getStringExtra("recipientProfileImgUrl");

        // Initialize UI components
        chatToolbar = findViewById(R.id.chatToolbar);
        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        // Initialize currentUserId with the current user's ID
        currentUserId = auth.getCurrentUser().getUid();

        // Set up the RecyclerView for messages
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);

        // Set the adapter for the RecyclerView
        messageRecyclerView.setAdapter(messageAdapter);

        // Set the layout manager for the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Scrolls to the bottom
        messageRecyclerView.setLayoutManager(layoutManager);

        // Set the toolbar title to the recipient's username
        chatToolbar.setTitle(recipientUsername);

        // Load existing messages
        loadMessages();

        // Send a new message when the send button is clicked
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }


    private void loadMessages() {
        // TODO: Implement loading existing messages from the database

        // Example code to retrieve messages
        messagesDatabaseRef.child(currentUserId).child(recipientUserId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Message message = dataSnapshot.getValue(Message.class);
                        messageList.add(message);
                        messageAdapter.notifyDataSetChanged();
                        messageRecyclerView.scrollToPosition(messageList.size() - 1);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();

        if (!messageText.isEmpty()) {
            // Create a new message object
            Message message = new Message(messageText, currentUserId, ServerValue.TIMESTAMP);

            // Push the message to the database under both sender and recipient nodes
            messagesDatabaseRef.child(currentUserId).child(recipientUserId).push().setValue(message);
            messagesDatabaseRef.child(recipientUserId).child(currentUserId).push().setValue(message);

            // Clear the message input field
            messageEditText.setText("");
        }
    }
}