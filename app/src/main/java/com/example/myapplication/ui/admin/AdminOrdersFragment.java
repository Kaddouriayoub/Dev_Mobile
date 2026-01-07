package com.example.myapplication.ui.admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Reservation;
import com.example.myapplication.model.ReservationStatus;
import com.example.myapplication.ui.adapters.OrderAdapter;
import io.realm.Realm;
import io.realm.RealmResults;

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
        RealmResults<Reservation> reservations = realm.where(Reservation.class).findAll();
        
        adapter = new OrderAdapter(getContext(), reservations, reservation -> {
            showStatusChangeDialog(reservation);
        });
        
        recyclerView.setAdapter(adapter);
    }

    private void showStatusChangeDialog(final Reservation reservation) {
        final String[] statuses = {"PENDING", "CONFIRMED", "CANCELLED"};
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Change Order Status")
            .setItems(statuses, (dialog, which) -> {
                realm.executeTransaction(r -> {
                    reservation.setStatus(ReservationStatus.valueOf(statuses[which]));
                });
                adapter.notifyDataSetChanged();
            })
            .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}
