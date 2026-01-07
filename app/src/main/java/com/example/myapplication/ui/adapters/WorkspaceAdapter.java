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
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Workspace;
import io.realm.RealmList;
import io.realm.RealmResults;
import java.io.File;
import java.io.InputStream;

public class WorkspaceAdapter extends RecyclerView.Adapter<WorkspaceAdapter.ViewHolder> {

    private Context context;
    private RealmResults<Workspace> workspaces;
    private RealmResults<Workspace> allWorkspaces;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Workspace workspace);
        void onEditClick(Workspace workspace);
    }

    public WorkspaceAdapter(Context context, RealmResults<Workspace> workspaces, OnItemClickListener listener) {
        this.context = context;
        this.workspaces = workspaces;
        this.allWorkspaces = workspaces;
        this.listener = listener;
    }

    public void filter(String query) {
        if (query == null || query.isEmpty()) {
            workspaces = allWorkspaces;
        } else {
            workspaces = allWorkspaces.where()
                    .contains("name", query, io.realm.Case.INSENSITIVE)
                    .or()
                    .contains("city", query, io.realm.Case.INSENSITIVE)
                    .or()
                    .contains("status", query, io.realm.Case.INSENSITIVE)
                    .findAll();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_workspace, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Workspace workspace = workspaces.get(position);
        if (workspace != null) {
            holder.tvName.setText(workspace.getName());
            holder.tvLocation.setText(workspace.getCity());
            holder.tvPrice.setText(String.format("$%.2f / day", workspace.getPricePerHour() * 8));

            // Set status with color
            String status = workspace.getStatus();
            if (status != null) {
                holder.tvStatus.setText(status);
                switch (status.toUpperCase()) {
                    case "AVAILABLE":
                        holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                        break;
                    case "FULL":
                        holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                        break;
                    case "MAINTENANCE":
                        holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                        break;
                    default:
                        holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                }
            } else {
                holder.tvStatus.setText("N/A");
            }

            // Set capacity (available / total)
            int available = workspace.getAvailablePlaces();
            int total = workspace.getCapacity();
            holder.tvCapacity.setText(available + "/" + total + " places");

            // Color code based on availability
            if (available == 0) {
                holder.tvCapacity.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else if (available <= total * 0.2) {
                holder.tvCapacity.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                holder.tvCapacity.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            }

            // Load first image if available
            RealmList<String> images = workspace.getImages();
            if (images != null && !images.isEmpty()) {
                String firstImageUri = images.first();
                if (firstImageUri != null && !firstImageUri.isEmpty()) {
                    loadImage(holder.imgWorkspace, firstImageUri);
                } else {
                    holder.imgWorkspace.setImageResource(R.drawable.ic_launcher_background);
                }
            } else {
                holder.imgWorkspace.setImageResource(R.drawable.ic_launcher_background);
            }

            holder.btnEdit.setOnClickListener(v -> listener.onEditClick(workspace));

            // Item click for preview
            holder.itemView.setOnClickListener(v -> listener.onItemClick(workspace));
        }
    }

    @Override
    public int getItemCount() {
        return workspaces != null ? workspaces.size() : 0;
    }

    private void loadImage(ImageView imageView, String imagePath) {
        try {
            Log.d("WorkspaceAdapter", "Loading image: " + imagePath);

            // Check if it's an absolute file path (starts with /)
            if (imagePath.startsWith("/")) {
                File file = new File(imagePath);
                if (file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        Log.d("WorkspaceAdapter", "Loaded successfully from: " + imagePath);
                        return;
                    }
                }
                Log.e("WorkspaceAdapter", "File not found: " + imagePath);
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

            // Fallback
            imageView.setImageResource(R.drawable.ic_launcher_background);
        } catch (Exception e) {
            Log.e("WorkspaceAdapter", "Error loading image: " + e.getMessage());
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgWorkspace, btnEdit;
        TextView tvName, tvLocation, tvPrice, tvStatus, tvCapacity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgWorkspace = itemView.findViewById(R.id.img_workspace);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            tvName = itemView.findViewById(R.id.tv_workspace_name);
            tvLocation = itemView.findViewById(R.id.tv_workspace_location);
            tvPrice = itemView.findViewById(R.id.tv_workspace_price);
            tvStatus = itemView.findViewById(R.id.tv_workspace_status);
            tvCapacity = itemView.findViewById(R.id.tv_workspace_capacity);
        }
    }
}
