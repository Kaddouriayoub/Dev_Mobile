package com.example.myapplication.ui.adapters;

import android.content.Context;
import android.net.Uri;
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

public class WorkspaceAdapter extends RecyclerView.Adapter<WorkspaceAdapter.ViewHolder> {

    private Context context;
    private RealmResults<Workspace> workspaces;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(Workspace workspace);
    }

    public WorkspaceAdapter(Context context, RealmResults<Workspace> workspaces, OnItemClickListener listener) {
        this.context = context;
        this.workspaces = workspaces;
        this.listener = listener;
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

            // Load first image if available
            RealmList<String> images = workspace.getImages();
            if (images != null && !images.isEmpty()) {
                String firstImageUri = images.first();
                if (firstImageUri != null && !firstImageUri.isEmpty()) {
                    try {
                        holder.imgWorkspace.setImageURI(Uri.parse(firstImageUri));
                    } catch (Exception e) {
                        holder.imgWorkspace.setImageResource(R.drawable.ic_launcher_background);
                    }
                } else {
                    holder.imgWorkspace.setImageResource(R.drawable.ic_launcher_background);
                }
            } else {
                holder.imgWorkspace.setImageResource(R.drawable.ic_launcher_background);
            }

            holder.btnEdit.setOnClickListener(v -> listener.onEditClick(workspace));
        }
    }

    @Override
    public int getItemCount() {
        return workspaces != null ? workspaces.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgWorkspace, btnEdit;
        TextView tvName, tvLocation, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgWorkspace = itemView.findViewById(R.id.img_workspace);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            tvName = itemView.findViewById(R.id.tv_workspace_name);
            tvLocation = itemView.findViewById(R.id.tv_workspace_location);
            tvPrice = itemView.findViewById(R.id.tv_workspace_price);
        }
    }
}
