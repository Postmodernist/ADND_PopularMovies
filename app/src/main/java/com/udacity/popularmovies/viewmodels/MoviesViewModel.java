package com.udacity.popularmovies.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.udacity.popularmovies.api.HttpUtils;
import com.udacity.popularmovies.model.discover.Result;
import com.udacity.popularmovies.repositories.MoviesRepository;

import java.util.ArrayList;
import java.util.List;

public class MoviesViewModel extends ViewModel {

  private static final String TAG = "TAG_" + MoviesViewModel.class.getSimpleName();
  private static final String SORT_ORDER_KEY = "SORT_ORDER";
  private static final int STARTING_PAGE = 1;

  private MoviesRepository moviesRepo;
  private SharedPreferences sharedPrefs;
  private LiveData<List<Result>> liveMoviesList;
  private LiveData<Boolean> liveLoadingStatus;
  private List<Result> moviesList = new ArrayList<>();
  private int page;
  private String sortBy;

  MoviesViewModel(MoviesRepository moviesRepo, SharedPreferences sharedPrefs) {
    this.moviesRepo = moviesRepo;
    this.sharedPrefs = sharedPrefs;
  }

  public void init() {
    if (liveMoviesList != null) {
      return;
    }
    Log.d(TAG, "Initializing ViewModel");
    liveMoviesList = Transformations.map(moviesRepo.getMoviesPage(), movies -> {
      if (movies != null) {
        moviesList.addAll(movies);
        ++page;
      }
      return moviesList;
    });
    liveLoadingStatus = moviesRepo.getLoadingStatus();
    sortBy = sharedPrefs.getString(SORT_ORDER_KEY, HttpUtils.SORT_BY_POPULARITY);
    page = STARTING_PAGE;
    loadMore();
  }

  public void refresh() {
    Log.d(TAG, "Reloading movies");
    moviesList.clear();
    page = STARTING_PAGE;
    loadMore();
  }

  public void loadMore() {
    moviesRepo.loadMoviesPage(sortBy, page);
  }

  public LiveData<List<Result>> getMoviesList() {
    return liveMoviesList;
  }

  public LiveData<Boolean> getLoadingStatus() {
    return liveLoadingStatus;
  }

  public String getSortBy() {
    return sortBy;
  }

  public void setSortBy(String sortBy) {
    if (TextUtils.equals(this.sortBy, sortBy)) {
      return;
    }
    this.sortBy = sortBy;
    sharedPrefs.edit().putString(SORT_ORDER_KEY, sortBy).apply();
    refresh();
  }
}
