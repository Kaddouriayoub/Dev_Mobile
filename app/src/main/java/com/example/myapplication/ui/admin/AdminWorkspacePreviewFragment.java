package com.example.myapplication.ui.admin;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.model.Review;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.ui.adapters.ImageCarouselAdapter;
import com.example.myapplication.ui.adapters.ReviewAdapter;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class AdminWorkspacePreviewFragment extends Fragment {

    private static final String ARG_WORKSPACE_ID = "WORKSPACE_ID";

    private ViewPager2 imageCarousel;
    private TextView tvImageCounter, tvName, tvPrice, tvType, tvLocation, tvStatus, tvCapacity, tvDescription;
    private LinearLayout indicatorContainer;
    private ImageView btnBack;
    private Button btnEdit;

    // Reviews
    private TextView tvAvgRating, tvReviewCount;
    private RatingBar ratingBarSmall;
    private ProgressBar pb5, pb4, pb3, pb2, pb1;
    private RecyclerView rvReviews;

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

        // Reviews views
        tvAvgRating = view.findViewById(R.id.tv_avg_rating);
        tvReviewCount = view.findViewById(R.id.tv_review_count);
        ratingBarSmall = view.findViewById(R.id.rating_bar_small);
        pb5 = view.findViewById(R.id.pb5);
        pb4 = view.findViewById(R.id.pb4);
        pb3 = view.findViewById(R.id.pb3);
        pb2 = view.findViewById(R.id.pb2);
        pb1 = view.findViewById(R.id.pb1);
        rvReviews = view.findViewById(R.id.rv_reviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setNestedScrollingEnabled(false);

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
        tvPrice.setText(String.format("%.2f dh/h", workspace.getPricePerHour()));

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

        // Load reviews
        loadReviews();
    }

    private void loadReviews() {
        // Get all reviews for this workspace
        RealmResults<Review> reviews = realm.where(Review.class)
                .equalTo("workspaceId", workspaceId)
                .findAll();

        int count = reviews.size();
        float avg = 0f;
        int[] dist = new int[5]; // [0]=1-star ... [4]=5-star

        if (count > 0) {
            int sum = 0;
            for (Review r : reviews) {
                int rating = r.getRating();
                if (rating < 1) rating = 1;
                if (rating > 5) rating = 5;
                sum += rating;
                dist[rating - 1]++;
            }
            avg = (float) sum / count;
        }

        // Display average rating
        tvAvgRating.setText(String.format(java.util.Locale.getDefault(), "%.1f", avg));
        tvReviewCount.setText(count + " avis");
        ratingBarSmall.setRating(avg);

        // Display distribution bars
        int max = Math.max(count, 1);
        pb5.setMax(max);
        pb5.setProgress(dist[4]);
        pb4.setMax(max);
        pb4.setProgress(dist[3]);
        pb3.setMax(max);
        pb3.setProgress(dist[2]);
        pb2.setMax(max);
        pb2.setProgress(dist[1]);
        pb1.setMax(max);
        pb1.setProgress(dist[0]);

        // Display reviews list
        ReviewAdapter adapter = new ReviewAdapter(reviews, realm);
        rvReviews.setAdapter(adapter);
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
