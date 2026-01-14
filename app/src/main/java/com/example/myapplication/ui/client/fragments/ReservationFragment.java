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
import com.example.myapplication.model.ReservationStatus;
import com.example.myapplication.model.Review;
import com.example.myapplication.ui.adapters.PastReservationAdapter;
import com.example.myapplication.ui.adapters.UpcomingReservationAdapter;
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class ReservationFragment extends Fragment {

    private RecyclerView rvUpcoming, rvPast;
    private TextView txtUpcomingCount, txtPastCount;
    private Realm realm;
    private SessionManager sessionManager;
//    private static final long CLIENT_ID = 0L;

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

        // Check and mark completed reservations before loading
        markCompletedReservations();

        loadReservationsFromRealm(CLIENT_ID);
    }

    private void markCompletedReservations() {
        // Get current date and time
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1; // Calendar months are 0-based
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        int currentHour = now.get(Calendar.HOUR_OF_DAY);

        // Find all CONFIRMED client reservations (not admin orders)
        RealmResults<Reservation> confirmedReservations = realm.where(Reservation.class)
                .equalTo("status", ReservationStatus.CONFIRMED.name())
                .equalTo("clientId", CLIENT_ID)
                .equalTo("isAdminOrder", false)  // Only client's own reservations
                .findAll();

        realm.executeTransaction(r -> {
            for (Reservation reservation : confirmedReservations) {
                if (isReservationCompleted(reservation, currentYear, currentMonth, currentDay, currentHour)) {
                    reservation.setStatus(ReservationStatus.COMPLETED);
                }
            }
        });
    }

    private boolean isReservationCompleted(Reservation reservation, int currentYear, int currentMonth, int currentDay, int currentHour) {
        try {
            // Parse reservation date (format: "YYYY-M-D")
            String dateStr = reservation.getReservationDate();
            if (dateStr == null) return false;

            String[] dateParts = dateStr.split("-");
            if (dateParts.length != 3) return false;

            int resYear = Integer.parseInt(dateParts[0]);
            int resMonth = Integer.parseInt(dateParts[1]);
            int resDay = Integer.parseInt(dateParts[2]);

            // Parse end time
            String endTimeStr = reservation.getEndTime();
            if (endTimeStr == null) return false;
            int endHour = Integer.parseInt(endTimeStr.replace(":00", ""));

            // Compare dates
            if (resYear < currentYear) return true;
            if (resYear > currentYear) return false;

            if (resMonth < currentMonth) return true;
            if (resMonth > currentMonth) return false;

            if (resDay < currentDay) return true;
            if (resDay > currentDay) return false;

            // Same day - check if end time has passed
            return endHour <= currentHour;

        } catch (Exception e) {
            return false;
        }
    }

    private void loadReservationsFromRealm(long clientId) {
        // Only load client reservations (not admin orders)
        RealmResults<Reservation> results = realm.where(Reservation.class)
                .equalTo("clientId", clientId)
                .equalTo("isAdminOrder", false)  // Only client's own reservations
                .findAll();

        List<Reservation> upcoming = new ArrayList<>();
        List<Reservation> past = new ArrayList<>();

        for (Reservation r : results) {
            ReservationStatus status = r.getStatus();
            if (status == null) {
                status = ReservationStatus.PENDING;
            }

            // Upcoming: PENDING or CONFIRMED
            // Past (History): REFUSED or COMPLETED
            // CANCELLED should never appear for client reservations
            switch (status) {
                case PENDING:
                case CONFIRMED:
                    upcoming.add(realm.copyFromRealm(r));
                    break;
                case REFUSED:
                case COMPLETED:
                    past.add(realm.copyFromRealm(r));
                    break;
                // CANCELLED is only for admin orders, ignore it here
            }
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

        // Only allow reviews for COMPLETED reservations
        ReservationStatus status = reservation.getStatus();
        if (status == null) {
            status = ReservationStatus.COMPLETED;
        }

        // If REFUSED, just open workspace details (no review allowed)
        if (status == ReservationStatus.REFUSED) {
            openWorkspaceDetails(workspaceId);
            return;
        }

        // For COMPLETED reservations, check if review already exists
        if (status == ReservationStatus.COMPLETED) {
            Realm r = Realm.getDefaultInstance();

            Review existing = r.where(Review.class)
                    .equalTo("reservationId", reservation.getId())
                    .findFirst();

            r.close();

            if (existing == null) {
                // No review yet, show rating dialog
                showReviewDialog(workspaceId, reservation.getId());
            } else {
                // Review already given, just open workspace details
                openWorkspaceDetails(workspaceId);
            }
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

    @Override
    public void onResume() {
        super.onResume();
        // Reload reservations when returning to this fragment
        if (realm != null && !realm.isClosed()) {
            markCompletedReservations();
            loadReservationsFromRealm(CLIENT_ID);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realm != null && !realm.isClosed()) realm.close();
    }
}
