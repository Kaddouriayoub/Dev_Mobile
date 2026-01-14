package com.example.myapplication.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Reservation;
import com.example.myapplication.model.ReservationStatus;
import com.example.myapplication.model.Workspace;
import com.google.android.material.chip.Chip;

import io.realm.Realm;

public class PastReservationAdapter extends RecyclerView.Adapter<PastReservationAdapter.VH> {

    public interface OnItemClick {
        void onClick(Reservation reservation);
    }

    private final java.util.List<Reservation> data;
    private final OnItemClick listener;

    private final Realm realm = Realm.getDefaultInstance();

    public PastReservationAdapter(java.util.List<Reservation> data,
                                  OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation_past, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int position) {
        Reservation r = data.get(position);

        String name = "Workspace";
        if (r.getWorkspaceId() != null) {
            Workspace w = realm.where(Workspace.class)
                    .equalTo("id", r.getWorkspaceId())
                    .findFirst();
            if (w != null && w.getName() != null) {
                name = w.getName();
            }
        }

        h.txtWorkspaceName.setText(name);
        h.txtDate.setText(r.getReservationDate() != null ? r.getReservationDate() : "");
        h.txtTime.setText((r.getStartTime() != null ? r.getStartTime() : "") + " → " +
                (r.getEndTime() != null ? r.getEndTime() : ""));
        h.txtTotal.setText("Total: " + (int) r.getTotalPrice() + "DH");

        // Display real status with colored text only
        // Only COMPLETED and REFUSED should appear in client history
        ReservationStatus status = r.getStatus();
        if (status == null) {
            status = ReservationStatus.COMPLETED;
        }

        // Set transparent background for all
        h.chipStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));

        switch (status) {
            case COMPLETED:
                h.chipStatus.setText("Terminé");
                h.chipStatus.setTextColor(Color.parseColor("#2196F3")); // Blue
                break;
            case REFUSED:
                h.chipStatus.setText("Refusé");
                h.chipStatus.setTextColor(Color.parseColor("#F44336")); // Red
                break;
            default:
                // This should never happen for client reservations
                h.chipStatus.setText(status.name());
                h.chipStatus.setTextColor(Color.GRAY);
                break;
        }

        h.itemView.setOnClickListener(v -> listener.onClick(r));
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    public void closeRealm() {
        if (!realm.isClosed()) realm.close();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtWorkspaceName, txtDate, txtTime, txtTotal;
        Chip chipStatus;

        VH(View itemView) {
            super(itemView);
            txtWorkspaceName = itemView.findViewById(R.id.txtWorkspaceName);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }
    }
}
