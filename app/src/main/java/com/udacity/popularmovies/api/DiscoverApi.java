package com.udacity.popularmovies.api;

import com.udacity.popularmovies.model.discover.MoviesPage;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface DiscoverApi {
  @GET(HttpUtils.DISCOVER_PATH)
  Call<MoviesPage> getMovies(@QueryMap Map<String, String> options);
}
