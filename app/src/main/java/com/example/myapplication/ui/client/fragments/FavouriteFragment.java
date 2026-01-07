package com.example.myapplication.ui.client.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Favorite;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.ui.adapters.ClientWorkspaceAdapter;

import io.realm.Realm;
import io.realm.RealmResults;

public class FavouriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private Realm realm;
    private int currentClientId = 0; // Remplace par l'ID réel du client connecté

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
        // 1. Récupérer tous les favoris de ce client
        RealmResults<Favorite> myFavorites = realm.where(Favorite.class)
                .equalTo("clientId", currentClientId)
                .findAll();

        if (myFavorites.isEmpty()) {
            Toast.makeText(getContext(), "Aucun favori trouvé", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Extraire les IDs des Workspaces aimés
        Long[] workspaceIds = new Long[myFavorites.size()];
        for (int i = 0; i < myFavorites.size(); i++) {
            Favorite fav = myFavorites.get(i);
            if (fav != null) {
                workspaceIds[i] = fav.getWorkspaceId();
            }
        }

        // 3. Faire une nouvelle requête pour obtenir les Workspaces correspondant à ces IDs
        // La méthode .in("champ", tableau[]) permet de filtrer par liste
        RealmResults<Workspace> favoriteWorkspaces = realm.where(Workspace.class)
                .in("id", workspaceIds)
                .findAll();

        // 4. Passer le résultat (qui est bien un RealmResults<Workspace>) à l'adaptateur existant
        ClientWorkspaceAdapter adapter = new ClientWorkspaceAdapter(
                favoriteWorkspaces,
                new ClientWorkspaceAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(long workspaceId) {
                        // Action au clic (ex: ouvrir les détails)
                        Toast.makeText(getContext(), "Click sur ID: " + workspaceId, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}