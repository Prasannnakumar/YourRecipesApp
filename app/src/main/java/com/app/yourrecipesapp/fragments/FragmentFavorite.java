package com.app.yourrecipesapp.fragments;

import static com.app.yourrecipesapp.utils.Constant.RECIPES_GRID_2_COLUMN;
import static com.app.yourrecipesapp.utils.Constant.RECIPES_GRID_3_COLUMN;
import static com.app.yourrecipesapp.utils.Constant.RECIPES_LIST_BIG;
import static com.app.yourrecipesapp.utils.Constant.RECIPES_LIST_SMALL;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.yourrecipesapp.R;
import com.app.yourrecipesapp.activities.ActivityRecipeDetail;
import com.app.yourrecipesapp.activities.ActivityRecipeDetailOffline;
import com.app.yourrecipesapp.adapters.AdapterFavorite;
import com.app.yourrecipesapp.databases.prefs.SharedPref;
import com.app.yourrecipesapp.databases.sqlite.DbHandler;
import com.app.yourrecipesapp.models.Recipe;
import com.app.yourrecipesapp.utils.Constant;
import com.app.yourrecipesapp.utils.ItemOffsetDecoration;
import com.app.yourrecipesapp.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorite extends Fragment {

    List<Recipe> recipes = new ArrayList<>();
    View root_view;
    AdapterFavorite adapterFavorite;
    DbHandler dbHandler;
    RecyclerView recyclerView;
    LinearLayout linearLayout;
    SharedPref sharedPref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_favorite, container, false);

        if (getActivity() != null) {
            sharedPref = new SharedPref(getActivity());
            dbHandler = new DbHandler(getActivity());
        }

        linearLayout = root_view.findViewById(R.id.lyt_no_favorite);
        recyclerView = root_view.findViewById(R.id.recyclerView);

        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getActivity(), R.dimen.grid_space_recipes);
        recyclerView.addItemDecoration(itemDecoration);

        if (sharedPref.getRecipesViewType() == RECIPES_LIST_SMALL || sharedPref.getRecipesViewType() == RECIPES_LIST_BIG) {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        } else if (sharedPref.getRecipesViewType() == RECIPES_GRID_3_COLUMN) {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        } else if (sharedPref.getRecipesViewType() == RECIPES_GRID_2_COLUMN) {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        }

        adapterFavorite = new AdapterFavorite(getActivity(), recyclerView, recipes);
        recyclerView.setAdapter(adapterFavorite);

        return root_view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        displayData(dbHandler.getAllData());
    }

    private void displayData(List<Recipe> recipes) {
        List<Recipe> favorites = new ArrayList<>();
        if (recipes != null && recipes.size() > 0) {
            favorites.addAll(recipes);
        }
        adapterFavorite.resetListData();
        adapterFavorite.insertData(favorites);

        showNoItemView(favorites.size() == 0);

        adapterFavorite.setOnItemClickListener((view, obj, position) -> {
            if (Tools.isConnect(getActivity())) {
                Intent intent = new Intent(getActivity(), ActivityRecipeDetail.class);
                intent.putExtra(Constant.EXTRA_OBJC, obj);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getActivity(), ActivityRecipeDetailOffline.class);
                intent.putExtra(Constant.EXTRA_OBJC, obj);
                startActivity(intent);
            }
        });
    }

    private void showNoItemView(boolean show) {
        if (show) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
    }

}
