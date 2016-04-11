package br.com.fiap.fiapfood.services;

import br.com.fiap.fiapfood.models.PlacesResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesService {

    @GET("json")
    Call<PlacesResult> GetPlacesNearBy(
                                  @Query("location") String location,
                                  @Query("radius") int radius,
                                  @Query("types") String types,
                                  @Query("sensor") boolean sensor,
                                  @Query("key") String key
                                );

}
