package br.com.fiap.fiapfood.services;

import java.util.List;

import br.com.fiap.fiapfood.models.ImportedRestaurant;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SyncService {

    @GET("restaurantes.json")
    Call<List<ImportedRestaurant>> ImportRestaurants();

}
