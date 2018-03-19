package com.udacity.popularmovies;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.udacity.popularmovies.utils.HttpUtils;

import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
    implements MoviesAdapter.MoviesOnClickHandler, LoaderManager.LoaderCallbacks<Bundle> {

  private static final String TAG = "TAG_" + MainActivity.class.getSimpleName();
  private static final String SORT_ORDER_KEY = "SORT_ORDER";
  private static final int STARTING_PAGE = 1;

  @BindView(R.id.rv_movies)
  RecyclerView moviesView;
  @BindView(R.id.tv_error)
  TextView errorView;
  @BindView(R.id.pb_loading)
  ProgressBar loadingIndicator;

  private MoviesAdapter adapter;
  private EndlessRecyclerViewScrollListener scrollListener;
  private String sortOrder;
  private SharedPreferences sharedPrefs;
  private int gridColumnsNumber;
  private int gridRowHeight;
  private int page = STARTING_PAGE;
  private int totalPages = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    // Load sorting preferences
    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    sortOrder = sharedPrefs.getString(SORT_ORDER_KEY, HttpUtils.SORT_BY_POPULARITY);
    if (sortOrder.equals(HttpUtils.SORT_BY_POPULARITY)) {
      setTitle(getString(R.string.most_popular_title));
    } else {
      setTitle(getString(R.string.top_rated_title));
    }

    // Setup RecyclerView
    initGridDimens();
    GridLayoutManager layoutManager = new GridLayoutManager(this, gridColumnsNumber);
    moviesView.setLayoutManager(layoutManager);
    adapter = new MoviesAdapter(this, gridRowHeight);
    moviesView.setAdapter(adapter);
    scrollListener = new EndlessRecyclerViewScrollListener(layoutManager, STARTING_PAGE) {
      @Override
      public void onLoadMore(int page) {
        if (!isOnline() || (totalPages > 0 && page > totalPages)) {
          return;
        }
        MainActivity.this.page = page;
        getLoaderManager().restartLoader(0, null, MainActivity.this);
      }
    };
    moviesView.addOnScrollListener(scrollListener);

    // Download movies data
    if (isOnline()) {
      showMovies();
      getLoaderManager().initLoader(0, null, this);
    } else {
      showError();
    }
  }

  @Override
  public void onClick(Movie movie) {
    Log.i(TAG, "Item clicked: " + movie.getTitle());
    Intent intent = new Intent(this, DetailActivity.class);
    intent.putExtra(Movie.MOVIE_KEY, movie);
    startActivity(intent);
  }

  // -----------------------------------------------------------------------------------------------
  // Loader

  @Override
  public Loader<Bundle> onCreateLoader(int id, Bundle args) {
    Log.i(TAG, "Loading page " + page + "...");
    if (page == STARTING_PAGE) {
      // Only show progress bar when loading the first page
      loadingIndicator.setVisibility(View.VISIBLE);
    }
    URL url = HttpUtils.buildDiscoverQueryUrl(sortOrder, page);
    return new MoviesLoader(this, url);
  }

  @Override
  public void onLoadFinished(Loader<Bundle> loader, Bundle data) {
    loadingIndicator.setVisibility(View.INVISIBLE);
    totalPages = data.getInt(Movie.TOTAL_PAGES_KEY);
    Movie[] movies = (Movie[]) data.getParcelableArray(Movie.MOVIE_KEY);
    if (!isOnline() || movies == null || movies.length == 0) {
      showError();
    } else {
      showMovies();
      adapter.appendMoviesData(movies);
    }
    getLoaderManager().destroyLoader(0);  // prevent timeout if app goes offline
  }

  @Override
  public void onLoaderReset(Loader<Bundle> loader) {
    loadingIndicator.setVisibility(View.INVISIBLE);
    resetMoviesView();
  }

  // -----------------------------------------------------------------------------------------------
  // Options menu

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_refresh) {
      refreshMoviesView();
      return true;
    } else if (id == R.id.action_most_popular) {
      if (!sortOrder.equals(HttpUtils.SORT_BY_POPULARITY)) {
        Log.i(TAG, "Most popular sorting selected");
        sharedPrefs.edit().putString(SORT_ORDER_KEY, HttpUtils.SORT_BY_POPULARITY).apply();
        reloadMovies(HttpUtils.SORT_BY_POPULARITY, R.string.most_popular_title);
      }
      return true;
    } else if (id == R.id.action_top_rated) {
      if (!sortOrder.equals(HttpUtils.SORT_BY_RATING)) {
        Log.i(TAG, "Top rated sorting selected");
        sharedPrefs.edit().putString(SORT_ORDER_KEY, HttpUtils.SORT_BY_RATING).apply();
        reloadMovies(HttpUtils.SORT_BY_RATING, R.string.top_rated_title);
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  // -----------------------------------------------------------------------------------------------
  // Utils

  /**
   * Initialize the number of columns and row height of in RecyclerView grid
   */
  private void initGridDimens() {
    int displayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    int displayHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    int gridColumnsPortrait = getResources().getInteger(R.integer.grid_columns_portrait);
    int gridColumnsLandscape = getResources().getInteger(R.integer.grid_columns_landscape);
    gridColumnsNumber = displayWidth < displayHeight ? gridColumnsPortrait : gridColumnsLandscape;
    int widthRatio = getResources().getInteger(R.integer.poster_width_ratio);
    int heightRatio = getResources().getInteger(R.integer.poster_height_ratio);
    gridRowHeight = displayWidth * heightRatio / (gridColumnsNumber * widthRatio);
  }

  /**
   * Check internet connection
   */
  private boolean isOnline() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = cm != null ? cm.getActiveNetworkInfo() : null;
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
  }

  /**
   * Show movies grid
   */
  private void showMovies() {
    moviesView.setVisibility(View.VISIBLE);
    errorView.setVisibility(View.INVISIBLE);
  }

  /**
   * Show error message
   */
  private void showError() {
    moviesView.setVisibility(View.INVISIBLE);
    errorView.setVisibility(View.VISIBLE);
  }

  /**
   * Change title and sorting mode, then refreshMoviesView
   */
  private void reloadMovies(String sortBy, int titleId) {
    setTitle(getString(titleId));
    sortOrder = sortBy;
    refreshMoviesView();
  }

  /**
   * Refresh RecyclerView
   */
  private void refreshMoviesView() {
    resetMoviesView();
    // Restart loader
    getLoaderManager().restartLoader(0, null, this);
    // Scroll RecyclerView to top
    GridLayoutManager layoutManager = (GridLayoutManager) moviesView.getLayoutManager();
    layoutManager.scrollToPositionWithOffset(0, 0);
  }

  /**
   * Reset RecyclerView state
   */
  private void resetMoviesView() {
    adapter.resetMoviesData();
    scrollListener.resetState();
    page = STARTING_PAGE;
    showMovies();
  }
}
