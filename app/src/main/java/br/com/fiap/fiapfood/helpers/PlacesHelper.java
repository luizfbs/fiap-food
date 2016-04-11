package br.com.fiap.fiapfood.helpers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.List;

import br.com.fiap.fiapfood.MainActivity;
import br.com.fiap.fiapfood.R;
import br.com.fiap.fiapfood.fragments.FormFragment;
import br.com.fiap.fiapfood.models.Place;
import br.com.fiap.fiapfood.models.PlacesResult;
import br.com.fiap.fiapfood.services.PlacesService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PlacesHelper {

    public static void execute(final Context context, final FormFragment form){
        final MainActivity activity = (MainActivity) context;

        if(activity.location == null)
            return;

        final ProgressDialog progress = ProgressDialog.show(context, "", context.getString(R.string.finding_restaurants_nearby), true);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.google_places_nearby_api_endpoint))
                .addConverterFactory(GsonConverterFactory.create()).build();


        PlacesService service = retrofit.create(PlacesService.class);
        Call<PlacesResult> call = service.GetPlacesNearBy(activity.location.getLatitude() + "," + activity.location.getLongitude(), 5000, "restaurant", true, context.getString(R.string.google_places_key));

        call.enqueue(new Callback<PlacesResult>() {
            @Override
            public void onResponse(Response<PlacesResult> response) {

                PlacesResult result = response.body();
                if (result != null && result.status.equals("OK")) {

                    final List<Place> list = result.results;
                    int total = list.size();
                    String[] places = new String[total];

                    for (int i = 0; i < total; i++) {
                        places[i] = list.get(i).name;
                    }

                    AlertDialog.Builder b = new AlertDialog.Builder(context);
                    b.setTitle(R.string.are_you_going_to_add_one_of_this_places);
                    b.setItems(places, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            form.fillData(list.get(which));
                        }

                    });

                    Dialog d = b.create();
                    d.show();
                    progress.dismiss();

                    int width = (int)(context.getResources().getDisplayMetrics().widthPixels*0.90);
                    int height = (int)(context.getResources().getDisplayMetrics().heightPixels*0.60);
                    d.getWindow().setLayout(width, height);
                }
            }

            @Override
            public void onFailure(Throwable t) {

                // failed =/
                progress.dismiss();

            }
        });

    }
}
