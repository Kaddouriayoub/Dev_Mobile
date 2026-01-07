package com.example.myapplication.ui.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.model.Review;
import com.example.myapplication.model.Workspace;

import io.realm.Realm;
import io.realm.RealmResults;

public class WorkspaceDetailsFragment extends Fragment {

    private static final String ARG_ID = "workspace_id";

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
        View view = inflater.inflate(
                R.layout.fragment_workspace_details, container, false);

        long workspaceId = getArguments().getLong(ARG_ID);

        Realm realm = Realm.getDefaultInstance();

        // Workspace
        Workspace w = realm.where(Workspace.class)
                .equalTo("id", workspaceId)
                .findFirst();

        // Reviews for this workspace
        RealmResults<Review> reviews = realm.where(Review.class)
                .equalTo("workspaceId", workspaceId)
                .findAll();

        float averageRating = 0f;

        if (reviews != null && reviews.size() > 0) {
            int sum = 0;
            for (Review r : reviews) {
                sum += r.getRating();
            }
            averageRating = (float) sum / reviews.size();
        }

        // Bind data
        if (w != null) {
            ((TextView) view.findViewById(R.id.txtWorkspaceName))
                    .setText(w.getName());

            ((TextView) view.findViewById(R.id.txtPrice))
                    .setText(w.getPricePerHour() + "€/h");

            ((TextView) view.findViewById(R.id.txtAddress))
                    .setText(w.getAddress());

            ((TextView) view.findViewById(R.id.txtStatus))
                    .setText(w.getStatus());

            ((TextView) view.findViewById(R.id.txtDescription))
                    .setText(w.getDescription());
        }

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        ratingBar.setRating(averageRating);

        realm.close();
        return view;
    }
}
