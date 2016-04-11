package br.com.fiap.fiapfood.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.fiap.fiapfood.MainActivity;
import br.com.fiap.fiapfood.R;
import br.com.fiap.fiapfood.adapters.RestaurantAdapter;
import br.com.fiap.fiapfood.models.Restaurant;

public class ListFragment extends Fragment implements IFilterableFragment {

    private RecyclerView recyclerView;
    private TextView noRestaurants;

    public ListFragment() {
    }

    public static ListFragment newInstance() {
        ListFragment fragment = new ListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        MainActivity activity = (MainActivity) getActivity();
        activity.activeFragment = this;
        activity.setTitle(R.string.list_mode);

        activity.fab.setVisibility(View.VISIBLE);
        activity.navigationView.getMenu().findItem(R.id.list_restaurants).setChecked(true);

        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // Set the adapter
        if (view instanceof LinearLayout) {
            Context context = view.getContext();

            noRestaurants = (TextView) view.findViewById(R.id.noRestaurants);
            recyclerView = (RecyclerView) view.findViewById(R.id.list);

            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            onFilterResult();
        }

        return view;
    }

    public void onFilterResult(){
        MainActivity activity = (MainActivity) getActivity();
        if(activity.restaurants.size() == 0){
            noRestaurants.setVisibility(View.VISIBLE);
        }else{
            noRestaurants.setVisibility(View.GONE);
        }
        recyclerView.setAdapter(new RestaurantAdapter(activity.restaurants));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
