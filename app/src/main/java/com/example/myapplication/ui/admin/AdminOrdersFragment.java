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
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

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

        loadOrders();

        return view;
    }

    private void loadOrders() {
        RealmResults<Reservation> reservations = realm.where(Reservation.class)
                .sort("id", Sort.DESCENDING)
                .findAll();

        adapter = new OrderAdapter(getContext(), reservations, this::showStatusChangeDialog);

        recyclerView.setAdapter(adapter);
    }

    private void showStatusChangeDialog(final Reservation reservation) {
        final String[] statuses = {"PENDING", "CONFIRMED", "REFUSED", "CANCELLED"};
        final String currentStatus = reservation.getStatus() != null ? reservation.getStatus().name() : "PENDING";

        new AlertDialog.Builder(requireContext())
            .setTitle("Change Order Status")
            .setItems(statuses, (dialog, which) -> {
                String newStatus = statuses[which];
                if (!newStatus.equals(currentStatus)) {
                    updateOrderStatus(reservation, ReservationStatus.valueOf(newStatus));
                }
            })
            .show();
    }

    private void updateOrderStatus(Reservation reservation, ReservationStatus newStatus) {
        ReservationStatus oldStatus = reservation.getStatus();
        int numberOfPlaces = reservation.getNumberOfPlaces();
        Long workspaceId = reservation.getWorkspaceId();

        realm.executeTransaction(r -> {
            // Update the reservation status
            reservation.setStatus(newStatus);

            // Update workspace available places based on status change
            if (workspaceId != null) {
                Workspace workspace = r.where(Workspace.class).equalTo("id", workspaceId).findFirst();
                if (workspace != null) {
                    int currentAvailable = workspace.getAvailablePlaces();
                    int capacity = workspace.getCapacity();

                    // If changing FROM confirmed TO non-confirmed: add places back
                    if (oldStatus == ReservationStatus.CONFIRMED &&
                        (newStatus == ReservationStatus.PENDING ||
                         newStatus == ReservationStatus.REFUSED ||
                         newStatus == ReservationStatus.CANCELLED)) {
                        int newAvailable = Math.min(capacity, currentAvailable + numberOfPlaces);
                        workspace.setAvailablePlaces(newAvailable);

                        // Update status if places became available
                        if (newAvailable > 0 && "FULL".equals(workspace.getStatus())) {
                            workspace.setStatus("AVAILABLE");
                        }
                    }

                    // If changing TO confirmed FROM non-confirmed: subtract places
                    if (newStatus == ReservationStatus.CONFIRMED &&
                        (oldStatus == ReservationStatus.PENDING ||
                         oldStatus == ReservationStatus.REFUSED ||
                         oldStatus == ReservationStatus.CANCELLED ||
                         oldStatus == null)) {
                        int newAvailable = Math.max(0, currentAvailable - numberOfPlaces);
                        workspace.setAvailablePlaces(newAvailable);

                        // Update status if full
                        if (newAvailable == 0) {
                            workspace.setStatus("FULL");
                        }
                    }
                }
            }
        });

        adapter.notifyDataSetChanged();

        String message = "Status changed to " + newStatus.name();
        if (newStatus == ReservationStatus.CONFIRMED) {
            message += " (" + numberOfPlaces + " places reserved)";
        } else if (oldStatus == ReservationStatus.CONFIRMED) {
            message += " (" + numberOfPlaces + " places released)";
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
