package com.example.myapplication.ui.client.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Favorite;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.ui.adapters.ClientWorkspaceAdapter;
import com.example.myapplication.utils.SessionManager;

import io.realm.Realm;
import io.realm.RealmResults;

public class FavouriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private Realm realm;
//    private int currentClientId = 0; // Remplace par l'ID réel du client connecté
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);

        recyclerView = view.findViewById(R.id.rvFavoriteSpaces);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        realm = Realm.getDefaultInstance();

        loadFavorites();

        return view;
    }

    private void loadFavorites() {
        sessionManager = new SessionManager(requireContext());
        // 1. Get all favorites for this client
        RealmResults<Favorite> myFavorites = realm.where(Favorite.class)
                .equalTo("clientId", sessionManager.getUserId())
                .sort("createdAt", io.realm.Sort.DESCENDING)
                .findAll();

        if (myFavorites.isEmpty()) {
            Toast.makeText(getContext(), "Aucun favori trouvé", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Extract Workspace IDs
        Long[] workspaceIds = new Long[myFavorites.size()];
        for (int i = 0; i < myFavorites.size(); i++) {
            Favorite fav = myFavorites.get(i);
            if (fav != null) {
                workspaceIds[i] = fav.getWorkspaceId();
            }
        }

        // 3. Query Workspaces from those IDs
        RealmResults<Workspace> favoriteWorkspaces = realm.where(Workspace.class)
                .in("id", workspaceIds)
                .findAll();

        // 4. Pass results to Adapter with Navigation Logic
        ClientWorkspaceAdapter adapter = new ClientWorkspaceAdapter(
                requireContext(),
                favoriteWorkspaces,
                workspaceId -> {
                    // Same logic as ExploreFragment: Navigate to details
                    WorkspaceDetailsFragment fragment =
                            WorkspaceDetailsFragment.newInstance(workspaceId);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null) // Allows user to press 'Back' to return to Favorites
                            .commit();
                }
        );

        recyclerView.setAdapter(adapter);
        // ... après recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // On ne gère pas le déplacement (drag & drop)
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Workspace workspaceToRemove = favoriteWorkspaces.get(position);

                if (workspaceToRemove != null) {
                    long wsId = workspaceToRemove.getId();

                    // Supprimer de la base de données Realm
                    realm.executeTransaction(r -> {
                        Favorite fav = r.where(Favorite.class)
                                .equalTo("clientId", sessionManager.getUserId())
                                .equalTo("workspaceId", wsId)
                                .findFirst();
                        if (fav != null) {
                            fav.deleteFromRealm();
                        }
                    });

                    // Mettre à jour l'affichage
                    // Comme vous utilisez RealmResults, la liste se met à jour automatiquement si l'adapter est bien configuré
                    // Sinon, on recharge simplement pour plus de sécurité :
                    loadFavorites();
                    Toast.makeText(getContext(), "Favori supprimé", Toast.LENGTH_SHORT).show();
                }
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}