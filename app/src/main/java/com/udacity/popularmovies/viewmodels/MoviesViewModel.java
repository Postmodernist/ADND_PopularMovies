package com.udacity.popularmovies.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.udacity.popularmovies.model.detail.MovieDetail;
import com.udacity.popularmovies.model.discover.MovieItem;
import com.udacity.popularmovies.repositories.MoviesRepository;
import com.udacity.popularmovies.utils.ApiUtils;

import java.util.ArrayList;
import java.util.List;

public class MoviesViewModel extends ViewModel {

  private static final String TAG = "TAG_" + MoviesViewModel.class.getSimpleName();
  private static final String SORT_ORDER_KEY = "SORT_ORDER";
  private static final int STARTING_PAGE = 1;

  private MoviesRepository moviesRepo;
  private SharedPreferences sharedPrefs;

  private boolean isInitialized = false;

  // Movies list
  private LiveData<List<MovieItem>> liveMoviesList;
  private MediatorLiveData<Boolean> liveLoadingStatus = new MediatorLiveData<>();
  private List<MovieItem> moviesList = new ArrayList<>();
  private int page = STARTING_PAGE;
  private String sortBy;

  // Movie details
  private MediatorLiveData<MovieDetail> liveMovieDetail = new MediatorLiveData<>();
  private int lastMovieId;

  MoviesViewModel(MoviesRepository moviesRepo, SharedPreferences sharedPrefs) {
    this.moviesRepo = moviesRepo;
    this.sharedPrefs = sharedPrefs;
  }

  public void init() {
    if (isInitialized) {
      return;
    }
    isInitialized = true;
    Log.d(TAG, "Initializing ViewModel");
    liveMoviesList = Transformations.map(moviesRepo.getMoviesPage(), movies -> {
      if (movies != null) {
        moviesList.addAll(movies);
        ++page;
      }
      return moviesList;
    });
    liveLoadingStatus.addSource(moviesRepo.getLoadingStatus(), liveLoadingStatus::setValue);
    liveMovieDetail.addSource(moviesRepo.getMovieDetail(), liveMovieDetail::setValue);
    sortBy = sharedPrefs.getString(SORT_ORDER_KEY, ApiUtils.SORT_BY_POPULARITY);
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

  public LiveData<List<MovieItem>> getMoviesList() {
    return liveMoviesList;
  }

  public LiveData<Boolean> getLoadingStatus() {
    return liveLoadingStatus;
  }

  public String getSortBy() {
    return sortBy;
  }

  public boolean setSortBy(String sortBy) {
    if (TextUtils.equals(this.sortBy, sortBy)) {
      return false;
    }
    this.sortBy = sortBy;
    sharedPrefs.edit().putString(SORT_ORDER_KEY, sortBy).apply();
    refresh();
    return true;
  }

  public LiveData<MovieDetail> getMovieDetail(int movieId, int position) {
    if (lastMovieId != movieId) {
      lastMovieId = movieId;
      liveMovieDetail.setValue(new MovieDetail(moviesList.get(position)));
      moviesRepo.loadMovieDetail(movieId);
    }
    return liveMovieDetail;
  }
}
