package com.example.groupassignment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private final List<CommentClass> commentList;

    public CommentAdapter( List<CommentClass> commentList) {
        this.commentList = commentList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CommentClass comment = commentList.get(position);
        holder.commentUsername.setText(comment.getCommenterUsername());
        holder.commentText.setText(comment.getCommentText());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView commentUsername;
        TextView commentText;

        public ViewHolder(View itemView) {
            super(itemView);
            commentUsername = itemView.findViewById(R.id.textViewCommentUsername);
            commentText = itemView.findViewById(R.id.textViewCommentText);
        }
    }
}
