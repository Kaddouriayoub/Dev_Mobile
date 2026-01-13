package com.example.myapplication.ui.client.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Reservation;
import com.example.myapplication.ui.adapters.PastReservationAdapter;
import com.example.myapplication.ui.adapters.UpcomingReservationAdapter;

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

        loadReservationsFromRealm(0L); // clientId = 0
    }

    private void loadReservationsFromRealm(long clientId) {
        RealmResults<Reservation> results = realm.where(Reservation.class)
                .equalTo("clientId", clientId)
                .findAll(); // you can sort later if you want

        List<Reservation> upcoming = new ArrayList<>();
        List<Reservation> past = new ArrayList<>();

        // Assumption: reservationDate format is "yyyy-MM-dd"
        // If yours is different, tell me the format and I’ll adjust.
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();

        for (Reservation r : results) {
            Date d = parseSafe(df, r.getReservationDate());
            if (d == null) continue;

            // if date >= today => upcoming, else past
            if (!d.before(stripTime(today))) upcoming.add(realm.copyFromRealm(r));
            else past.add(realm.copyFromRealm(r));
        }

        txtUpcomingCount.setText(String.valueOf(upcoming.size()));
        txtPastCount.setText(String.valueOf(past.size()));

        // TODO: plug your adapters here (different click behavior)
        rvUpcoming.setAdapter(new UpcomingReservationAdapter(upcoming, reservation -> {
            // UPCOMING click behavior
            // e.g. open details, allow cancel, show QR, etc.
        }));

        rvPast.setAdapter(new PastReservationAdapter(past, reservation -> {
            // PAST click behavior
            // e.g. open receipt, allow review, etc.
        }));

    }

    private Date parseSafe(SimpleDateFormat df, String value) {
        try { return df.parse(value); } catch (ParseException e) { return null; }
    }

    private Date stripTime(Date d) {
        // today at 00:00 for fair compare
        return new Date(d.getYear(), d.getMonth(), d.getDate());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realm != null && !realm.isClosed()) realm.close();
    }
}
