package com.udacity.popularmovies.repositories;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
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
  private static final String SORT_ORDER_KEY = "SORT_ORDER";
  private static final int STARTING_PAGE = 1;

  private static MoviesRepository INSTANCE;

  private DiscoverApi discoverApi;
  private Executor executor;
  private SharedPreferences sharedPrefs;
  private String sortBy;
  private int page;

  // List of movies to be displayed in discovery activity
  private MutableLiveData<List<Result>> moviesList = new MutableLiveData<>();

  // Loading status indicator
  private MutableLiveData<Boolean> loading = new MutableLiveData<>();


  public MoviesRepository(DiscoverApi discoverApi, Executor executor, SharedPreferences sharedPrefs) {
    this.discoverApi = discoverApi;
    this.executor = executor;
    this.sharedPrefs = sharedPrefs;
    sortBy = sharedPrefs.getString(SORT_ORDER_KEY, HttpUtils.SORT_BY_POPULARITY);
    page = STARTING_PAGE;
    loading.setValue(false);
    INSTANCE = this;
  }

  public static MoviesRepository getInstance() {
    return INSTANCE;
  }

  /**
   * Load everything from scratch
   */
  public void refresh() {
    moviesList.setValue(null);
    page = STARTING_PAGE;
    loadMore();
  }

  /**
   * Load next page of movies if possible
   */
  public void loadMore() {
    fetchMoviesPage(discoverApi.getMovies(HttpUtils.discoveryQueryOptions(sortBy, page)));
  }

  /**
   * Return movies LiveData
   */
  public LiveData<List<Result>> getMoviesList() {
    List<Result> movies = moviesList.getValue();
    if (movies == null || movies.size() == 0) {
      // Auto load if list is empty
      loadMore();
    }
    return moviesList;
  }

  /**
   * Return loading status LiveData
   */
  public LiveData<Boolean> getLoading() {
    return loading;
  }

  /**
   * Return sorting order
   */
  public String getSortBy() {
    return sortBy;
  }

  /**
   * Set sorting order and reload data if necessary
   */
  public void setSortBy(String sortBy) {
    if (TextUtils.equals(this.sortBy, sortBy)) {
      return;
    }
    this.sortBy = sortBy;
    sharedPrefs.edit().putString(SORT_ORDER_KEY, sortBy).apply();
    refresh();
  }

  /**
   * Load movies from web service
   */
  private void fetchMoviesPage(Call<MoviesPage> discoverApiCall) {
    loading.setValue(true);
    executor.execute(() -> discoverApiCall.enqueue(new Callback<MoviesPage>() {

      @Override
      public void onResponse(@NonNull Call<MoviesPage> call,
                             @NonNull Response<MoviesPage> response) {
        MoviesPage moviesPage = response.body();
        List<Result> movies = moviesPage != null ? moviesPage.getResults() : null;
        if (moviesList.getValue() == null) {
          moviesList.setValue(movies);
        } else if (movies != null) {
          moviesList.getValue().addAll(movies);
        }
        Log.i(TAG, "Finished loading page " + page);
        ++page;
        loading.setValue(false);
      }

      @Override
      public void onFailure(@NonNull Call<MoviesPage> call,
                            @NonNull Throwable t) {
        Log.w(TAG, "Failed to load page " + page);
        loading.setValue(false);
      }
    }));
  }
}
