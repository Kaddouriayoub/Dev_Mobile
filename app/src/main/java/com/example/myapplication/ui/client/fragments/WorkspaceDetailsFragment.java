package com.example.myapplication.ui.client.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.model.*;
import com.example.myapplication.ui.adapters.ReviewAdapter;
import com.example.myapplication.ui.client.BookingActivity;

import java.io.File;
import java.util.List;

import io.realm.*;

public class WorkspaceDetailsFragment extends Fragment {

    private static final String ARG_ID = "workspace_id";
    private static final long CLIENT_ID = 0L;
    private Realm realm;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_workspace_details, container, false);

        long workspaceId = getArguments().getLong(ARG_ID);
        realm = Realm.getDefaultInstance();

        Workspace w = realm.where(Workspace.class)
                .equalTo("id", workspaceId)
                .findFirst();

        // ===== IMAGE CAROUSEL =====
        ViewPager2 pager = view.findViewById(R.id.image_carousel_client);
        TextView counter = view.findViewById(R.id.tv_image_counter_client);

        if (w != null && w.getImages() != null) {
            List<String> images = w.getImages();
            pager.setAdapter(new ImagePagerAdapter(images));
            counter.setText("1 / " + images.size());

            pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    counter.setText((position + 1) + " / " + images.size());
                }
            });
        }

        // ===== TEXT =====
        ((TextView) view.findViewById(R.id.txtWorkspaceName)).setText(w.getName());
        ((TextView) view.findViewById(R.id.txtPrice)).setText(w.getPricePerHour() + " €/h");
        ((TextView) view.findViewById(R.id.txtAddress)).setText(w.getAddress());
        ((TextView) view.findViewById(R.id.txtStatus)).setText(w.getStatus());
        ((TextView) view.findViewById(R.id.txtDescription)).setText(w.getDescription());

        // ===== REVIEWS =====
        RealmResults<Review> reviews = realm.where(Review.class)
                .equalTo("workspaceId", workspaceId)
                .findAll();

        RecyclerView rv = view.findViewById(R.id.rvReviews);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new ReviewAdapter(reviews, realm));

        // ===== FAVORITE =====
        ImageButton fav = view.findViewById(R.id.btnFavorite);
        Favorite f = realm.where(Favorite.class)
                .equalTo("clientId", CLIENT_ID)
                .equalTo("workspaceId", workspaceId)
                .findFirst();
        fav.setSelected(f != null);

        fav.setOnClickListener(v -> {
            realm.executeTransaction(r -> {
                Favorite existing = r.where(Favorite.class)
                        .equalTo("clientId", CLIENT_ID)
                        .equalTo("workspaceId", workspaceId)
                        .findFirst();
                if (existing != null) {
                    existing.deleteFromRealm();
                    fav.setSelected(false);
                } else {
                    Favorite nf = r.createObject(Favorite.class, System.currentTimeMillis());
                    nf.setClientId(CLIENT_ID);
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
            return images.size();
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
