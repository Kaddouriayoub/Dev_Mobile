package com.example.myapplication.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Favorite;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.utils.SessionManager;

import io.realm.Realm;
import io.realm.RealmResults;

public class ClientWorkspaceAdapter
        extends RecyclerView.Adapter<ClientWorkspaceAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(long workspaceId);
    }

//    private static final long CLIENT_ID = 0L;

    private RealmResults<Workspace> workspaces;
    private OnItemClickListener listener;
    private Context context;
    private SessionManager sessionManager;

    public ClientWorkspaceAdapter(
            Context context,
            RealmResults<Workspace> workspaces,
            OnItemClickListener listener
    ) {
        this.workspaces = workspaces;
        this.listener = listener;
        this.context = context;
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
        sessionManager = new SessionManager(context);
        if (workspace == null) return;

        holder.tvLocation.setText(
                workspace.getName() + " · " + workspace.getCity()
        );
        holder.tvDescription.setText(workspace.getDescription());
        holder.tvPrice.setText(workspace.getPricePerHour() + " DHS/h");

        // STATUS
        String status = workspace.getStatus();
        holder.tvStatus.setText(status);
        holder.tvStatus.setTextColor(Color.WHITE);

        switch (status) {
            case "AVAILABLE":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#2E7D32"));
                break;
            case "FULL":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#C62828"));
                break;
            case "MAINTENANCE":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#F9A825"));
                break;
            default:
                holder.tvStatus.setBackgroundColor(Color.GRAY);
        }

        Realm realm = Realm.getDefaultInstance();

        // Favorite state
        Favorite favorite = realm.where(Favorite.class)
                .equalTo("clientId", sessionManager.getUserId())
                .equalTo("workspaceId", workspace.getId())
                .findFirst();

        holder.ivFavorite.setSelected(favorite != null);

        // Toggle favorite
        holder.ivFavorite.setOnClickListener(v -> {
            realm.executeTransaction(r -> {

                Favorite existing = r.where(Favorite.class)
                        .equalTo("clientId", sessionManager.getUserId())
                        .equalTo("workspaceId", workspace.getId())
                        .findFirst();

                if (existing != null) {
                    existing.deleteFromRealm();
                    holder.ivFavorite.setSelected(false);
                } else {
                    Number maxId = r.where(Favorite.class).max("id");
                    long nextId = (maxId == null) ? 1 : maxId.longValue() + 1;

                    Favorite f = r.createObject(Favorite.class, nextId);
                    f.setClientId(sessionManager.getUserId());
                    f.setWorkspaceId(workspace.getId());
                    f.setCreatedAt(String.valueOf(System.currentTimeMillis()));

                    holder.ivFavorite.setSelected(true);
                }
            });
        });

        holder.itemView.setOnClickListener(v ->
                listener.onItemClick(workspace.getId())
        );

        realm.close();
    }

    @Override
    public int getItemCount() {
        return workspaces != null ? workspaces.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivSpace, ivFavorite;
        TextView tvLocation, tvDescription, tvPrice, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSpace = itemView.findViewById(R.id.ivSpace);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
