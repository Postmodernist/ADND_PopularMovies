package com.alexbaryzhikov.popularmovies.api;

import com.alexbaryzhikov.popularmovies.model.detail.MovieDetail;
import com.alexbaryzhikov.popularmovies.model.detail.MovieReviews;
import com.alexbaryzhikov.popularmovies.model.detail.MovieVideos;
import com.alexbaryzhikov.popularmovies.model.discover.MoviePage;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface MovieApi {
  @GET(ApiUtils.DISCOVER_PATH)
  Call<MoviePage> getMovies(@QueryMap Map<String, String> options);

  @GET(ApiUtils.DETAIL_PATH + "{id}")
  Call<MovieDetail> getMovieDetail(@Path("id") int movieId, @QueryMap Map<String, String> options);

  @GET(ApiUtils.DETAIL_PATH + "{id}/videos")
  Call<MovieVideos> getMovieVideos(@Path("id") int movieId, @QueryMap Map<String, String> options);

  @GET(ApiUtils.DETAIL_PATH + "{id}/reviews")
  Call<MovieReviews> getMovieReviews(@Path("id") int movieId, @QueryMap Map<String, String> options);
}
