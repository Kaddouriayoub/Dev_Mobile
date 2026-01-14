package com.example.myapplication.ui.client.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Reservation;
import com.example.myapplication.model.Review;
import com.example.myapplication.ui.adapters.PastReservationAdapter;
import com.example.myapplication.ui.adapters.UpcomingReservationAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class ReservationFragment extends Fragment {

    private RecyclerView rvUpcoming, rvPast;
    private TextView txtUpcomingCount, txtPastCount;
    private Realm realm;

    private static final long CLIENT_ID = 0L;

    public ReservationFragment() {
        super(R.layout.fragment_reservation);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvUpcoming = view.findViewById(R.id.rvUpcoming);
        rvPast = view.findViewById(R.id.rvPast);
        txtUpcomingCount = view.findViewById(R.id.txtUpcomingCount);
        txtPastCount = view.findViewById(R.id.txtPastCount);

        rvUpcoming.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPast.setLayoutManager(new LinearLayoutManager(requireContext()));

        realm = Realm.getDefaultInstance();
        loadReservationsFromRealm(CLIENT_ID);
    }

    private void loadReservationsFromRealm(long clientId) {
        RealmResults<Reservation> results = realm.where(Reservation.class)
                .equalTo("clientId", clientId)
                .findAll();

        List<Reservation> upcoming = new ArrayList<>();
        List<Reservation> past = new ArrayList<>();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();

        for (Reservation r : results) {
            Date d = parseSafe(df, r.getReservationDate());
            if (d == null) continue;

            if (!d.before(stripTime(today))) upcoming.add(realm.copyFromRealm(r));
            else past.add(realm.copyFromRealm(r));
        }

        txtUpcomingCount.setText(String.valueOf(upcoming.size()));
        txtPastCount.setText(String.valueOf(past.size()));

        rvUpcoming.setAdapter(new UpcomingReservationAdapter(upcoming, reservation -> {
            long workspaceId = reservation.getWorkspaceId();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, WorkspaceDetailsFragment.newInstance(workspaceId))
                    .addToBackStack(null)
                    .commit();
        }));

        rvPast.setAdapter(new PastReservationAdapter(past, this::handlePastReservationClick));
    }

    private void handlePastReservationClick(Reservation reservation) {
        long workspaceId = reservation.getWorkspaceId();

//        long startMs = toMillis(reservation.getReservationDate(), reservation.getStartTime());
//        long endMs   = toMillis(reservation.getReservationDate(), reservation.getEndTime());

        Realm r = Realm.getDefaultInstance();

        // createdAt is stored as String -> compare as String (milliseconds)
        Review existing = r.where(Review.class)
                .equalTo("reservationId", reservation.getId())
//                .equalTo("clientId", CLIENT_ID)
//                .equalTo("workspaceId", workspaceId)
//                .greaterThanOrEqualTo("createdAt", String.valueOf(startMs))
//                .lessThanOrEqualTo("createdAt", String.valueOf(endMs))
                .findFirst();

        r.close();

        if (existing == null) {
            showReviewDialog(workspaceId, reservation.getId());
        } else {
            openWorkspaceDetails(workspaceId);
        }
    }

    private void openWorkspaceDetails(long workspaceId) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, WorkspaceDetailsFragment.newInstance(workspaceId))
                .addToBackStack(null)
                .commit();
    }

    private void showReviewDialog(long workspaceId, long reservationId) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_review, null);

        RatingBar ratingBar = dialogView.findViewById(R.id.dialogRatingBar);
        EditText edtComment = dialogView.findViewById(R.id.edtComment);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Noter votre expérience")
                .setView(dialogView)
                .setNegativeButton("Annuler", (d, w) -> d.dismiss())
                .setPositiveButton("Envoyer", (d, w) -> {
                    int rating = (int) ratingBar.getRating();
                    String comment = edtComment.getText().toString().trim();
                    saveReview(workspaceId, rating, comment, reservationId);
                    openWorkspaceDetails(workspaceId);
                })
                .show();
    }

    private void saveReview(long workspaceId, int rating, String comment, long reservationId) {
        Realm r = Realm.getDefaultInstance();

        r.executeTransaction(tx -> {
            Number maxId = tx.where(Review.class).max("id");
            long nextId = (maxId == null) ? 1 : maxId.longValue() + 1;

            Review review = tx.createObject(Review.class, nextId);
            review.setWorkspaceId(workspaceId);
            review.setClientId(CLIENT_ID);
            review.setRating(rating);
            review.setComment(comment);
            review.setReservationId(reservationId);
            review.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        });

        r.close();
    }

    private long toMillis(String date, String time) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date d = df.parse(date + " " + time);
            return d != null ? d.getTime() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private Date parseSafe(SimpleDateFormat df, String value) {
        try {
            return df.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    private Date stripTime(Date d) {
        return new Date(d.getYear(), d.getMonth(), d.getDate());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realm != null && !realm.isClosed()) realm.close();
    }
}
