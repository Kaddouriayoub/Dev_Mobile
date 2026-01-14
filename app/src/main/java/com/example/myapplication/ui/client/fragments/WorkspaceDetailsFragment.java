package com.example.myapplication.ui.client.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.example.myapplication.model.Favorite;
import com.example.myapplication.model.Review;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.ui.client.BookingActivity;
import com.example.myapplication.ui.adapters.ReviewAdapter;
import com.example.myapplication.utils.SessionManager;

import java.io.File;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class WorkspaceDetailsFragment extends Fragment {

    private static final String ARG_ID = "workspace_id";
    private SessionManager sessionManager;
//    private static final long CLIENT_ID = 0L;

    private Realm realm;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        sessionManager = new SessionManager(requireContext());
        View view = inflater.inflate(R.layout.fragment_workspace_details, container, false);

        long workspaceId = getArguments() != null ? getArguments().getLong(ARG_ID) : -1L;
        realm = Realm.getDefaultInstance();

        Workspace w = realm.where(Workspace.class)
                .equalTo("id", workspaceId)
                .findFirst();

        // ===== IMAGE CAROUSEL =====
        ViewPager2 pager = view.findViewById(R.id.image_carousel_client);
        TextView counter = view.findViewById(R.id.tv_image_counter_client);

        if (w != null && w.getImages() != null && !w.getImages().isEmpty()) {
            List<String> images = w.getImages();
            pager.setAdapter(new ImagePagerAdapter(images));
            counter.setText("1 / " + images.size());

            pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    counter.setText((position + 1) + " / " + images.size());
                }
            });
        } else {
            if (counter != null) counter.setText("0 / 0");
        }

        // ===== TEXT =====
        if (w != null) {
            ((TextView) view.findViewById(R.id.txtWorkspaceName)).setText(w.getName());
            ((TextView) view.findViewById(R.id.txtPrice)).setText(w.getPricePerHour() + " DHS/h");
            ((TextView) view.findViewById(R.id.txtAddress)).setText(w.getAddress());
            ((TextView) view.findViewById(R.id.txtStatus)).setText(w.getStatus());
            ((TextView) view.findViewById(R.id.txtDescription)).setText(w.getDescription());
        }

        // ===== REVIEWS LIST =====
        RealmResults<Review> reviews = realm.where(Review.class)
                .equalTo("workspaceId", workspaceId)
                .findAll();

        View commentsCard = view.findViewById(R.id.commentsCard);

        if (reviews == null || reviews.isEmpty()) {
            commentsCard.setVisibility(View.GONE); // ✅ hide entire card
        } else {
            commentsCard.setVisibility(View.VISIBLE);
            RecyclerView rv = view.findViewById(R.id.rvReviews);
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));
            rv.setAdapter(new ReviewAdapter(reviews, realm));
        }

        // ===== RATING SUMMARY (AVG + BARS) =====
        bindRatingSummary(view, reviews);

        // ===== FAVORITE =====
        ImageButton fav = view.findViewById(R.id.btnFavorite);
        Favorite f = realm.where(Favorite.class)
                .equalTo("clientId", sessionManager.getUserId())
                .equalTo("workspaceId", workspaceId)
                .findFirst();
        fav.setSelected(f != null);

        fav.setOnClickListener(v -> {
            realm.executeTransaction(r -> {
                Favorite existing = r.where(Favorite.class)
                        .equalTo("clientId", sessionManager.getUserId())
                        .equalTo("workspaceId", workspaceId)
                        .findFirst();
                if (existing != null) {
                    existing.deleteFromRealm();
                    fav.setSelected(false);
                } else {
                    Favorite nf = r.createObject(Favorite.class, System.currentTimeMillis());
                    nf.setClientId(sessionManager.getUserId());
                    nf.setWorkspaceId(workspaceId);
                    fav.setSelected(true);
                }
            });
        });

        // ===== RESERVE =====
        view.findViewById(R.id.btnReserve).setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), BookingActivity.class);
            i.putExtra("workspace_id", workspaceId);
            startActivity(i);
        });

        return view;
    }

    private void bindRatingSummary(@NonNull View view, @Nullable RealmResults<Review> reviews) {

        TextView txtAvg = view.findViewById(R.id.txtAvgRating);
        RatingBar rbSmall = view.findViewById(R.id.ratingBarSmall);
        TextView txtCount = view.findViewById(R.id.txtReviewCount);

        ProgressBar pb5 = view.findViewById(R.id.pb5);
        ProgressBar pb4 = view.findViewById(R.id.pb4);
        ProgressBar pb3 = view.findViewById(R.id.pb3);
        ProgressBar pb2 = view.findViewById(R.id.pb2);
        ProgressBar pb1 = view.findViewById(R.id.pb1);

        int total = (reviews == null) ? 0 : reviews.size();

        if (total == 0) {
            txtAvg.setText("0.0");
            rbSmall.setRating(0f);
            txtCount.setText("0 avis");

            // set max to 1 (avoid division by 0) and progress to 0
            pb5.setMax(1);
            pb5.setProgress(0);
            pb4.setMax(1);
            pb4.setProgress(0);
            pb3.setMax(1);
            pb3.setProgress(0);
            pb2.setMax(1);
            pb2.setProgress(0);
            pb1.setMax(1);
            pb1.setProgress(0);
            return;
        }

        int c1 = 0, c2 = 0, c3 = 0, c4 = 0, c5 = 0;
        int sum = 0;

        for (Review r : reviews) {
            if (r == null) continue;
            int rating = r.getRating(); // expected 1..5
            if (rating < 1) rating = 1;
            if (rating > 5) rating = 5;

            sum += rating;

            switch (rating) {
                case 1:
                    c1++;
                    break;
                case 2:
                    c2++;
                    break;
                case 3:
                    c3++;
                    break;
                case 4:
                    c4++;
                    break;
                case 5:
                    c5++;
                    break;
            }
        }

        float avg = (float) sum / (float) total;

        txtAvg.setText(String.format(Locale.US, "%.1f", avg));
        rbSmall.setRating(avg);
        txtCount.setText(total + (total <= 1 ? " avis" : " avis"));

        // progress bars: set max to total so proportions display nicely
        pb5.setMax(total);
        pb5.setProgress(c5);
        pb4.setMax(total);
        pb4.setProgress(c4);
        pb3.setMax(total);
        pb3.setProgress(c3);
        pb2.setMax(total);
        pb2.setProgress(c2);
        pb1.setMax(total);
        pb1.setProgress(c1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realm != null) realm.close();
    }

    public static WorkspaceDetailsFragment newInstance(long workspaceId) {
        Bundle args = new Bundle();
        args.putLong(ARG_ID, workspaceId);
        WorkspaceDetailsFragment fragment = new WorkspaceDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // ===== IMAGE ADAPTER =====
    class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.VH> {

        private final List<String> images;

        ImagePagerAdapter(List<String> images) {
            this.images = images;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            iv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new VH(iv);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            File f = new File(images.get(position));
            if (f.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                holder.image.setImageBitmap(bmp);
            }
        }

        @Override
        public int getItemCount() {
            return images == null ? 0 : images.size();
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView image;

            VH(View v) {
                super(v);
                image = (ImageView) v;
            }
        }
    }
}
