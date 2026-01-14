package com.example.myapplication.ui.client.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.model.Favorite;
import com.example.myapplication.model.Review;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.ui.client.BookingActivity;

import io.realm.Realm;
import io.realm.RealmResults;

public class WorkspaceDetailsFragment extends Fragment {

    private static final String ARG_ID = "workspace_id";
    private static final long CLIENT_ID = 0L;

    public static WorkspaceDetailsFragment newInstance(long workspaceId) {
        Bundle args = new Bundle();
        args.putLong(ARG_ID, workspaceId);
        WorkspaceDetailsFragment fragment = new WorkspaceDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_workspace_details, container, false);

        long workspaceId = getArguments().getLong(ARG_ID);
        Realm realm = Realm.getDefaultInstance();

        Workspace w = realm.where(Workspace.class)
                .equalTo("id", workspaceId)
                .findFirst();

        RealmResults<Review> reviews = realm.where(Review.class)
                .equalTo("workspaceId", workspaceId)
                .findAll();

        int count = reviews.size();
        float avg = 0f;
        int[] dist = new int[5]; // index 0 => 1-star, index 4 => 5-star

        if (count > 0) {
            int sum = 0;
            for (Review r : reviews) {
                int rating = r.getRating();
                if (rating < 1) rating = 1;
                if (rating > 5) rating = 5;
                sum += rating;
                dist[rating - 1]++;
            }
            avg = (float) sum / count;
        }

        if (w != null) {
            ((TextView) view.findViewById(R.id.txtWorkspaceName)).setText(w.getName());
            ((TextView) view.findViewById(R.id.txtPrice)).setText(w.getPricePerHour() + "€/h");
            ((TextView) view.findViewById(R.id.txtAddress)).setText(w.getAddress());
            ((TextView) view.findViewById(R.id.txtStatus)).setText(w.getStatus());
            ((TextView) view.findViewById(R.id.txtDescription)).setText(w.getDescription());
        }

        // Rating header (Google Play style)
        TextView txtAvg = view.findViewById(R.id.txtAvgRating);
        TextView txtReviewCount = view.findViewById(R.id.txtReviewCount);
        RatingBar rbSmall = view.findViewById(R.id.ratingBarSmall);

        txtAvg.setText(String.format(java.util.Locale.getDefault(), "%.1f", avg));
        txtReviewCount.setText(count + " avis");
        rbSmall.setRating(avg);

        ProgressBar pb5 = view.findViewById(R.id.pb5);
        ProgressBar pb4 = view.findViewById(R.id.pb4);
        ProgressBar pb3 = view.findViewById(R.id.pb3);
        ProgressBar pb2 = view.findViewById(R.id.pb2);
        ProgressBar pb1 = view.findViewById(R.id.pb1);

        int max = Math.max(count, 1);
        pb5.setMax(max); pb5.setProgress(dist[4]);
        pb4.setMax(max); pb4.setProgress(dist[3]);
        pb3.setMax(max); pb3.setProgress(dist[2]);
        pb2.setMax(max); pb2.setProgress(dist[1]);
        pb1.setMax(max); pb1.setProgress(dist[0]);

        // Favorite
        ImageButton btnFavorite = view.findViewById(R.id.btnFavorite);

        Favorite existing = realm.where(Favorite.class)
                .equalTo("clientId", CLIENT_ID)
                .equalTo("workspaceId", workspaceId)
                .findFirst();

        btnFavorite.setSelected(existing != null);

        btnFavorite.setOnClickListener(v -> {
            realm.executeTransaction(r -> {
                Favorite fav = r.where(Favorite.class)
                        .equalTo("clientId", CLIENT_ID)
                        .equalTo("workspaceId", workspaceId)
                        .findFirst();

                if (fav != null) {
                    fav.deleteFromRealm();
                    btnFavorite.setSelected(false);
                } else {
                    Number maxId = r.where(Favorite.class).max("id");
                    long nextId = (maxId == null) ? 1 : maxId.longValue() + 1;

                    Favorite f = r.createObject(Favorite.class, nextId);
                    f.setClientId(CLIENT_ID);
                    f.setWorkspaceId(workspaceId);
                    f.setCreatedAt(String.valueOf(System.currentTimeMillis()));

                    btnFavorite.setSelected(true);
                }
            });
        });

        Button btnReserve = view.findViewById(R.id.btnReserve);
        btnReserve.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BookingActivity.class);
            intent.putExtra("workspace_id", workspaceId);
            startActivity(intent);
        });

        return view;
    }
}
