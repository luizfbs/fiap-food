package br.com.fiap.fiapfood;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.design.widget.NavigationView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.util.List;

import br.com.fiap.fiapfood.fragments.AboutFragment;
import br.com.fiap.fiapfood.fragments.FormFragment;
import br.com.fiap.fiapfood.fragments.IFilterableFragment;
import br.com.fiap.fiapfood.fragments.ListFragment;
import br.com.fiap.fiapfood.fragments.MapsFragment;

import br.com.fiap.fiapfood.helpers.AuthHelper;
import br.com.fiap.fiapfood.helpers.SyncHelper;

import br.com.fiap.fiapfood.models.Restaurant;
import br.com.fiap.fiapfood.models.RestaurantCost;
import br.com.fiap.fiapfood.models.RestaurantType;
import br.com.fiap.fiapfood.models.User;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 103;

    private static String BUNDLE_ACTIVE_FRAGMENT_KEY = "BUNDLE_ACTIVE_FRAGMENT_KEY";
    private static String BUNDLE_ACTIVE_TITLE_KEY = "BUNDLE_ACTIVE_TITLE_KEY";

    public User activeUser;
    public Fragment activeFragment;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.drawer_layout) DrawerLayout drawer;
    public @Bind(R.id.nav_view) NavigationView navigationView;
    public @Bind(R.id.fab) FloatingActionButton fab;

    public Location location = null;
    public List<Restaurant> restaurants;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private SearchView searchView;
    private boolean isFiltered = false;

    private MenuItem activeTypeFilter;
    private MenuItem activeCostFilter;

    private MenuItem allTypes;
    private MenuItem allCosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthHelper.isAuthenticated(this)) {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);

            finish();
            return;
        }
        activeUser = AuthHelper.getUser(this);

        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);

        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        if(!AuthHelper.isSynchronized(this)){
            SyncHelper.execute(this);
        }

        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            activeFragment = fragmentManager.getFragment(savedInstanceState, BUNDLE_ACTIVE_FRAGMENT_KEY);
            setTitle(savedInstanceState.getString(BUNDLE_ACTIVE_TITLE_KEY));
        }

        if (activeFragment == null) {
            activeFragment = new MapsFragment();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.content, activeFragment);
            fragmentTransaction.commit();
            setTitle(getString(R.string.map_mode));
        }

        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.userName)).setText(activeUser.name);
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.userEmail)).setText(activeUser.email);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_required);

                builder.setMessage(R.string.this_app_requires_location_permissions_to_work);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        requestAccessFineLocationPermission();

                    }
                });

                android.app.AlertDialog dialog = builder.create();
                dialog.show();

            } else {

                requestAccessFineLocationPermission();

            }
        } else {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if(location != null)
                Log.v(TAG, location.getLatitude() + ", " + location.getLongitude());

        }

        // first run, feed data
        if( RestaurantCost.retrieve().size() == 0){
            RestaurantCost.feed();
        }

        // first run, feed data
        if( RestaurantType.retrieve().size() == 0){
            RestaurantType.feed();
        }

        refreshRestaurants();
    }

    private void requestAccessFineLocationPermission() {

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        );

    }

    @OnClick(R.id.fab)
    public void startFormNew(View view) {
        changeFragment(new FormFragment());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0){
                filterRestaurants();
                getSupportFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        String[] restaurant_types = getResources().getStringArray(R.array.restaurant_types);
        restaurant_types[0] = getString(R.string.all);
        MenuItem filter_type = menu.findItem(R.id.filters_type);
        for(int i = 0; i < restaurant_types.length; i++){
            MenuItem item = filter_type.getSubMenu().add(101, i, i, restaurant_types[i]);
            if(i == 0){
                item.setCheckable(true).setChecked(true);
                activeTypeFilter = item;
                allTypes = item;
            }
        }

        String[] restaurant_costs = getResources().getStringArray(R.array.restaurant_costs);
        restaurant_costs[0] = getString(R.string.all);
        MenuItem filter_cost = menu.findItem(R.id.filters_cost);
        for(int i = 0; i < restaurant_costs.length; i++){
            MenuItem item = filter_cost.getSubMenu().add(102, i, i, restaurant_costs[i]);
            if(i == 0){
                item.setCheckable(true).setChecked(true);
                activeCostFilter = item;
                allCosts = item;
            }
        }

        MenuItem searchItem = menu.findItem(R.id.search);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                fab.setVisibility(View.GONE);
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                fab.setVisibility(View.VISIBLE);
                searchView.setQuery("", false);

                filterRestaurants();
                return true;
            }
        });

        searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterRestaurants();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        ImageView searchClose = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery("", false);
                filterRestaurants();
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int groupId = item.getGroupId();
        int id = item.getItemId();

        if (id == R.id.filters) {
            return true;
        }else if(id == R.id.filters_type){
            return true;
        }else if(id == R.id.filters_cost){
            return true;
        } else{
            if(groupId == 101){
                if(activeTypeFilter != null)
                    activeTypeFilter.setCheckable(false);

                item.setCheckable(true).setChecked(true);
                activeTypeFilter = item;

                filterRestaurants();
                return true;
            }else if(groupId == 102){
                if(activeCostFilter != null)
                    activeCostFilter.setCheckable(false);

                item.setCheckable(true).setChecked(true);
                activeCostFilter = item;

                filterRestaurants();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.new_restaurant) {
            changeFragment(new FormFragment());
        } else if (id == R.id.list_restaurants) {
            changeFragment(new ListFragment());
        } else if (id == R.id.maps_restaurants) {
            changeFragment(new MapsFragment());
        } else if (id == R.id.sync_restaurants) {
            SyncHelper.execute(this);
        } else if (id == R.id.about) {
            changeFragment(new AboutFragment());
        } else if (id == R.id.logout) {
            AuthHelper.clearSession(this);
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    public void changeFragment(Fragment fragment){
        if (fragment.getClass().isInstance(activeFragment))
            return;

        // clear filters
        resetFilters();
        // end

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        fragmentTransaction.replace(R.id.content, fragment);
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }

    public void refreshRestaurants(){
        restaurants = Restaurant.retrieve(activeUser.getId());
    }

    public void resetFilters(){
        activeTypeFilter.setCheckable(false);
        activeCostFilter.setCheckable(false);

        activeTypeFilter = allTypes;
        activeTypeFilter.setCheckable(true).setChecked(true);

        activeCostFilter = allCosts;
        activeCostFilter.setCheckable(true).setChecked(true);

        searchView.setQuery("", false);
        filterRestaurants();
    }

    public void filterRestaurants(){
        String query = searchView.getQuery().toString();

        RestaurantType typeFilter = null;
        long typeFilterId = -1;

        if(activeTypeFilter != null) {
            typeFilter = RestaurantType.find(activeTypeFilter.getTitle().toString());
            if(typeFilter != null){
                typeFilterId = typeFilter.getId();
            }
        }

        RestaurantCost costFilter = null;
        long costFilterId = -1;

        if(activeCostFilter != null) {
            costFilter = RestaurantCost.find(activeCostFilter.getTitle().toString());
            if(costFilter != null) {
                costFilterId = costFilter.getId();
            }
        }

        isFiltered = query.length() > 0 || typeFilterId > -1 ||  costFilterId > -1;
        restaurants = Restaurant.find(activeUser.getId(), query, typeFilterId, costFilterId);

        if(IFilterableFragment.class.isInstance(activeFragment)) {
            IFilterableFragment fragment = (IFilterableFragment) getSupportFragmentManager().findFragmentById(R.id.content);
            fragment.onFilterResult();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        fragmentManager.putFragment(outState, BUNDLE_ACTIVE_FRAGMENT_KEY, activeFragment);
        outState.putString(BUNDLE_ACTIVE_TITLE_KEY, getTitle().toString());
    }

    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            location = loc;
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.v(TAG, "GPS Disabled");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.v(TAG, "GPS Enabled");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                } else {

                    Toast.makeText(this, R.string.this_app_requires_location_permissions_to_work,
                            Toast.LENGTH_LONG).show();
                    finish();

                }
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(!AuthHelper.keepConnected(this)){
            AuthHelper.clearSession(this);
        }
        super.onDestroy();
    }
}
