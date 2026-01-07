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

    private RealmResults<Workspace> workspaces;

    public ClientWorkspaceAdapter(RealmResults<Workspace> workspaces) {
        this.workspaces = workspaces;
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

        holder.tvLocation.setText(
                workspace.getType().name() + " · " + workspace.getCity()
        );

        holder.tvDescription.setText(workspace.getDescription());

        holder.tvPrice.setText(
                workspace.getPricePerHour() + " $ / hour"
        );

        // Favorite click (later)
        holder.ivFavorite.setOnClickListener(v -> {
            // TODO: add/remove favorite
        });
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
