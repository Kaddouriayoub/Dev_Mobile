package com.example.myapplication.ui.client.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Favorite;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.utils.SessionManager;

import java.io.File;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class ExploreFragment extends Fragment {

    private Realm realm;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.rvPopularSpaces);
        EditText etSearch = view.findViewById(R.id.etSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        realm = Realm.getDefaultInstance();

        // Initial load
        loadWorkspaces("");

        // 🔍 Search on ENTER
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {

                String query = etSearch.getText().toString().trim();
                loadWorkspaces(query);
                return true;
            }
            return false;
        });

        return view;
    }

    private void loadWorkspaces(String query) {

        RealmResults<Workspace> results;

        if (TextUtils.isEmpty(query)) {
            results = realm.where(Workspace.class)
                    .equalTo("status", "AVAILABLE")
                    .findAll();
        } else {
            results = realm.where(Workspace.class)
                    .equalTo("status", "AVAILABLE")
                    .beginGroup()
                    .contains("name", query, Case.INSENSITIVE)
                    .or()
                    .contains("description", query, Case.INSENSITIVE)
                    .or()
                    .contains("address", query, Case.INSENSITIVE)
                    .or()
                    .contains("city", query, Case.INSENSITIVE)
                    .endGroup()
                    .findAll();
        }

        // Adapter that shows ONLY ONE image in the card: the first image of w.getImages()
        SingleImageWorkspaceAdapter adapter =
                new SingleImageWorkspaceAdapter(results, workspaceId -> {

                    WorkspaceDetailsFragment fragment =
                            WorkspaceDetailsFragment.newInstance(workspaceId);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                });

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) realm.close();
    }

    // =========================================================
    // Adapter (ONE IMAGE PER CARD)
    // =========================================================
    interface OnWorkspaceClick {
        void onClick(long workspaceId);
    }

    class SingleImageWorkspaceAdapter extends RecyclerView.Adapter<SingleImageWorkspaceAdapter.VH> {

        private final RealmResults<Workspace> data;
        private final OnWorkspaceClick listener;

        SingleImageWorkspaceAdapter(RealmResults<Workspace> data, OnWorkspaceClick listener) {
            this.data = data;
            this.listener = listener;
            setHasStableIds(true);
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // ⚠️ Put here the layout file that contains your card XML (the one you pasted)
            // Example names: item_workspace_card, item_workspace_client, item_workspace
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_coworking_space, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Workspace w = data.get(position);
            if (w == null) return;

            // ===== ONE IMAGE TO FILL THE CARD =====
            // Take the FIRST image from w.getImages()
            String firstPath = null;
            List<String> imgs = w.getImages();
            if (imgs != null && !imgs.isEmpty()) firstPath = imgs.get(0);

            if (!TextUtils.isEmpty(firstPath) && new File(firstPath).exists()) {
                h.ivSpace.setImageBitmap(decodeScaled(firstPath, 1080, 600));
            } else {
                // fallback (keep your sample if you want)
                h.ivSpace.setImageResource(R.drawable.sample_office);
            }

            // ===== TEXT =====
            // Fix title: show workspace name + city (not city + address)
            String workspaceName = w.getName();
            if (TextUtils.isEmpty(workspaceName)) {
                workspaceName = "Workspace";
            }
            h.tvLocation.setText(workspaceName + " · " + w.getCity());
            h.tvDescription.setText(w.getDescription());

            // ===== STATUS WITH STYLING =====
            String status = w.getStatus();
            h.tvStatus.setText(status);
            h.tvStatus.setTextColor(Color.WHITE);

            switch (status) {
                case "AVAILABLE":
                    h.tvStatus.setBackgroundColor(Color.parseColor("#2E7D32"));
                    break;
                case "FULL":
                    h.tvStatus.setBackgroundColor(Color.parseColor("#C62828"));
                    break;
                case "MAINTENANCE":
                    h.tvStatus.setBackgroundColor(Color.parseColor("#F9A825"));
                    break;
                default:
                    h.tvStatus.setBackgroundColor(Color.GRAY);
            }

            h.tvPrice.setText(w.getPricePerHour() + " DHS/h");

            // ===== FAVORITE TOGGLE =====
            SessionManager sessionManager = new SessionManager(h.itemView.getContext());
            Realm realmInstance = Realm.getDefaultInstance();
            Favorite existingFav = realmInstance.where(Favorite.class)
                    .equalTo("clientId", sessionManager.getUserId())
                    .equalTo("workspaceId", w.getId())
                    .findFirst();

            h.ivFavorite.setSelected(existingFav != null);

            h.ivFavorite.setOnClickListener(v -> {
                realmInstance.executeTransaction(r -> {
                    Favorite existing = r.where(Favorite.class)
                            .equalTo("clientId", sessionManager.getUserId())
                            .equalTo("workspaceId", w.getId())
                            .findFirst();

                    if (existing != null) {
                        existing.deleteFromRealm();
                        h.ivFavorite.setSelected(false);
                    } else {
                        Number maxId = r.where(Favorite.class).max("id");
                        long nextId = (maxId == null) ? 1 : maxId.longValue() + 1;

                        Favorite f = r.createObject(Favorite.class, nextId);
                        f.setClientId(sessionManager.getUserId());
                        f.setWorkspaceId(w.getId());
                        f.setCreatedAt(String.valueOf(System.currentTimeMillis()));

                        h.ivFavorite.setSelected(true);
                    }
                });
            });

            // ===== CLICK =====
            h.itemView.setOnClickListener(v -> listener.onClick(w.getId()));
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public long getItemId(int position) {
            Workspace w = data.get(position);
            return (w != null) ? w.getId() : position;
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView ivSpace, ivFavorite;
            TextView tvLocation, tvDescription, tvStatus, tvPrice;

            VH(@NonNull View itemView) {
                super(itemView);
                ivSpace = itemView.findViewById(R.id.ivSpace);
                ivFavorite = itemView.findViewById(R.id.ivFavorite);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }
        }

        // Decode image with downscaling to avoid OOM
        private Bitmap decodeScaled(String path, int reqW, int reqH) {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);

            int inSampleSize = 1;
            while ((o.outWidth / inSampleSize) > reqW || (o.outHeight / inSampleSize) > reqH) {
                inSampleSize *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = inSampleSize;
            return BitmapFactory.decodeFile(path, o2);
        }
    }
}
