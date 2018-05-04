package com.udacity.popularmovies.repositories;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.udacity.popularmovies.api.MoviesApi;
import com.udacity.popularmovies.utils.ApiUtils;
import com.udacity.popularmovies.model.detail.MovieDetail;
import com.udacity.popularmovies.model.discover.MoviesPage;
import com.udacity.popularmovies.model.discover.MovieItem;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class MoviesRepository {

  private static final String TAG = "TAG_" + MoviesRepository.class.getSimpleName();

  private MoviesApi moviesApi;
  private Executor executor;
  private MutableLiveData<List<MovieItem>> liveMoviesPage = new MutableLiveData<>();
  private MutableLiveData<Boolean> liveLoadingStatus = new MutableLiveData<>();
  private MutableLiveData<MovieDetail> liveMovieDetail = new MutableLiveData<>();

  public MoviesRepository(MoviesApi moviesApi, Executor executor) {
    this.moviesApi = moviesApi;
    this.executor = executor;
    liveLoadingStatus.setValue(false);
  }

  // -----------------------------------------------------------------------------------------------
  // Movies list

  public void loadMoviesPage(String sortBy, int page) {
    Call<MoviesPage> discoverApiCall = moviesApi.getMovies(ApiUtils.discoveryQueryOptions(sortBy, page));
    liveLoadingStatus.setValue(true);
    executor.execute(() -> discoverApiCall.enqueue(new Callback<MoviesPage>() {

      @Override
      public void onResponse(@NonNull Call<MoviesPage> call,
                             @NonNull Response<MoviesPage> response) {
        if (response.isSuccessful()) {
          MoviesPage moviesPage = response.body();
          liveMoviesPage.setValue(moviesPage != null ? moviesPage.getResults() : null);
          Log.d(TAG, "Finished loading page " + page);
        } else {
          Log.d(TAG, "Failed to load page " + page + ". Error code: " + response.code());
        }
        liveLoadingStatus.postValue(false);
      }

      @Override
      public void onFailure(@NonNull Call<MoviesPage> call,
                            @NonNull Throwable t) {
        Log.d(TAG, "Failed to load page " + page);
        liveLoadingStatus.postValue(false);
      }
    }));
  }

  public LiveData<List<MovieItem>> getMoviesPage() {
    return liveMoviesPage;
  }

  public LiveData<Boolean> getLoadingStatus() {
    return liveLoadingStatus;
  }

  // -----------------------------------------------------------------------------------------------
  // Movie detail

  public void loadMovieDetail(int movieId) {
    Call<MovieDetail> movieApiCall = moviesApi.getMovieDetail(movieId, ApiUtils.detailQueryOptions());
    executor.execute(() -> movieApiCall.enqueue(new Callback<MovieDetail>() {

      @Override
      public void onResponse(@NonNull Call<MovieDetail> call,
                             @NonNull Response<MovieDetail> response) {
        if (response.isSuccessful()) {
          liveMovieDetail.setValue(response.body());
          Log.d(TAG, "Finished loading movie detail, id " + movieId);
        } else {
          Log.d(TAG, "Failed to load movie detail, id " + movieId + ". Error code: " + response.code());
        }
      }

      @Override
      public void onFailure(@NonNull Call<MovieDetail> call,
                            @NonNull Throwable t) {
        Log.d(TAG, "Failed to load movie detail, id " + movieId);
      }
    }));
  }

  public LiveData<MovieDetail> getMovieDetail() {
    return liveMovieDetail;
  }
}
