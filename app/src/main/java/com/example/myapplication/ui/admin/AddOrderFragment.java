package com.example.myapplication.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.model.Reservation;
import com.example.myapplication.model.ReservationStatus;
import com.example.myapplication.model.Workspace;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmResults;

public class AddOrderFragment extends Fragment {

    private AutoCompleteTextView atWorkspace;
    private TextInputEditText etClientName;
    private TextView tvWorkspaceInfo;
    private TextView txtPricePerHour, txtDuration, txtTotalPrice;
    private NumberPicker startPicker, endPicker;
    private DatePicker datePicker;
    private Button btnConfirm;

    private Realm realm;
    private List<Workspace> workspaceList;
    private Workspace selectedWorkspace;

    private double pricePerHour;

    private List<Reservation> dayReservations = new ArrayList<>();
    private Set<Integer> bookedHours = new HashSet<>();

    private static final int OPEN_HOUR = 8;
    private static final int CLOSE_HOUR = 20;

    // Track current available hours for start/end pickers
    private List<Integer> currentStartHours = new ArrayList<>();
    private List<Integer> currentEndHours = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_order, container, false);

        // Initialize views
        atWorkspace = view.findViewById(R.id.at_workspace);
        etClientName = view.findViewById(R.id.et_client_name);
        tvWorkspaceInfo = view.findViewById(R.id.tv_workspace_info);
        datePicker = view.findViewById(R.id.datePicker);
        startPicker = view.findViewById(R.id.startHourPicker);
        endPicker = view.findViewById(R.id.endHourPicker);
        txtPricePerHour = view.findViewById(R.id.txtPricePerHour);
        txtDuration = view.findViewById(R.id.txtDuration);
        txtTotalPrice = view.findViewById(R.id.txtTotalPrice);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        realm = Realm.getDefaultInstance();

        // Setup workspace dropdown
        setupWorkspaceDropdown();

        // Setup date picker listener
        datePicker.init(
                datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth(),
                (picker, year, month, day) -> {
                    if (selectedWorkspace != null) {
                        loadDayReservations(year, month, day);
                    }
                }
        );

        // Confirm button
        btnConfirm.setOnClickListener(v -> confirmOrder());

        // Initially disable pickers until workspace is selected
        setPickersEnabled(false);

        return view;
    }

    private void setPickersEnabled(boolean enabled) {
        startPicker.setEnabled(enabled);
        endPicker.setEnabled(enabled);
        datePicker.setEnabled(enabled);
    }

    private void setupWorkspaceDropdown() {
        RealmResults<Workspace> workspaces = realm.where(Workspace.class).findAll();
        workspaceList = new ArrayList<>(realm.copyFromRealm(workspaces));

        List<String> workspaceNames = new ArrayList<>();
        for (Workspace w : workspaceList) {
            workspaceNames.add(w.getName() + " - " + w.getPricePerHour() + " dh/h");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, workspaceNames);
        atWorkspace.setAdapter(adapter);

        atWorkspace.setOnItemClickListener((parent, view, position, id) -> {
            selectedWorkspace = workspaceList.get(position);
            pricePerHour = selectedWorkspace.getPricePerHour();

            // Show workspace info
            tvWorkspaceInfo.setVisibility(View.VISIBLE);
            tvWorkspaceInfo.setText("Capacity: " + selectedWorkspace.getCapacity() + " places");

            // Update price display
            txtPricePerHour.setText("Price per hour: " + pricePerHour + " dh");

            // Enable pickers
            setPickersEnabled(true);

            // Load reservations for selected date
            loadDayReservations(
                    datePicker.getYear(),
                    datePicker.getMonth(),
                    datePicker.getDayOfMonth()
            );
        });
    }

    /* ---------------- CORE LOGIC ---------------- */

    private void loadDayReservations(int year, int month, int day) {
        if (selectedWorkspace == null) return;

        String dateKey = year + "-" + (month + 1) + "-" + day;

        RealmResults<Reservation> results = realm.where(Reservation.class)
                .equalTo("workspaceId", selectedWorkspace.getId())
                .equalTo("reservationDate", dateKey)
                .equalTo("status", ReservationStatus.CONFIRMED.name())
                .findAll();

        dayReservations.clear();
        dayReservations.addAll(realm.copyFromRealm(results));

        computeBookedHours();
        setupStartPicker();
    }

    private void computeBookedHours() {
        bookedHours.clear();

        // Find all hours that are already booked
        for (Reservation r : dayReservations) {
            try {
                int s = Integer.parseInt(r.getStartTime().replace(":00", ""));
                int e = Integer.parseInt(r.getEndTime().replace(":00", ""));

                for (int h = s; h < e; h++) {
                    bookedHours.add(h);
                }
            } catch (NumberFormatException e) {
                // Skip malformed reservations
            }
        }
    }

    private void setupStartPicker() {
        currentStartHours.clear();

        // Find available start hours (not booked)
        for (int h = OPEN_HOUR; h < CLOSE_HOUR; h++) {
            if (!bookedHours.contains(h)) {
                currentStartHours.add(h);
            }
        }

        if (currentStartHours.isEmpty()) {
            // No available slots
            applyPickerValues(startPicker, new ArrayList<>());
            applyPickerValues(endPicker, new ArrayList<>());
            txtTotalPrice.setText("Total: -- dh (fully booked)");
            txtDuration.setText("Duration: -- hours");
            return;
        }

        applyPickerValues(startPicker, currentStartHours);
        startPicker.setValue(0);

        int startHour = currentStartHours.get(0);
        setupEndPicker(startHour);

        startPicker.setOnValueChangedListener((p, o, n) -> {
            if (n < currentStartHours.size()) {
                setupEndPicker(currentStartHours.get(n));
            }
        });
    }

    private void setupEndPicker(int startHour) {
        currentEndHours.clear();

        // Find valid end hours (consecutive free slots from start)
        for (int h = startHour + 1; h <= CLOSE_HOUR; h++) {
            if (h == CLOSE_HOUR) {
                // Can end at closing time
                currentEndHours.add(h);
                break;
            }

            if (bookedHours.contains(h)) {
                // Hit a booked hour, must end here
                currentEndHours.add(h);
                break;
            }

            currentEndHours.add(h);
        }

        if (currentEndHours.isEmpty()) {
            currentEndHours.add(startHour + 1);
        }

        applyPickerValues(endPicker, currentEndHours);
        endPicker.setValue(0);

        updatePricing();

        endPicker.setOnValueChangedListener((p, o, n) -> updatePricing());
    }

    private void applyPickerValues(NumberPicker picker, List<Integer> values) {
        if (values.isEmpty()) {
            picker.setDisplayedValues(null);
            picker.setMinValue(0);
            picker.setMaxValue(0);
            picker.setDisplayedValues(new String[]{"--:--"});
            return;
        }

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

    private void updatePricing() {
        if (selectedWorkspace == null || currentStartHours.isEmpty() || currentEndHours.isEmpty()) {
            return;
        }

        int startIdx = startPicker.getValue();
        int endIdx = endPicker.getValue();

        if (startIdx >= currentStartHours.size() || endIdx >= currentEndHours.size()) {
            return;
        }

        int startHour = currentStartHours.get(startIdx);
        int endHour = currentEndHours.get(endIdx);

        int hours = endHour - startHour;
        double total = hours * pricePerHour;

        txtDuration.setText("Duration: " + hours + " hour(s)");
        txtTotalPrice.setText("Total: " + String.format("%.2f", total) + " dh");
    }

    /* ---------------- CONFIRM ORDER ---------------- */

    private void confirmOrder() {
        if (selectedWorkspace == null) {
            Toast.makeText(getContext(), "Please select a workspace", Toast.LENGTH_SHORT).show();
            return;
        }

        String clientName = etClientName.getText().toString().trim();
        if (clientName.isEmpty()) {
            Toast.makeText(getContext(), "Please enter client name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentStartHours.isEmpty() || currentEndHours.isEmpty()) {
            Toast.makeText(getContext(), "No available time slots", Toast.LENGTH_SHORT).show();
            return;
        }

        int startIdx = startPicker.getValue();
        int endIdx = endPicker.getValue();

        if (startIdx >= currentStartHours.size() || endIdx >= currentEndHours.size()) {
            Toast.makeText(getContext(), "Invalid time selection", Toast.LENGTH_SHORT).show();
            return;
        }

        int startHour = currentStartHours.get(startIdx);
        int endHour = currentEndHours.get(endIdx);

        int year = datePicker.getYear();
        int month = datePicker.getMonth() + 1;
        int day = datePicker.getDayOfMonth();
        String dateKey = year + "-" + month + "-" + day;

        int hours = endHour - startHour;
        double total = hours * pricePerHour;

        final long workspaceId = selectedWorkspace.getId();

        realm.executeTransaction(r -> {
            // Create reservation with unique ID
            Number maxId = r.where(Reservation.class).max("id");
            long nextId = (maxId == null) ? 1 : maxId.longValue() + 1;

            Reservation reservation = r.createObject(Reservation.class, nextId);
            reservation.setClientName(clientName);
            reservation.setReservationDate(dateKey);
            reservation.setStartTime(String.valueOf(startHour));
            reservation.setEndTime(String.valueOf(endHour));
            reservation.setTotalPrice(total);
            reservation.setStatus(ReservationStatus.CONFIRMED); // Admin orders auto-confirmed
            reservation.setAdminOrder(true);
            reservation.setWorkspaceId(workspaceId);

            // Link workspace
            Workspace managedWorkspace = r.where(Workspace.class).equalTo("id", workspaceId).findFirst();
            if (managedWorkspace != null) {
                reservation.setWorkspace(managedWorkspace);
            }
        });

        Toast.makeText(getContext(), "Order Created Successfully!", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}
