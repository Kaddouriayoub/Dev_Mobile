package com.example.myapplication.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Reservation;

import io.realm.RealmResults;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private Context context;
    private RealmResults<Reservation> reservations;
    private OnStatusChangeListener listener;

    public interface OnStatusChangeListener {
        void onChangeStatus(Reservation reservation);
    }

    public OrderAdapter(Context context, RealmResults<Reservation> reservations, OnStatusChangeListener listener) {
        this.context = context;
        this.reservations = reservations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        if (reservation != null) {
            holder.tvOrderId.setText("Order #" + reservation.getId());
            
            if (reservation.getStatus() != null) {
                holder.tvStatus.setText(reservation.getStatus().name());
            }

            // Using relationships
            if (reservation.getClient() != null) {
                // Client doesn't have name, maybe link to User later? 
                holder.tvClientName.setText("Client #" + reservation.getClient().getId());
            } else {
                holder.tvClientName.setText("Client #" + reservation.getClientId());
            }

            if (reservation.getWorkspace() != null) {
                holder.tvWorkspaceName.setText(reservation.getWorkspace().getName());
            }

            holder.tvTotalPrice.setText(String.format("$%.2f", reservation.getTotalPrice()));
            holder.tvDate.setText(reservation.getReservationDate());

            holder.btnChangeStatus.setOnClickListener(v -> listener.onChangeStatus(reservation));
        }
    }

    @Override
    public int getItemCount() {
        return reservations != null ? reservations.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvClientName, tvWorkspaceName, tvTotalPrice, tvDate;
        Button btnChangeStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvClientName = itemView.findViewById(R.id.tv_client_name);
            tvWorkspaceName = itemView.findViewById(R.id.tv_workspace_name);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnChangeStatus = itemView.findViewById(R.id.btn_change_status);
        }
    }
}
