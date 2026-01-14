package com.example.myapplication.ui.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Reservation;
import com.example.myapplication.model.ReservationStatus;
import com.example.myapplication.model.RevenueData;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.ui.adapters.RevenueAdapter;
import com.example.myapplication.utils.SessionManager;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminRevenueFragment extends Fragment {

    private RecyclerView recyclerView;
    private Spinner spinnerPeriodType, spinnerWorkspace;
    private TextView tvTotalRevenueSummary, tvTotalOrdersSummary, tvSelectedDate;
    private Realm realm;
    private RevenueAdapter adapter;

    private String[] periodTypes = {"Journalier", "Mensuel"};
    private String currentPeriodType = "Journalier";
    private Long selectedWorkspaceId = null;

    private int selectedYear, selectedMonth, selectedDay;

    private List<Workspace> workspaceList;
    private List<String> workspaceNames;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_revenue, container, false);

        // Initialize Views
        recyclerView = view.findViewById(R.id.recycler_revenue);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        spinnerPeriodType = view.findViewById(R.id.spinner_period_type);
        spinnerWorkspace = view.findViewById(R.id.spinner_workspace);
        tvTotalRevenueSummary = view.findViewById(R.id.tv_total_revenue_summary);
        tvTotalOrdersSummary = view.findViewById(R.id.tv_total_orders_summary);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);

        // Initialize selected date with current date
        Calendar now = Calendar.getInstance();
        selectedYear = now.get(Calendar.YEAR);
        selectedMonth = now.get(Calendar.MONTH) + 1;
        selectedDay = now.get(Calendar.DAY_OF_MONTH);
        updateDateDisplay();

        // Initialize Realm
        realm = Realm.getDefaultInstance();

        // Setup Adapter
        adapter = new RevenueAdapter(getContext());
        recyclerView.setAdapter(adapter);

        // Setup Date Picker
        setupDatePicker();

        // Setup Spinners
        setupPeriodTypeSpinner();
        setupWorkspaceSpinner();

        // Load Revenue Data
        loadRevenueData();

        return view;
    }

    private void setupPeriodTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                periodTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriodType.setAdapter(adapter);

        spinnerPeriodType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentPeriodType = periodTypes[position];
                updateDateDisplay();
                loadRevenueData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupWorkspaceSpinner() {
        // Get current admin's workspaces
        SessionManager sessionManager = new SessionManager(getContext());
        Long currentAdminId = sessionManager.getUserId();

        workspaceList = realm.where(Workspace.class)
                .equalTo("adminId", currentAdminId)
                .findAll();

        // Build workspace names list with "All" option
        workspaceNames = new ArrayList<>();
        workspaceNames.add("Tous les workspaces");
        for (Workspace workspace : workspaceList) {
            workspaceNames.add(workspace.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                workspaceNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWorkspace.setAdapter(adapter);

        spinnerWorkspace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedWorkspaceId = null; // "All"
                } else {
                    selectedWorkspaceId = workspaceList.get(position - 1).getId();
                }
                loadRevenueData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupDatePicker() {
        tvSelectedDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedYear = year;
                        selectedMonth = month + 1;
                        selectedDay = dayOfMonth;
                        updateDateDisplay();
                        loadRevenueData();
                    },
                    selectedYear,
                    selectedMonth - 1,
                    selectedDay
            );
            datePickerDialog.show();
        });
    }

    private void updateDateDisplay() {
        if (currentPeriodType.equals("Journalier")) {
            tvSelectedDate.setText(String.format("%d-%d-%d", selectedYear, selectedMonth, selectedDay));
        } else {
            tvSelectedDate.setText(String.format("%d-%d", selectedYear, selectedMonth));
        }
    }

    private void loadRevenueData() {
        // Get current date period string
        String currentPeriod = getCurrentPeriodString();

        // Calculate revenue data
        List<RevenueData> revenueDataList = calculateRevenueByWorkspace(currentPeriod);

        // Update summary totals
        double totalRevenue = 0.0;
        int totalOrders = 0;
        for (RevenueData data : revenueDataList) {
            totalRevenue += data.getTotalRevenue();
            totalOrders += data.getTotalOrderCount();
        }

        tvTotalRevenueSummary.setText(String.format("%.2f dh", totalRevenue));
        tvTotalOrdersSummary.setText(String.valueOf(totalOrders));

        // Update RecyclerView
        adapter.setRevenueList(revenueDataList);
    }

    private String getCurrentPeriodString() {
        if (currentPeriodType.equals("Journalier")) {
            return selectedYear + "-" + selectedMonth + "-" + selectedDay;
        } else {
            return selectedYear + "-" + selectedMonth;
        }
    }

    private List<RevenueData> calculateRevenueByWorkspace(String period) {
        SessionManager sessionManager = new SessionManager(getContext());
        Long currentAdminId = sessionManager.getUserId();

        // Query reservations: ONLY COMPLETED for revenue calculation
        RealmResults<Reservation> allReservations = realm.where(Reservation.class)
                .equalTo("workspace.adminId", currentAdminId)
                .equalTo("status", ReservationStatus.COMPLETED.name())
                .findAll();

        // Filter by period and workspace
        Map<Long, RevenueData> revenueMap = new HashMap<>();

        for (Reservation reservation : allReservations) {
            // Period filter
            if (!matchesPeriod(reservation.getReservationDate(), period)) {
                continue;
            }

            // Workspace filter
            Long workspaceId = reservation.getWorkspaceId();
            if (selectedWorkspaceId != null && !selectedWorkspaceId.equals(workspaceId)) {
                continue;
            }

            // Aggregate data
            if (!revenueMap.containsKey(workspaceId)) {
                Workspace workspace = reservation.getWorkspace();
                String workspaceName = workspace != null ? workspace.getName() : "Unknown";
                revenueMap.put(workspaceId, new RevenueData(workspaceId, workspaceName));
            }

            RevenueData data = revenueMap.get(workspaceId);
            data.addRevenue(reservation.getTotalPrice());
            data.incrementCompleted();
        }

        return new ArrayList<>(revenueMap.values());
    }

    private boolean matchesPeriod(String reservationDate, String period) {
        if (reservationDate == null || period == null) {
            return false;
        }

        if (currentPeriodType.equals("Journalier")) {
            return reservationDate.equals(period);
        } else {
            return reservationDate.startsWith(period + "-");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (realm != null && !realm.isClosed()) {
            loadRevenueData();
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
