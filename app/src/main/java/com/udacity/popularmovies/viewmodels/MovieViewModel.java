package com.udacity.popularmovies.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.database.MovieContract;
import com.udacity.popularmovies.model.detail.MovieDetail;
import com.udacity.popularmovies.model.discover.MovieItem;
import com.udacity.popularmovies.repositories.MovieRepository;
import com.udacity.popularmovies.utils.ApiUtils;

import java.util.ArrayList;
import java.util.List;

public class MovieViewModel extends ViewModel {

  private static final String TAG = "TAG_" + MovieViewModel.class.getSimpleName();
  private static final String SORT_ORDER_KEY = "SORT_ORDER";
  private static final int STARTING_PAGE = 1;

  private MovieRepository movieRepo;
  private SharedPreferences sharedPrefs;
  private boolean isInitialized = false;

  // Movie list
  private LiveData<List<MovieItem>> liveMovieList;
  private MediatorLiveData<Boolean> liveLoadingStatus = new MediatorLiveData<>();
  private List<MovieItem> movieList = new ArrayList<>();
  private int page = STARTING_PAGE;
  private String sortBy;

  // Movie detail
  private MediatorLiveData<MovieDetail> liveMovieDetail = new MediatorLiveData<>();
  private int lastMovieId;
  private volatile boolean starLock = false;

  // -----------------------------------------------------------------------------------------------
  // Constructor and initializer

  MovieViewModel(MovieRepository movieRepo, SharedPreferences sharedPrefs) {
    this.movieRepo = movieRepo;
    this.sharedPrefs = sharedPrefs;
  }

  public void init() {
    if (isInitialized) {
      return;
    }
    isInitialized = true;
    Log.d(TAG, "Initializing ViewModel");
    liveMovieList = Transformations.map(movieRepo.getMoviesPage(), movies -> {
      if (movies != null) {
        movieList.addAll(movies);
        ++page;
      }
      return movieList;
    });
    liveLoadingStatus.addSource(movieRepo.getLoadingStatus(), liveLoadingStatus::setValue);
    liveMovieDetail.addSource(movieRepo.getMovieDetail(), liveMovieDetail::setValue);
    sortBy = sharedPrefs.getString(SORT_ORDER_KEY, ApiUtils.SORT_BY_POPULARITY);
    loadMore();
  }

  // -----------------------------------------------------------------------------------------------
  // Movies list

  public void refresh() {
    Log.d(TAG, "Reloading movies");
    movieList.clear();
    page = STARTING_PAGE;
    loadMore();
  }

  public void loadMore() {
    movieRepo.loadMoviesPage(sortBy, page);
  }

  public LiveData<List<MovieItem>> getMovieList() {
    return liveMovieList;
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

  // -----------------------------------------------------------------------------------------------
  // Movie detail

  public LiveData<MovieDetail> getMovieDetail(int movieId, int position) {
    if (lastMovieId != movieId) {
      lastMovieId = movieId;
      liveMovieDetail.setValue(new MovieDetail(movieList.get(position)));
      movieRepo.loadMovieDetail(movieId);
    }
    return liveMovieDetail;
  }

  /** Check local db if this movie is starred */
  public void initStarButton(MovieDetail movie, StarButtonCallback callback) {
    long id = -1;
    try (Cursor cursor = movieRepo.getStarredMovie(movie)) {
      if (cursor.getCount() == 1) {
        cursor.moveToNext();
        id = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry._ID));
      }
    }
    setupStarButton(movie, callback, id);
  }

  /** Switch between starred/unstarred */
  private void setupStarButton(MovieDetail movie, StarButtonCallback callback, final long id) {
    if (id == -1) {

      // *** Not starred ***

      callback.run(R.string.star_button_text, v -> {
        // Add to favorites
        final long newId = movieRepo.starMovie(movie);
        // Make star button great again
        setupStarButton(movie, callback, newId);
      });

    } else {

      // *** Starred ***

      callback.run(R.string.unstar_button_text, v -> {
        // Remove from favorites
        movieRepo.unstarMovie(id);
        // Make star button great again
        setupStarButton(movie, callback, -1);
      });
    }
  }

  /** Callback interface to modify UI when the star state changes */
  public interface StarButtonCallback {
    void run(@StringRes int text, View.OnClickListener onClickListener);
  }
}
