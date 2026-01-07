package com.example.myapplication.ui.client;

import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.model.Reservation;
import com.example.myapplication.model.ReservationStatus;
import com.example.myapplication.model.Workspace;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class BookingActivity extends AppCompatActivity {

    private Realm realm;
    private long workspaceId;

    private NumberPicker startPicker, endPicker;
    private TextView txtTotalPrice;
    private DatePicker datePicker;
    private Button btnConfirm;

    private double pricePerHour;
    private List<Reservation> dayReservations = new ArrayList<>();

    private static final int OPEN_HOUR = 8;
    private static final int CLOSE_HOUR = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        workspaceId = getIntent().getLongExtra("workspace_id", -1);
        realm = Realm.getDefaultInstance();

        startPicker = findViewById(R.id.startHourPicker);
        endPicker = findViewById(R.id.endHourPicker);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        datePicker = findViewById(R.id.datePicker);
        btnConfirm = findViewById(R.id.btnConfirm);

        Workspace w = realm.where(Workspace.class)
                .equalTo("id", workspaceId)
                .findFirst();

        if (w != null) {
            pricePerHour = w.getPricePerHour();
            ((TextView) findViewById(R.id.txtPricePerHour))
                    .setText("Prix par heure : " + pricePerHour + "€");
        }

        datePicker.init(
                datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth(),
                (view, year, month, day) -> loadDayReservations(year, month, day)
        );

        loadDayReservations(
                datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth()
        );

        // ✅ CONFIRM RESERVATION
        btnConfirm.setOnClickListener(v -> confirmReservation());
    }

    /* ---------------- CORE LOGIC ---------------- */

    private void loadDayReservations(int year, int month, int day) {

        String dateKey = year + "-" + (month + 1) + "-" + day;

        RealmResults<Reservation> results = realm.where(Reservation.class)
                .equalTo("workspaceId", workspaceId)
                .equalTo("reservationDate", dateKey)
                .findAll();

        dayReservations.clear();
        dayReservations.addAll(results);

        setupStartPicker();
    }

    private void setupStartPicker() {

        List<Integer> availableStarts = new ArrayList<>();

        for (int h = OPEN_HOUR; h < CLOSE_HOUR; h++) {
            if (!isHourReserved(h)) {
                availableStarts.add(h);
            }
        }

        applyPickerValues(startPicker, availableStarts);

        if (availableStarts.isEmpty()) {
            txtTotalPrice.setText("Total : --€");
            return;
        }

        startPicker.setValue(0);
        int startHour = availableStarts.get(0);

        setupEndPicker(startHour);

        startPicker.setOnValueChangedListener((p, o, n) -> {
            int newStart = availableStarts.get(n);
            setupEndPicker(newStart);
        });
    }

    private void setupEndPicker(int startHour) {

        int nextBookingStart = CLOSE_HOUR;

        for (Reservation r : dayReservations) {
            int rs = Integer.parseInt(r.getStartTime());
            if (rs > startHour && rs < nextBookingStart) {
                nextBookingStart = rs;
            }
        }

        List<Integer> ends = new ArrayList<>();
        for (int h = startHour + 1; h <= nextBookingStart; h++) {
            ends.add(h);
        }

        applyPickerValues(endPicker, ends);

        endPicker.setValue(0);
        updateTotal(startHour, ends.get(0));

        endPicker.setOnValueChangedListener((p, o, n) -> {
            updateTotal(startHour, ends.get(n));
        });
    }

    private boolean isHourReserved(int hour) {
        for (Reservation r : dayReservations) {
            int s = Integer.parseInt(r.getStartTime());
            int e = Integer.parseInt(r.getEndTime());
            if (hour >= s && hour < e) return true;
        }
        return false;
    }

    /* ---------------- CONFIRM ---------------- */

    private void confirmReservation() {

        int year = datePicker.getYear();
        int month = datePicker.getMonth() + 1;
        int day = datePicker.getDayOfMonth();
        String dateKey = year + "-" + month + "-" + day;

        int startHour = Integer.parseInt(
                startPicker.getDisplayedValues()[startPicker.getValue()]
                        .replace(":00", "")
        );

        int endHour = Integer.parseInt(
                endPicker.getDisplayedValues()[endPicker.getValue()]
                        .replace(":00", "")
        );

        double total = (endHour - startHour) * pricePerHour;

        realm.executeTransaction(r -> {
            Number maxId = r.where(Reservation.class).max("id");
            long nextId = (maxId == null) ? 1 : maxId.longValue() + 1;

            Reservation res = r.createObject(Reservation.class, nextId);
            res.setWorkspaceId(workspaceId);
            res.setClientId(0L); // 🔒 hard-coded client
            res.setReservationDate(dateKey);
            res.setStartTime(String.valueOf(startHour));
            res.setEndTime(String.valueOf(endHour));
            res.setTotalPrice(total);
            res.setStatus(ReservationStatus.CONFIRMED);
        });

        Toast.makeText(this, "Réservation confirmée", Toast.LENGTH_SHORT).show();
        finish();
    }

    /* ---------------- UI HELPERS ---------------- */

    private void applyPickerValues(NumberPicker picker, List<Integer> values) {

        String[] displayed = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            displayed[i] = values.get(i) + ":00";
        }

        picker.setDisplayedValues(null);
        picker.setMinValue(0);
        picker.setMaxValue(values.size() - 1);
        picker.setDisplayedValues(displayed);
        picker.setWrapSelectorWheel(false);
    }

    private void updateTotal(int start, int end) {
        int hours = end - start;
        double total = hours * pricePerHour;
        txtTotalPrice.setText("Total : " + total + "€");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
