package com.example.myapplication.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Review;
import com.example.myapplication.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {

    private final RealmResults<Review> reviews;
    private final Realm realm;

    public ReviewAdapter(RealmResults<Review> reviews, Realm realm) {
        this.reviews = reviews;
        this.realm = realm;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Review r = reviews.get(position);
        if (r == null) return;

        // Get user name using clientId (as requested)
        String userName = "Utilisateur";
        if (r.getClientId() != null) {
            User u = realm.where(User.class)
                    .equalTo("id", r.getClientId())
                    .findFirst();
            if (u != null && u.getFullName() != null && !u.getFullName().isEmpty()) {
                userName = u.getFullName();
            }
        }

        h.txtUserName.setText(userName);
        h.rbStars.setRating(r.getRating());
        h.txtDate.setText(formatDate(r.getCreatedAt()));
        h.txtComment.setText(r.getComment() != null ? r.getComment() : "");
    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    private String formatDate(String createdAt) {
        // If createdAt is millis in String => show like Google Play
        try {
            long ms = Long.parseLong(createdAt);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            return sdf.format(new Date(ms));
        } catch (Exception e) {
            // Otherwise display as-is (fallback)
            return createdAt != null ? createdAt : "";
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtUserName, txtDate, txtComment;
        RatingBar rbStars;

        VH(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtComment = itemView.findViewById(R.id.txtComment);
            rbStars = itemView.findViewById(R.id.rbItemStars);
        }
    }
}
