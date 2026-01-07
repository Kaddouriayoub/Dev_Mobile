package com.example.myapplication.ui.admin;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.ui.adapters.ImageCarouselAdapter;

import io.realm.Realm;
import io.realm.RealmList;

public class AdminWorkspacePreviewFragment extends Fragment {

    private static final String ARG_WORKSPACE_ID = "WORKSPACE_ID";

    private ViewPager2 imageCarousel;
    private TextView tvImageCounter, tvName, tvPrice, tvType, tvLocation, tvStatus, tvCapacity, tvDescription;
    private LinearLayout indicatorContainer;
    private ImageView btnBack;
    private Button btnEdit;

    private Realm realm;
    private Workspace workspace;
    private long workspaceId;

    public static AdminWorkspacePreviewFragment newInstance(long workspaceId) {
        AdminWorkspacePreviewFragment fragment = new AdminWorkspacePreviewFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_WORKSPACE_ID, workspaceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            workspaceId = getArguments().getLong(ARG_WORKSPACE_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_workspace_preview, container, false);

        // Initialize views
        imageCarousel = view.findViewById(R.id.image_carousel);
        tvImageCounter = view.findViewById(R.id.tv_image_counter);
        indicatorContainer = view.findViewById(R.id.indicator_container);
        tvName = view.findViewById(R.id.tv_name);
        tvPrice = view.findViewById(R.id.tv_price);
        tvType = view.findViewById(R.id.tv_type);
        tvLocation = view.findViewById(R.id.tv_location);
        tvStatus = view.findViewById(R.id.tv_status);
        tvCapacity = view.findViewById(R.id.tv_capacity);
        tvDescription = view.findViewById(R.id.tv_description);
        btnBack = view.findViewById(R.id.btn_back);
        btnEdit = view.findViewById(R.id.btn_edit);

        // Initialize Realm
        realm = Realm.getDefaultInstance();

        // Load workspace
        loadWorkspace();

        // Back button
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Edit button
        btnEdit.setOnClickListener(v -> {
            AddEditWorkspaceFragment fragment = new AddEditWorkspaceFragment();
            Bundle args = new Bundle();
            args.putLong("WORKSPACE_ID", workspaceId);
            fragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadWorkspace() {
        workspace = realm.where(Workspace.class).equalTo("id", workspaceId).findFirst();

        if (workspace == null) {
            return;
        }

        // Set basic info
        tvName.setText(workspace.getName());
        tvPrice.setText(String.format("$%.2f/h", workspace.getPricePerHour()));

        if (workspace.getType() != null) {
            tvType.setText(workspace.getType().name().replace("_", " "));
        }

        // Location
        String location = "";
        if (workspace.getAddress() != null && !workspace.getAddress().isEmpty()) {
            location = workspace.getAddress();
        }
        if (workspace.getCity() != null && !workspace.getCity().isEmpty()) {
            if (!location.isEmpty()) {
                location += ", ";
            }
            location += workspace.getCity();
        }
        tvLocation.setText(location.isEmpty() ? "No location" : location);

        // Status with color
        String status = workspace.getStatus();
        if (status != null) {
            tvStatus.setText(status);
            switch (status.toUpperCase()) {
                case "AVAILABLE":
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "FULL":
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    break;
                case "MAINTENANCE":
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    break;
                default:
                    tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            tvStatus.setText("N/A");
        }

        // Capacity
        tvCapacity.setText(workspace.getCapacity() + " places");

        // Description
        String description = workspace.getDescription();
        tvDescription.setText(description != null && !description.isEmpty() ? description : "No description available");

        // Setup image carousel
        setupImageCarousel();
    }

    private void setupImageCarousel() {
        RealmList<String> images = workspace.getImages();

        if (images == null || images.isEmpty()) {
            tvImageCounter.setVisibility(View.GONE);
            indicatorContainer.setVisibility(View.GONE);
            return;
        }

        ImageCarouselAdapter adapter = new ImageCarouselAdapter(getContext(), images);
        imageCarousel.setAdapter(adapter);

        int totalImages = images.size();
        tvImageCounter.setText("1 / " + totalImages);

        // Create indicator dots
        setupIndicators(totalImages);

        // Page change listener
        imageCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tvImageCounter.setText((position + 1) + " / " + totalImages);
                updateIndicators(position);
            }
        });
    }

    private void setupIndicators(int count) {
        indicatorContainer.removeAllViews();

        for (int i = 0; i < count; i++) {
            View dot = new View(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(8, 8);
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0 ? R.drawable.indicator_active : R.drawable.indicator_inactive);
            indicatorContainer.addView(dot);
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < indicatorContainer.getChildCount(); i++) {
            View dot = indicatorContainer.getChildAt(i);
            dot.setBackgroundResource(i == position ? R.drawable.indicator_active : R.drawable.indicator_inactive);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realm != null) {
            realm.close();
        }
    }
}
