package com.example.myapplication.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Workspace;
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
            holder.tvLocation.setText(workspace.getCity()); // Or getAddress()
            holder.tvPrice.setText(String.format("$%.2f / day", workspace.getPricePerHour() * 8)); // Assuming 8 hour day for display or just per hour

            // TODO: Load image using Glide/Picasso if available
            // holder.imgWorkspace.setImageResource(...);

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
