package com.example.myapplication.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private Realm realm;
    private OrderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);

        recyclerView = view.findViewById(R.id.recycler_orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAdd = view.findViewById(R.id.btn_add_order);

        btnAdd.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AddOrderFragment())
                .addToBackStack(null)
                .commit();
        });

        realm = Realm.getDefaultInstance();

        // Check and mark completed reservations before loading
        markCompletedReservations();

        loadOrders();

        return view;
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

        // Filter reservations by workspaces owned by current admin using link query
        RealmResults<Reservation> reservations = realm.where(Reservation.class)
                .equalTo("workspace.adminId", currentAdminId)
                .sort("id", Sort.DESCENDING)
                .findAll();

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
                // CONFIRMED can only become CANCELLED
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

    private void updateOrderStatus(Reservation reservation, ReservationStatus newStatus) {
        realm.executeTransaction(r -> {
            reservation.setStatus(newStatus);
        });

        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Status changed to " + newStatus.name(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
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
