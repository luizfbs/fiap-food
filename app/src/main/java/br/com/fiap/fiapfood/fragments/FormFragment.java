package br.com.fiap.fiapfood.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.fiap.fiapfood.MainActivity;
import br.com.fiap.fiapfood.R;

import br.com.fiap.fiapfood.helpers.BitmapHelper;
import br.com.fiap.fiapfood.helpers.PlacesHelper;
import br.com.fiap.fiapfood.helpers.URIHelper;

import br.com.fiap.fiapfood.models.Place;
import br.com.fiap.fiapfood.models.Restaurant;
import br.com.fiap.fiapfood.models.RestaurantCost;
import br.com.fiap.fiapfood.models.RestaurantType;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FormFragment extends Fragment {

    private static String TAG = "FormFragment";

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;
    private static final int REQUEST_CODE_PICTURE = 1;

    private static String BUNDLE_OUTPUTFILEURI_KEY = "BUNDLE_OUTPUTFILEURI_KEY";
    public static String BUNDLE_RESTAURANT_TO_EDIT_KEY = "BUNDLE_RESTAURANT_TO_EDIT_KEY";

    @Bind(R.id.picture) ImageView picture;
    @Bind(R.id.name) EditText name;
    @Bind(R.id.phone) EditText phone;
    @Bind(R.id.type) Spinner type;
    @Bind(R.id.cost) Spinner cost;
    @Bind(R.id.notes) EditText notes;
    @Bind(R.id.btnRemove) Button btnRemove;

    private String outputFileUri;
    private Restaurant restaurant = null;

    private double latitude = -1;
    private double longitude = -1;

    public FormFragment() {
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
        activity.setTitle(R.string.new_restaurant);

        activity.fab.setVisibility(View.GONE);
        activity.navigationView.getMenu().findItem(R.id.new_restaurant).setChecked(true);

        View view = inflater.inflate(R.layout.fragment_form, container, false);

        ButterKnife.bind(this, view);

        if(savedInstanceState != null){
            outputFileUri = savedInstanceState.getString(BUNDLE_OUTPUTFILEURI_KEY);
            if(outputFileUri != null) showBitmap();
        }

        btnRemove.setVisibility(View.GONE);
        Bundle bundle = getArguments();

        if(bundle != null) {
            Long id = bundle.getLong(BUNDLE_RESTAURANT_TO_EDIT_KEY, -1);

            if (id > -1) {
                restaurant = Restaurant.get(activity.activeUser.getId(), id);

                name.setText(restaurant.name);
                phone.setText(restaurant.phone);

                if(restaurant.type != null)
                    selectValue(type, restaurant.type.name);
                if(restaurant.cost != null)
                    selectValue(cost, restaurant.cost.name);

                notes.setText(restaurant.notes);
                outputFileUri = restaurant.pictureURL;
                if (outputFileUri != null) showBitmap();

                activity.setTitle(R.string.editing_restaurant);
                activity.navigationView.getMenu().findItem(R.id.new_restaurant).setChecked(false);

                btnRemove.setVisibility(View.VISIBLE);
            }
        }else{
            PlacesHelper.execute(getActivity(), this);
        }

        if(activity.location != null){
            latitude = activity.location.getLatitude();
            longitude = activity.location.getLongitude();
        }

        return view;
    }

    public void fillData(Place place){
        name.setText(place.name);
        MainActivity activity = (MainActivity) getActivity();
        if(activity.location != null) {
            latitude = place.geometry.location.lat;
            longitude = place.geometry.location.lng;
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.btnSave)
    public void onSave(View view){
        final MainActivity activity = (MainActivity) getActivity();

        name.setError(null);
        if(name.getText().toString().length() == 0){
            name.setError(getString(R.string.you_must_provide_the_restaurant_s_name));
            return;
        }

        if (restaurant == null) {
            restaurant = new Restaurant();

            restaurant.createdBy = activity.activeUser;
            restaurant.createdOn = new Date();

            saveData(activity, restaurant, true);
        }else{
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.update_location);

            builder.setMessage(R.string.would_you_like_to_update_the_restaurant_gps_coordinates);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveData(activity, restaurant, true);
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveData(activity, restaurant, false);
                }
            });

            android.app.AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void saveData(MainActivity activity, Restaurant restaurant, boolean saveCoordinates){
        if (saveCoordinates && (latitude != -1 || longitude != -1)) {
            Log.v(TAG, "Updating GPS coordinates");
            restaurant.latitude = latitude;
            restaurant.longitude = longitude;
        }

        restaurant.name = name.getText().toString();
        restaurant.phone = phone.getText().toString();

        restaurant.type = RestaurantType.find(type.getSelectedItem().toString());
        restaurant.cost = RestaurantCost.find(cost.getSelectedItem().toString());

        restaurant.notes = notes.getText().toString();
        restaurant.pictureURL = outputFileUri;

        restaurant.save();

        activity.refreshRestaurants();
        activity.onBackPressed();
    }

    @OnClick(R.id.btnRemove)
    public void onRemove(View view){
        final MainActivity activity = (MainActivity) getActivity();

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.remove);

        builder.setMessage(R.string.are_you_sure_you_want_to_remove_this_restaurant);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restaurant.delete();
                activity.refreshRestaurants();
                activity.onBackPressed();
            }
        });
        builder.setNegativeButton(android.R.string.no, null);

        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    @OnClick(R.id.btnTakePicture)
    public void takePicture(View view) {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.permission_required);

                builder.setMessage(R.string.write_permission_required_to_take_picture);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        requestWriteExternalStoragePermission();

                    }
                });

                android.app.AlertDialog dialog = builder.create();
                dialog.show();

            } else {

                requestWriteExternalStoragePermission();

            }
        }else{

            chooseIntent();

        }
    }

    private void chooseIntent(){
        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        pickIntent.setAction(Intent.ACTION_GET_CONTENT);

        File outputFile = getOutputMediaFile();
        outputFileUri = outputFile.getAbsolutePath();

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile));

        Intent chooserIntent = Intent.createChooser(pickIntent, getString(R.string.capture_or_select));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhotoIntent});

        startActivityForResult(chooserIntent, REQUEST_CODE_PICTURE);
    }

    private void requestWriteExternalStoragePermission() {

        requestPermissions(
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
        );

    }

    private void showBitmap(){
        picture.setImageBitmap(BitmapHelper.reduce(outputFileUri, 90));
        picture.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_PICTURE &&
                resultCode == Activity.RESULT_OK) {

            if (data != null) {
                try {
                    FileInputStream inStream = new FileInputStream(URIHelper.getPath(getContext(), data.getData()));
                    FileOutputStream outStream = new FileOutputStream(outputFileUri);
                    FileChannel inChannel = inStream.getChannel();
                    FileChannel outChannel = outStream.getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    inStream.close();
                    outStream.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            galleryAddPic();
            showBitmap();

        }else{
            outputFileUri = null;
            picture.setVisibility(View.GONE);
        }

    }

    @Nullable
    private File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getString(R.string.app_name));

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        File f = new File(outputFileUri);
        Uri contentUri = Uri.fromFile(f);

        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    chooseIntent();

                } else {

                    Toast.makeText(getActivity(), R.string.write_permission_required_to_take_picture,
                            Toast.LENGTH_LONG).show();

                }
                return;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_OUTPUTFILEURI_KEY, outputFileUri);
    }

    private void selectValue(Spinner spinner, Object value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
