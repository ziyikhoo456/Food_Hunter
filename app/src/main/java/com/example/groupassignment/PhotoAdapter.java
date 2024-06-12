package com.example.groupassignment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

//Done by Khoo Zi Yi
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>{

    private List<String> photoReferenceList;

    public PhotoAdapter(List<String> photoReferenceList){
        this.photoReferenceList = photoReferenceList;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_restaurant, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String photoReference = photoReferenceList.get(position);
        // Load and display the photo using Glide
        if(photoReference != null){
            String photoUrl = getPhotoUrl(photoReference);
            Glide.with(holder.itemView.getContext())
                    .load(photoUrl)
                    .into(holder.resPhotoImageView);
        }
    }

    private String getPhotoUrl(String photoReference) {
        // Construct the URL using your API key and the photo reference
        String apiKey = "PLACES_API_KEY";
        return "https://maps.googleapis.com/maps/api/place/photo"+
                "?maxwidth=500"+
                "&photo_reference=" + photoReference +
                "&key=" + apiKey;
    }

    @Override
    public int getItemCount() {
        return photoReferenceList.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView resPhotoImageView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            resPhotoImageView = itemView.findViewById(R.id.ResPhotoImageView);
        }
    }
}
