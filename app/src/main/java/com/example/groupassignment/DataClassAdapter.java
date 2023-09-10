package com.example.groupassignment;

import android.content.Context;
import android.content.DialogInterface;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Done by Chok Xin Yi
public class DataClassAdapter extends RecyclerView.Adapter<DataClassAdapter.ViewHolder> {

    private final Context context;
    private final List<DataClass> postList;
    private final List<String> postIds; // Store the postIds for each post
    private Map<String, List<CommentClass>> commentsMap;
    private List<CommentClass> commentsForPost;
    private CommentAdapter commentAdapter;

    public DataClassAdapter(Context context, List<DataClass> postList, List<String> postIds) {
        this.context = context;
        this.postList = postList;
        this.postIds = postIds; // Initialize the list of postIds
        this.commentsMap = new HashMap<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DataClass post = postList.get(position);
        String postId = postIds.get(position); // Get the postId for this post

        // Set the post caption and username
        holder.textViewCaption.setText(post.getDataDesc());
        holder.textViewUsername.setText(post.getUsername());

        // Load the post image using Glide (you may need to add Glide dependency to your build.gradle)
        Glide.with(context)
                .load(post.getDataImage())
                .into(holder.imageViewPost);

        // Set the like button icon based on whether the post is liked by the current user
        if(post.getLikedByUsernames().contains(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())){
            holder.likeButton.setBackgroundResource(R.drawable.like);
        }
        else{
            holder.likeButton.setBackgroundResource(R.drawable.dislike);
        }

        // Display the like count
        holder.textViewLikeCount.setText(String.valueOf(post.getLikes()));

        // Handle like button click
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLikeStatus(post, postId);
            }
        });

        // Handle comment button click
        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentDialog(post, postId, holder);
            }
        });

        String currentUsername = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if(currentUsername.equals(post.getUsername())){
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete Post");
                    builder.setMessage("Are you sure you want to delete this post?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deletePost(postId);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            });
        }
        else{
            holder.deleteButton.setVisibility(View.GONE);
        }

        if(!commentsMap.containsKey(postId)){
            fetchCommentsForPost(postId, holder);
        }
        else{
            List<CommentClass> commentsForPost = commentsMap.get(postId);
            CommentAdapter commentAdapter = new CommentAdapter(commentsForPost);
            holder.recyclerViewComments.setAdapter(commentAdapter);
        }
    }

    private void fetchCommentsForPost(String postId, ViewHolder holder) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("post")
                .child(postId)
                .child("comments");

        Log.i("CommentsRef", String.valueOf(commentsRef));

        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<CommentClass> commentsForPost = new ArrayList<>();
                for(DataSnapshot commentSnapshot : snapshot.getChildren()){
                    CommentClass comment = commentSnapshot.getValue(CommentClass.class);
                    if(comment != null){
                        commentsForPost.add(comment);
                        Log.i("Comments", String.valueOf(comment.getCommentText()));
                    }

                }
                commentsMap.put(postId, commentsForPost);

                CommentAdapter commentAdapter = new CommentAdapter(commentsForPost);
                holder.recyclerViewComments.setAdapter(commentAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseError", "Error: " + error.getMessage());

            }
        });
    }

    private void toggleLikeStatus(DataClass post, String postId) {
        String currentUsername = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        List<String> likedByUsernames = post.getLikedByUsernames();

        if (likedByUsernames.contains(currentUsername)) {
            likedByUsernames.remove(currentUsername);
            post.setLikes(post.getLikes() - 1);
        } else {
            likedByUsernames.add(currentUsername);
            post.setLikes(post.getLikes() + 1);
        }

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("post").child(postId);
        postRef.setValue(post);

        notifyDataSetChanged();
    }

    private void deletePost(String postId){
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference()
                .child("post").child(postId);

        // Remove the post from Firebase
        postRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCaption;
        TextView textViewUsername;
        ImageView imageViewPost;
        ImageButton likeButton;
        TextView textViewLikeCount;
        ImageButton commentButton;
        ImageButton deleteButton;
        RecyclerView recyclerViewComments;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewCaption = itemView.findViewById(R.id.textViewCaption);
            textViewUsername  = itemView.findViewById(R.id.textViewUsername);
            imageViewPost = itemView.findViewById(R.id.imageViewPost);
            likeButton = itemView.findViewById(R.id.likeButton);
            textViewLikeCount = itemView.findViewById(R.id.textViewLikeCount);
            commentButton = itemView.findViewById(R.id.commentButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            recyclerViewComments = itemView.findViewById(R.id.recyclerViewComments);
        }
    }

    private void showCommentDialog(DataClass post, String postId, ViewHolder holder){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_comment, null);
        builder.setView(dialogView);

        TextView textViewUsername = dialogView.findViewById(R.id.textViewUsername);
        EditText editTextComment = dialogView.findViewById(R.id.editTextComment);
        Button buttonSend = dialogView.findViewById(R.id.buttonSend);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        String currentUsername = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        textViewUsername.setText(currentUsername);

        AlertDialog dialog = builder.create();

        // Handle send button
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = editTextComment.getText().toString().trim();
                if(!commentText.isEmpty()){
                    // Save the comment to Firebase
                    DatabaseReference commentsRef = FirebaseDatabase.getInstance()
                            .getReference("post")
                            .child(postId)
                            .child("comments");

                    String commentId = commentsRef.push().getKey();

                    CommentClass comment = new CommentClass(commentText, currentUsername);
                    commentsRef.child(commentId).setValue(comment)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(context, "Comment Added", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                    else{
                                        Toast.makeText(context, "Failed to adad comment", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    //commentsForPost.clear();
                    //commentAdapter.notifyDataSetChanged();

                    fetchCommentsForPost(postId, holder);
                }
                else{
                    Toast.makeText(context, "Please enter a comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}