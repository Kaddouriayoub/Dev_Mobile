package com.example.myapplication.ui.adapters;

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

public class ClientWorkspaceAdapter
        extends RecyclerView.Adapter<ClientWorkspaceAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(long workspaceId);
    }

    private RealmResults<Workspace> workspaces;
    private OnItemClickListener listener;

    public ClientWorkspaceAdapter(
            RealmResults<Workspace> workspaces,
            OnItemClickListener listener
    ) {
        this.workspaces = workspaces;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coworking_space, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        Workspace workspace = workspaces.get(position);
        if (workspace == null) return;

        // ✅ TITLE = WORKSPACE NAME (FIX)
        holder.tvLocation.setText(
                workspace.getName() + " · " + workspace.getCity()
        );

        // Description
        holder.tvDescription.setText(workspace.getDescription());

        // Price
        holder.tvPrice.setText(workspace.getPricePerHour() + "€/h");

        // Click → details
        holder.itemView.setOnClickListener(v ->
                listener.onItemClick(workspace.getId())
        );
    }

    @Override
    public int getItemCount() {
        return workspaces != null ? workspaces.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivSpace, ivFavorite;
        TextView tvLocation, tvDescription, tvPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSpace = itemView.findViewById(R.id.ivSpace);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
