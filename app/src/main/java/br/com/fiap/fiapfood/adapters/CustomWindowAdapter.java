package br.com.fiap.fiapfood.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

import br.com.fiap.fiapfood.R;
import br.com.fiap.fiapfood.helpers.BitmapHelper;
import br.com.fiap.fiapfood.models.Place;
import br.com.fiap.fiapfood.models.Restaurant;

public class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private HashMap<String, Restaurant> markers;

    public CustomWindowAdapter(Context context, HashMap<String, Restaurant> markers){
        this.context = context;
        this.markers = markers;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View v = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.fragment_list_item, null);
        v.setLayoutParams(new RelativeLayout.LayoutParams(500, RelativeLayout.LayoutParams.WRAP_CONTENT));

        final Restaurant restaurant = markers.get(marker.getTitle());

        ImageView picture = (ImageView) v.findViewById(R.id.picture);
        if(restaurant.pictureURL != null) {
            picture.setImageBitmap(BitmapHelper.reduce(restaurant.pictureURL, 120));
        }else{
            picture.setImageResource(R.mipmap.ic_pin);
        }

        TextView name = (TextView) v.findViewById(R.id.name);
        name.setText(restaurant.name);

        TextView phone = (TextView) v.findViewById(R.id.phone);
        if(restaurant.phone.length() > 0){
            phone.setVisibility(View.VISIBLE);
            phone.setText(restaurant.phone);
        }else{
            phone.setVisibility(View.GONE);
        }

        TextView type = (TextView) v.findViewById(R.id.type);
        if(restaurant.type != null){
            type.setVisibility(View.VISIBLE);
            type.setText(restaurant.type.name);
        }else{
            type.setVisibility(View.GONE);
        }

        TextView cost = (TextView) v.findViewById(R.id.cost);
        if(restaurant.cost != null){
            cost.setVisibility(View.VISIBLE);
            cost.setText(restaurant.cost.name);
        }else{
            cost.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }
}
