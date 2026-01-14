package com.example.myapplication.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Reservation;
import com.example.myapplication.model.ReservationStatus;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.ui.adapters.OrderAdapter;
import com.example.myapplication.utils.SessionManager;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import java.util.Calendar;

public class AdminOrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageView btnAdd;
    private Spinner spinnerFilter;
    private Realm realm;
    private OrderAdapter adapter;

    private String[] filterOptions = {"Tous", "En attente", "Confirmé", "Refusé", "Annulé", "Terminé"};
    private ReservationStatus currentFilter = null; // null means "ALL"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);

        recyclerView = view.findViewById(R.id.recycler_orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAdd = view.findViewById(R.id.btn_add_order);
        spinnerFilter = view.findViewById(R.id.spinner_status_filter);

        btnAdd.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AddOrderFragment())
                .addToBackStack(null)
                .commit();
        });

        realm = Realm.getDefaultInstance();

        // Check and mark completed reservations before loading
        markCompletedReservations();

        // Setup spinner
        setupFilterSpinner();

        loadOrders();

        return view;
    }

    private void setupFilterSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                filterOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // Tous
                        currentFilter = null;
                        break;
                    case 1: // En attente
                        currentFilter = ReservationStatus.PENDING;
                        break;
                    case 2: // Confirmé
                        currentFilter = ReservationStatus.CONFIRMED;
                        break;
                    case 3: // Refusé
                        currentFilter = ReservationStatus.REFUSED;
                        break;
                    case 4: // Annulé
                        currentFilter = ReservationStatus.CANCELLED;
                        break;
                    case 5: // Terminé
                        currentFilter = ReservationStatus.COMPLETED;
                        break;
                }
                loadOrders();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void markCompletedReservations() {
        // Get current admin's ID from session
        SessionManager sessionManager = new SessionManager(getContext());
        Long currentAdminId = sessionManager.getUserId();

        // Get current date and time
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1; // Calendar months are 0-based
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        int currentHour = now.get(Calendar.HOUR_OF_DAY);

        // Find all CONFIRMED reservations for current admin's workspaces
        RealmResults<Reservation> confirmedReservations = realm.where(Reservation.class)
                .equalTo("status", ReservationStatus.CONFIRMED.name())
                .equalTo("workspace.adminId", currentAdminId)
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

    private void loadOrders() {
        // Get current admin's ID from session
        SessionManager sessionManager = new SessionManager(getContext());
        Long currentAdminId = sessionManager.getUserId();

        // Build query based on filter
        RealmResults<Reservation> reservations;

        if (currentFilter == null) {
            // Show all orders
            reservations = realm.where(Reservation.class)
                    .equalTo("workspace.adminId", currentAdminId)
                    .sort("id", Sort.DESCENDING)
                    .findAll();
        } else {
            // Filter by specific status
            reservations = realm.where(Reservation.class)
                    .equalTo("workspace.adminId", currentAdminId)
                    .equalTo("status", currentFilter.name())
                    .sort("id", Sort.DESCENDING)
                    .findAll();
        }

        adapter = new OrderAdapter(getContext(), reservations, this::showStatusChangeDialog);
        recyclerView.setAdapter(adapter);
    }

    private void showStatusChangeDialog(final Reservation reservation) {
        final ReservationStatus currentStatus = reservation.getStatus() != null ?
                reservation.getStatus() : ReservationStatus.PENDING;

        // Determine allowed transitions based on current status
        String[] allowedStatuses;
        switch (currentStatus) {
            case PENDING:
                // PENDING can become CONFIRMED or REFUSED
                allowedStatuses = new String[]{"CONFIRMED", "REFUSED"};
                break;
            case CONFIRMED:
                // CONFIRMED can only become CANCELLED if:
                // 1. It's an admin order (isAdminOrder = true)
                // 2. The reservation time hasn't started yet
                if (!reservation.isAdminOrder()) {
                    // Client reservations cannot be cancelled by admin after confirmation
                    Toast.makeText(getContext(), "Cannot cancel client reservations after confirmation", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if reservation time has not started yet
                if (hasReservationStarted(reservation)) {
                    Toast.makeText(getContext(), "Cannot cancel: reservation time has already started or passed", Toast.LENGTH_SHORT).show();
                    return;
                }

                allowedStatuses = new String[]{"CANCELLED"};
                break;
            case REFUSED:
            case CANCELLED:
            case COMPLETED:
                // REFUSED, CANCELLED and COMPLETED are final states - no changes allowed
                Toast.makeText(getContext(), "Cannot change status: " + currentStatus.name() + " is a final state", Toast.LENGTH_SHORT).show();
                return;
            default:
                allowedStatuses = new String[]{};
        }

        if (allowedStatuses.length == 0) {
            Toast.makeText(getContext(), "No status changes available", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
            .setTitle("Change Status (Current: " + currentStatus.name() + ")")
            .setItems(allowedStatuses, (dialog, which) -> {
                String newStatus = allowedStatuses[which];
                updateOrderStatus(reservation, ReservationStatus.valueOf(newStatus));
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private boolean hasReservationStarted(Reservation reservation) {
        try {
            // Get current date and time
            Calendar now = Calendar.getInstance();
            int currentYear = now.get(Calendar.YEAR);
            int currentMonth = now.get(Calendar.MONTH) + 1; // Calendar months are 0-based
            int currentDay = now.get(Calendar.DAY_OF_MONTH);
            int currentHour = now.get(Calendar.HOUR_OF_DAY);

            // Parse reservation date (format: "YYYY-M-D")
            String dateStr = reservation.getReservationDate();
            if (dateStr == null) return true; // If no date, assume started

            String[] dateParts = dateStr.split("-");
            if (dateParts.length != 3) return true;

            int resYear = Integer.parseInt(dateParts[0]);
            int resMonth = Integer.parseInt(dateParts[1]);
            int resDay = Integer.parseInt(dateParts[2]);

            // Parse start time
            String startTimeStr = reservation.getStartTime();
            if (startTimeStr == null) return true;
            int startHour = Integer.parseInt(startTimeStr.replace(":00", ""));

            // Compare dates
            if (resYear < currentYear) return true;
            if (resYear > currentYear) return false;

            if (resMonth < currentMonth) return true;
            if (resMonth > currentMonth) return false;

            if (resDay < currentDay) return true;
            if (resDay > currentDay) return false;

            // Same day - check if start time has passed
            return startHour <= currentHour;

        } catch (Exception e) {
            return true; // If error, assume started (safer)
        }
    }

    private void updateOrderStatus(Reservation reservation, ReservationStatus newStatus) {
        realm.executeTransaction(r -> {
            reservation.setStatus(newStatus);
        });

        // Reload orders to update the filtered list
        loadOrders();
        Toast.makeText(getContext(), "Status changed to " + newStatus.name(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload orders when returning to fragment
        if (realm != null && !realm.isClosed()) {
            markCompletedReservations();
            loadOrders();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}
