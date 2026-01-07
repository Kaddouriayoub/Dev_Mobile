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
import com.example.myapplication.model.ReservationStatus;
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
            // Order ID with admin badge if applicable
            String orderId = "Order #" + reservation.getId();
            if (reservation.isAdminOrder()) {
                orderId += " (Admin)";
            }
            holder.tvOrderId.setText(orderId);

            // Status with color coding
            if (reservation.getStatus() != null) {
                ReservationStatus status = reservation.getStatus();
                holder.tvStatus.setText(status.name());

                switch (status) {
                    case CONFIRMED:
                        holder.tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed);
                        break;
                    case PENDING:
                        holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                        break;
                    case REFUSED:
                        holder.tvStatus.setBackgroundResource(R.drawable.bg_status_refused);
                        break;
                    case CANCELLED:
                        holder.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                        break;
                }
            }

            // Client name
            if (reservation.getClientName() != null && !reservation.getClientName().isEmpty()) {
                holder.tvClientName.setText(reservation.getClientName());
            } else if (reservation.getClient() != null) {
                holder.tvClientName.setText("Client #" + reservation.getClient().getId());
            } else if (reservation.getClientId() != null) {
                holder.tvClientName.setText("Client #" + reservation.getClientId());
            } else {
                holder.tvClientName.setText("Unknown Client");
            }

            // Workspace name
            if (reservation.getWorkspace() != null) {
                holder.tvWorkspaceName.setText(reservation.getWorkspace().getName());
            } else {
                holder.tvWorkspaceName.setText("Unknown Workspace");
            }

            // Time range
            String startTime = reservation.getStartTime();
            String endTime = reservation.getEndTime();
            if (startTime != null && endTime != null) {
                holder.tvPlaces.setText(startTime + ":00 - " + endTime + ":00");
            } else {
                holder.tvPlaces.setText("--");
            }

            // Price and date
            holder.tvTotalPrice.setText(String.format("%.2f dh", reservation.getTotalPrice()));
            holder.tvDate.setText(reservation.getReservationDate());

            // Change status button
            holder.btnChangeStatus.setOnClickListener(v -> listener.onChangeStatus(reservation));
        }
    }

    @Override
    public int getItemCount() {
        return reservations != null ? reservations.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvClientName, tvWorkspaceName, tvPlaces, tvTotalPrice, tvDate;
        Button btnChangeStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvClientName = itemView.findViewById(R.id.tv_client_name);
            tvWorkspaceName = itemView.findViewById(R.id.tv_workspace_name);
            tvPlaces = itemView.findViewById(R.id.tv_places);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnChangeStatus = itemView.findViewById(R.id.btn_change_status);
        }
    }
}
