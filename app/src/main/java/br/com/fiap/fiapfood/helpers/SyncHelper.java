package br.com.fiap.fiapfood.helpers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import br.com.fiap.fiapfood.MainActivity;
import br.com.fiap.fiapfood.R;
import br.com.fiap.fiapfood.fragments.MapsFragment;
import br.com.fiap.fiapfood.models.ImportedRestaurant;
import br.com.fiap.fiapfood.models.Restaurant;
import br.com.fiap.fiapfood.models.RestaurantCost;
import br.com.fiap.fiapfood.models.RestaurantType;
import br.com.fiap.fiapfood.services.SyncService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SyncHelper {

    public static void execute(final Context context){
        final MainActivity activity = (MainActivity) context;
        ProgressDialog progress = ProgressDialog.show(context, "", context.getString(R.string.synchronizing_data), true);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.app_sync_data_api_endpoint))
                    .addConverterFactory(GsonConverterFactory.create()).build();

        SyncService service = retrofit.create(SyncService.class);
        Call<List<ImportedRestaurant>> call = service.ImportRestaurants();

        call.enqueue(new Callback<List<ImportedRestaurant>>() {
            @Override
            public void onResponse(Response<List<ImportedRestaurant>> response) {
                List<ImportedRestaurant> result = response.body();

                if(result != null){

                    int created = 0;
                    for(int i = 0; i < result.size(); i++){
                        ImportedRestaurant imported = result.get(i);
                        String[] split = imported.LOCALIZACAO.split(",");

                        Restaurant restaurant = Restaurant.get(activity.activeUser.getId(), imported.getName(), imported.getLatitude(), imported.getLongitude());
                        if(restaurant == null){
                            restaurant = new Restaurant();

                            restaurant.createdBy = activity.activeUser;
                            restaurant.createdOn = new Date();

                            restaurant.latitude = imported.getLatitude();
                            restaurant.longitude = imported.getLongitude();

                            restaurant.name = imported.getName();
                            restaurant.phone = imported.getPhone();

                            restaurant.type = RestaurantType.find(parseType(imported.getType()));
                            restaurant.cost = RestaurantCost.find(parseCost(imported.getCost()));

                            restaurant.notes = imported.getNotes();
                            restaurant.pictureURL = null;

                            restaurant.save();
                            created++;
                        }
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setMessage(context.getString(R.string.data_synchronization_is_successful) + " " + created + " " + context.getString(R.string.new_restaurants));
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        AuthHelper.SetSynchronized(context);
                        activity.refreshRestaurants();

                        activity.activeFragment = null;
                        activity.changeFragment(new MapsFragment());
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

            @Override
            public void onFailure(Throwable t) {

                Toast.makeText(context,
                        R.string.an_error_occurred_when_trying_to_sync_data__check_your_connection,
                            Toast.LENGTH_LONG).show();

            }
        });

        progress.dismiss();

    }

    public static String parseType(String type){
        switch (type){
            case "não sei":
                type = "Unknown";
                break;
            default:
                break;
        }

        return type;
    }

    public static String parseCost(double cost){
        String costStr = "";

        if(cost < 20){
            costStr = "$";
        }else if(cost > 20 && cost < 30){
            costStr = "$$";
        }else if(cost > 30 && cost < 50){
            costStr = "$$$";
        }else if(cost > 50 && cost < 85){
            costStr = "$$$$";
        }else if(cost > 85){
            costStr = "$$$$$";
        }

        return costStr;
    }
}
