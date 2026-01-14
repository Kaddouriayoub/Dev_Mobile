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

import java.util.List;

import io.realm.Realm;


public class UpcomingReservationAdapter extends RecyclerView.Adapter<UpcomingReservationAdapter.VH> {

    public interface OnItemClick {
        void onClick(Reservation reservation);
    }

    private final List<Reservation> data;
    private final OnItemClick listener;

    private final Realm realm = Realm.getDefaultInstance();

    public UpcomingReservationAdapter(List<Reservation> data,
                                      OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation_upcoming, parent, false);
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
        ReservationStatus status = r.getStatus();
        if (status == null) {
            status = ReservationStatus.PENDING;
        }

        // Set transparent background for all
        h.chipStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));

        switch (status) {
            case PENDING:
                h.chipStatus.setText("En attente");
                h.chipStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
                break;
            case CONFIRMED:
                h.chipStatus.setText("Confirmé");
                h.chipStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
                break;
            default:
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

        VH(android.view.View itemView) {
            super(itemView);
            txtWorkspaceName = itemView.findViewById(R.id.txtWorkspaceName);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }
    }
}


