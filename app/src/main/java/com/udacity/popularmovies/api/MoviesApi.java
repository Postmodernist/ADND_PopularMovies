package com.udacity.popularmovies.api;

import com.udacity.popularmovies.model.detail.MovieDetail;
import com.udacity.popularmovies.model.discover.MoviesPage;
import com.udacity.popularmovies.utils.ApiUtils;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface MoviesApi {
  @GET(ApiUtils.DISCOVER_PATH)
  Call<MoviesPage> getMovies(@QueryMap Map<String, String> options);

  @GET(ApiUtils.DETAIL_PATH + "{id}")
  Call<MovieDetail> getMovieDetail(@Path("id") int movieId, @QueryMap Map<String, String> options);
}
