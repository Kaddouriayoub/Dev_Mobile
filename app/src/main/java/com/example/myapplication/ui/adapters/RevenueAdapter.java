package com.example.myapplication.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.RevenueData;
import java.util.ArrayList;
import java.util.List;

public class RevenueAdapter extends RecyclerView.Adapter<RevenueAdapter.ViewHolder> {

    private Context context;
    private List<RevenueData> revenueList;

    public RevenueAdapter(Context context) {
        this.context = context;
        this.revenueList = new ArrayList<>();
    }

    public void setRevenueList(List<RevenueData> revenueList) {
        this.revenueList = revenueList != null ? revenueList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_revenue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RevenueData data = revenueList.get(position);

        holder.tvWorkspaceName.setText(data.getWorkspaceName());
        holder.tvTotalRevenue.setText(String.format("%.2f dh", data.getTotalRevenue()));
        holder.tvOrderCount.setText(String.valueOf(data.getTotalOrderCount()));
    }

    @Override
    public int getItemCount() {
        return revenueList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkspaceName, tvTotalRevenue, tvOrderCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkspaceName = itemView.findViewById(R.id.tv_workspace_name);
            tvTotalRevenue = itemView.findViewById(R.id.tv_total_revenue);
            tvOrderCount = itemView.findViewById(R.id.tv_order_count);
        }
    }
}
