package com.udacity.popularmovies.repositories;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.udacity.popularmovies.api.DiscoverApi;
import com.udacity.popularmovies.api.HttpUtils;
import com.udacity.popularmovies.model.discover.MoviesPage;
import com.udacity.popularmovies.model.discover.Result;

import java.util.List;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoviesRepository {

  private static final String TAG = "TAG_" + MoviesRepository.class.getSimpleName();
  private static MoviesRepository INSTANCE;

  private DiscoverApi discoverApi;
  private Executor executor;
  private MutableLiveData<List<Result>> liveMoviesPage = new MutableLiveData<>();
  private MutableLiveData<Boolean> liveLoadingStatus = new MutableLiveData<>();


  public MoviesRepository(DiscoverApi discoverApi, Executor executor) {
    this.discoverApi = discoverApi;
    this.executor = executor;
    liveLoadingStatus.setValue(false);
    INSTANCE = this;
  }

  public static MoviesRepository getInstance() {
    return INSTANCE;
  }

  // -----------------------------------------------------------------------------------------------
  // Movies list

  /**
   * Load a page of movies from web service
   */
  public void loadMoviesPage(String sortBy, int page) {
    Call<MoviesPage> discoverApiCall = discoverApi.getMovies(HttpUtils.discoveryQueryOptions(sortBy, page));
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

  /**
   * Return movies page
   */
  public LiveData<List<Result>> getMoviesPage() {
    return liveMoviesPage;
  }

  /**
   * Return liveLoadingStatus status LiveData
   */
  public LiveData<Boolean> getLoadingStatus() {
    return liveLoadingStatus;
  }

  // -----------------------------------------------------------------------------------------------
  // Movies details

}
