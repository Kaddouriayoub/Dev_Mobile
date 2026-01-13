package com.example.myapplication.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Reservation;
import com.google.android.material.chip.Chip;

import java.util.List;

public class PastReservationAdapter extends RecyclerView.Adapter<PastReservationAdapter.VH> {

    public interface OnItemClick {
        void onClick(Reservation reservation);
    }

    private final List<Reservation> data;
    private final OnItemClick listener;

    public PastReservationAdapter(List<Reservation> data, OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation_past, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Reservation r = data.get(position);

        String name = (r.getWorkspace() != null) ? r.getWorkspace().getName() : "Workspace";
        h.txtWorkspaceName.setText(name);

        h.txtDate.setText(r.getReservationDate());
        h.txtTime.setText(r.getStartTime() + " → " + r.getEndTime());
        h.txtTotal.setText("Total: " + (int) r.getTotalPrice() + "€");

        // you can map status string here if you want
        h.chipStatus.setText("Terminé");

        h.itemView.setOnClickListener(v -> listener.onClick(r));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtWorkspaceName, txtDate, txtTime, txtTotal;
        Chip chipStatus;

        VH(@NonNull View itemView) {
            super(itemView);
            txtWorkspaceName = itemView.findViewById(R.id.txtWorkspaceName);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }
    }
}

