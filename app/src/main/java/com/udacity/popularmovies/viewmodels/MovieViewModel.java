package com.udacity.popularmovies.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;
import android.util.Log;
import android.util.SparseArray;
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
  private static final String LIST_MODE_KEY = "LIST_MODE";
  private static final int STARTING_PAGE = 1;
  private static final int PAGE_SIZE = 20;

  private MovieRepository movieRepo;
  private SharedPreferences sharedPrefs;

  private boolean isInitialized = false;

  // Movie list
  private LiveData<List<MovieItem>> liveMovieList;
  private MediatorLiveData<Boolean> liveLoadingStatus = new MediatorLiveData<>();
  private List<MovieItem> movieList = new ArrayList<>();
  private int listSize;  // for starred list updating
  private ListMode listMode;
  private int page = STARTING_PAGE;
  private int lastLoadedMovieId = -1;  // for starred list paging

  // Movie detail
  private MediatorLiveData<MovieDetail> liveMovieDetail = new MediatorLiveData<>();
  private int lastViewedMovieId;

  MovieViewModel(MovieRepository movieRepo, SharedPreferences sharedPrefs) {
    this.movieRepo = movieRepo;
    this.sharedPrefs = sharedPrefs;
  }

  // -----------------------------------------------------------------------------------------------
  // Constructor and initializer

  public void init() {
    if (isInitialized) {
      return;
    }
    isInitialized = true;
    Log.d(TAG, "Initializing ViewModel");
    liveMovieList = Transformations.map(movieRepo.getMoviesPage(), movies -> {
      if (movies != null) {
        movieList.addAll(movies);
        switch (listMode) {
          case MOST_POPULAR:
            // Fall through
          case TOP_RATED:
            ++page;
            break;
          case STARRED:
            if (movies.size() > 0) {
              lastLoadedMovieId = movies.get(movies.size() - 1).getId();
            }
            break;
        }
      }
      return movieList;
    });
    liveLoadingStatus.addSource(movieRepo.getLoadingStatus(), liveLoadingStatus::setValue);
    liveMovieDetail.addSource(movieRepo.getMovieDetail(), liveMovieDetail::setValue);
    listMode = ListMode.fromId(sharedPrefs.getInt(LIST_MODE_KEY, ListMode.MOST_POPULAR.id));
    loadMore();
  }

  // -----------------------------------------------------------------------------------------------
  // Movies list

  public void refresh() {
    Log.d(TAG, "Reloading movies");
    movieList.clear();
    page = STARTING_PAGE;
    lastLoadedMovieId = -1;
    loadMore();
  }

  private void refreshStarred() {
    Log.d(TAG, "Reloading starred movies");
    movieList.clear();
    movieRepo.loadStarred(-1, listSize);
  }

  public void loadMore() {
    switch (listMode) {
      case MOST_POPULAR:
        movieRepo.loadMoviesPage(ApiUtils.SORT_BY_POPULARITY, page);
        break;
      case TOP_RATED:
        movieRepo.loadMoviesPage(ApiUtils.SORT_BY_RATING, page);
        break;
      case STARRED:
        movieRepo.loadStarred(lastLoadedMovieId, PAGE_SIZE);
        break;
    }
  }

  public LiveData<List<MovieItem>> getMovieList() {
    return liveMovieList;
  }

  public LiveData<Boolean> getLoadingStatus() {
    return liveLoadingStatus;
  }

  public ListMode getListMode() {
    return listMode;
  }

  public boolean setListMode(ListMode listMode) {
    if (this.listMode == listMode) {
      return false;
    }
    this.listMode = listMode;
    sharedPrefs.edit().putInt(LIST_MODE_KEY, listMode.id).apply();
    refresh();
    return true;
  }

  public LiveData<MovieDetail> getMovieDetail(int movieId, int position) {
    if (lastViewedMovieId != movieId) {
      lastViewedMovieId = movieId;
      liveMovieDetail.setValue(new MovieDetail(movieList.get(position)));
      movieRepo.loadMovieDetail(movieId);
    }
    return liveMovieDetail;
  }

  // -----------------------------------------------------------------------------------------------
  // Movie detail

  /** Check local db if this movie is starred */
  public void initStarButton(MovieDetail movie, Runnable disableStarCallback,
                             EnableStarCallback enableStarCallback) {
    Log.d(TAG, "Initializing Star button...");
    movieRepo.getStarredMovie(movie, cursor -> {
      long id = -1;
      if (cursor.getCount() == 1) {
        cursor.moveToNext();
        id = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry._ID));
      }
      Log.d(TAG, id != -1 ? "Movie is starred, database_id " + id : "Movie is NOT starred");
      setupStarButton(movie, disableStarCallback, enableStarCallback, id);
    });
    // Save movie list size
    listSize = movieList.size();
  }

  /** Switch between starred/unstarred */
  private void setupStarButton(MovieDetail movie, Runnable disableStarCallback,
                               EnableStarCallback enableStarCallback, final long id) {
    if (id == -1) {

      // *** Not starred ***

      enableStarCallback.run(R.string.star_button_text, v -> {
        // Disable star button
        disableStarCallback.run();
        // Add to favorites
        movieRepo.starMovie(movie, newId -> {
          // Make star button great again
          setupStarButton(movie, disableStarCallback, enableStarCallback, newId);
          // Refresh starred movies list
          if (listMode == ListMode.STARRED) {
            refreshStarred();
          }
        });
      });
    } else {

      // *** Starred ***

      enableStarCallback.run(R.string.unstar_button_text, v -> {
        // Disable star button
        disableStarCallback.run();
        // Remove from favorites
        movieRepo.unstarMovie(id, w -> {
          // Make star button great again
          setupStarButton(movie, disableStarCallback, enableStarCallback, -1);
          // Refresh starred movies list
          if (listMode == ListMode.STARRED) {
            refreshStarred();
          }
        });
      });
    }
  }

  public enum ListMode {
    MOST_POPULAR(0), TOP_RATED(1), STARRED(2);

    private static final SparseArray<ListMode> valuesById;

    static {
      valuesById = new SparseArray<>(values().length);
      for (ListMode value : values()) {
        valuesById.put(value.id, value);
      }
    }

    public final int id;

    ListMode(int id) {
      this.id = id;
    }

    public static ListMode fromId(Integer id) {
      return valuesById.get(id);
    }
  }

  /** Callback to modify UI when the star state changes */
  public interface EnableStarCallback {
    void run(@StringRes int text, View.OnClickListener onClickListener);
  }
}
