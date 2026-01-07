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
