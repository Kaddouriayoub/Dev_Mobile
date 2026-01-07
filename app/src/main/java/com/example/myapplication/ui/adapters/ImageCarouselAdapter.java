package com.example.myapplication.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.io.File;
import java.io.InputStream;

import io.realm.RealmList;

public class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.ImageViewHolder> {

    private Context context;
    private RealmList<String> images;

    public ImageCarouselAdapter(Context context, RealmList<String> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carousel_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUri = images.get(position);
        if (imageUri != null && !imageUri.isEmpty()) {
            loadImage(holder.imageView, imageUri);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    private void loadImage(ImageView imageView, String imagePath) {
        try {
            Log.d("ImageCarousel", "Loading: " + imagePath);

            // Check if it's an absolute file path (starts with /)
            if (imagePath.startsWith("/")) {
                File file = new File(imagePath);
                if (file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        return;
                    }
                }
            } else {
                // It's a URI
                Uri uri = Uri.parse(imagePath);

                if ("file".equals(uri.getScheme())) {
                    File file = new File(uri.getPath());
                    if (file.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            return;
                        }
                    }
                } else if ("content".equals(uri.getScheme())) {
                    InputStream inputStream = context.getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            return;
                        }
                    }
                }
            }

            imageView.setImageResource(R.drawable.ic_launcher_background);
        } catch (Exception e) {
            Log.e("ImageCarousel", "Error loading image: " + e.getMessage());
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.carousel_image);
        }
    }
}
