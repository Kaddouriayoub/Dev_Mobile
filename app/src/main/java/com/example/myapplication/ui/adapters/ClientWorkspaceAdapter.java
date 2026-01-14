package com.example.myapplication.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
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

import java.io.File;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class ClientWorkspaceAdapter extends RecyclerView.Adapter<ClientWorkspaceAdapter.VH> {

    public interface OnWorkspaceClick {
        void onClick(long workspaceId);
    }

    private final Context context;
    private final RealmResults<Workspace> data;
    private final OnWorkspaceClick listener;

    public ClientWorkspaceAdapter(Context context, RealmResults<Workspace> data, OnWorkspaceClick listener) {
        this.context = context;
        this.data = data;
        this.listener = listener;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coworking_space, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Workspace w = data.get(position);
        if (w == null) return;

        // =========================================================
        // ✅ SIMPLE FIX: SET THE PHOTO (first image) INTO ivSpace
        // =========================================================
        String firstPath = null;
        List<String> imgs = w.getImages();
        if (imgs != null && !imgs.isEmpty()) firstPath = imgs.get(0);

        if (!TextUtils.isEmpty(firstPath) && new File(firstPath).exists()) {
            h.ivSpace.setImageBitmap(decodeScaled(firstPath, 1080, 600));
        } else {
            h.ivSpace.setImageResource(R.drawable.sample_office);
        }

        // ===== TEXT (keep your existing mapping) =====
        String name = w.getName();
        if (TextUtils.isEmpty(name)) name = "Workspace";

        String city = w.getCity();
        if (TextUtils.isEmpty(city)) city = "";

        h.tvLocation.setText(name + (TextUtils.isEmpty(city) ? "" : " · " + city));
        h.tvDescription.setText(w.getDescription());

        String status = w.getStatus();
        h.tvStatus.setText(status);
        h.tvStatus.setTextColor(Color.WHITE);

        if ("AVAILABLE".equals(status)) {
            h.tvStatus.setBackgroundColor(Color.parseColor("#2E7D32"));
        } else if ("FULL".equals(status)) {
            h.tvStatus.setBackgroundColor(Color.parseColor("#C62828"));
        } else if ("MAINTENANCE".equals(status)) {
            h.tvStatus.setBackgroundColor(Color.parseColor("#F9A825"));
        } else {
            h.tvStatus.setBackgroundColor(Color.GRAY);
        }

        h.tvPrice.setText(w.getPricePerHour() + " DHS/h");

        // ===== FAVORITE STATE (optional but usually already there) =====
        SessionManager sessionManager = new SessionManager(h.itemView.getContext());
        Realm realm = Realm.getDefaultInstance();

        Favorite existingFav = realm.where(Favorite.class)
                .equalTo("clientId", sessionManager.getUserId())
                .equalTo("workspaceId", w.getId())
                .findFirst();

        h.ivFavorite.setSelected(existingFav != null);

        // If you already had favorite toggle here, keep it.
        h.ivFavorite.setOnClickListener(v -> {
            realm.executeTransaction(r -> {
                Favorite existing = r.where(Favorite.class)
                        .equalTo("clientId", sessionManager.getUserId())
                        .equalTo("workspaceId", w.getId())
                        .findFirst();

                if (existing != null) {
                    existing.deleteFromRealm();
                    h.ivFavorite.setSelected(false);
                } else {
                    Number maxId = r.where(Favorite.class).max("id");
                    long nextId = (maxId == null) ? 1 : maxId.longValue() + 1;

                    Favorite f = r.createObject(Favorite.class, nextId);
                    f.setClientId(sessionManager.getUserId());
                    f.setWorkspaceId(w.getId());
                    f.setCreatedAt(String.valueOf(System.currentTimeMillis()));

                    h.ivFavorite.setSelected(true);
                }
            });
        });

        // ===== OPEN DETAILS =====
        h.itemView.setOnClickListener(v -> listener.onClick(w.getId()));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public long getItemId(int position) {
        Workspace w = data.get(position);
        return (w != null) ? w.getId() : position;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivSpace, ivFavorite;
        TextView tvLocation, tvDescription, tvStatus, tvPrice;

        VH(@NonNull View itemView) {
            super(itemView);
            ivSpace = itemView.findViewById(R.id.ivSpace);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }

    // Small helper to avoid OOM when loading photos
    private Bitmap decodeScaled(String path, int reqW, int reqH) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, o);

        int inSampleSize = 1;
        while ((o.outWidth / inSampleSize) > reqW || (o.outHeight / inSampleSize) > reqH) {
            inSampleSize *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = inSampleSize;
        return BitmapFactory.decodeFile(path, o2);
    }
}
