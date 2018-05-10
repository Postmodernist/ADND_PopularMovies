package com.udacity.popularmovies.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.api.ApiUtils;
import com.udacity.popularmovies.database.MovieContract;
import com.udacity.popularmovies.model.detail.MovieDetail;
import com.udacity.popularmovies.model.detail.MovieReview;
import com.udacity.popularmovies.model.detail.MovieVideo;
import com.udacity.popularmovies.model.discover.MovieItem;
import com.udacity.popularmovies.repositories.MovieRepository;

import java.util.ArrayList;
import java.util.List;

public class MovieViewModel extends ViewModel {

  private static final String TAG = "TAG_" + MovieViewModel.class.getSimpleName();
  private static final String LIST_MODE_KEY = "LIST_MODE";
  private static final int STARTING_PAGE = 1;
  private static final int PAGE_SIZE = 20;
  private static final List<MovieVideo> EMPTY_VIDEOS_LIST = new ArrayList<>(0);
  private static final List<MovieReview> EMPTY_REVIEWS_LIST = new ArrayList<>(0);

  private MovieRepository movieRepo;
  private SharedPreferences sharedPrefs;

  private LiveData<List<MovieItem>> liveMovieList;
  private MediatorLiveData<Boolean> liveLoadingStatus = new MediatorLiveData<>();
  private MediatorLiveData<MovieDetail> liveMovieDetail = new MediatorLiveData<>();
  private LiveData<List<MovieVideo>> liveMovieTrailers;
  private MediatorLiveData<List<MovieReview>> liveMovieReviews = new MediatorLiveData<>();

  private boolean isInitialized = false;
  private ListMode listMode;
  private List<MovieItem> movieList = new ArrayList<>(100);
  private List<MovieVideo> trailerList = EMPTY_VIDEOS_LIST;
  private int page = STARTING_PAGE;  // for discovery list paging
  private int listSize;  // for starred list updating
  private int lastStarredMovieId = -1;  // for starred list paging
  private int detailMovieId = -1;
  private int lastDetailMovieId = -1;  // for avoiding unnecessary detail reload

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

    listMode = ListMode.fromId(sharedPrefs.getInt(LIST_MODE_KEY, ListMode.MOST_POPULAR.id));
    liveLoadingStatus.addSource(movieRepo.getLoadingStatus(), liveLoadingStatus::setValue);
    liveMovieDetail.addSource(movieRepo.getMovieDetail(), liveMovieDetail::setValue);
    liveMovieReviews.addSource(movieRepo.getMovieReviews(), liveMovieReviews::setValue);

    liveMovieList = Transformations.map(movieRepo.getMoviesPage(), movies -> {
      if (movies != null) {
        movieList.addAll(movies);
        if (listMode == ListMode.MOST_POPULAR || listMode == ListMode.TOP_RATED) {
          ++page;
        } else if (listMode == ListMode.STARRED) {
          if (movies.size() > 0) {
            lastStarredMovieId = movies.get(movies.size() - 1).getId();
          }
        }
      }
      return movieList;
    });

    liveMovieTrailers = Transformations.map(movieRepo.getMovieVideos(), videos -> {
      if (videos != null) {
        trailerList = new ArrayList<>();
        // Filter videos
        for (MovieVideo video : videos) {
          String type = video.getType();
          if ("YouTube".equals(video.getSite()) && ("Teaser".equals(type) || "Trailer".equals(type))) {
            trailerList.add(video);
          }
        }
      } else {
        trailerList = EMPTY_VIDEOS_LIST;
      }
      return trailerList;
    });

    loadMore();
  }

  public void setDetailMovieId(int detailMovieId) {
    lastDetailMovieId = this.detailMovieId;
    this.detailMovieId = detailMovieId;
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

  public LiveData<MovieDetail> getMovieDetail(int position) {
    if (lastDetailMovieId != detailMovieId) {
      liveMovieDetail.setValue(new MovieDetail(movieList.get(position)));
      movieRepo.loadMovieDetail(detailMovieId);
    }
    return liveMovieDetail;
  }

  public LiveData<List<MovieVideo>> getMovieTrailers() {
    if (lastDetailMovieId != detailMovieId) {
      trailerList.clear();
      movieRepo.loadMovieVideos(detailMovieId);
    }
    return liveMovieTrailers;
  }

  public LiveData<List<MovieReview>> getMovieReviews() {
    if (lastDetailMovieId != detailMovieId) {
      liveMovieReviews.setValue(EMPTY_REVIEWS_LIST);
      movieRepo.loadMovieReviews(detailMovieId);
    }
    return liveMovieReviews;
  }

  public void refresh() {
    Log.d(TAG, "Reloading movies");
    movieList.clear();
    page = STARTING_PAGE;
    lastStarredMovieId = -1;
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
        movieRepo.loadStarred(lastStarredMovieId, PAGE_SIZE);
        break;
    }
  }

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

      enableStarCallback.run(R.drawable.ic_star_disabled, R.string.star_button_text, v -> {
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

      enableStarCallback.run(R.drawable.ic_star_enabled, R.string.unstar_button_text, v -> {
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
    void run(@DrawableRes int icon, @StringRes int text, View.OnClickListener onClickListener);
  }
}
