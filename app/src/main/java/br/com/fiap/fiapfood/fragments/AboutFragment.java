package br.com.fiap.fiapfood.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.fiap.fiapfood.MainActivity;
import br.com.fiap.fiapfood.R;


public class AboutFragment extends Fragment {


    public AboutFragment() {
        // Required empty public constructor
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
        activity.setTitle(R.string.about);

        activity.fab.setVisibility(View.GONE);
        activity.navigationView.getMenu().findItem(R.id.about).setChecked(true);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

}
