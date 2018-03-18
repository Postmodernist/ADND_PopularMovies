package com.udacity.popularmovies;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
    implements MoviesAdapter.MoviesOnClickHandler, LoaderManager.LoaderCallbacks<Movie[]> {

  private static final String TAG = MainActivity.class.getSimpleName();

  @BindView(R.id.rv_movies)
  RecyclerView moviesView;
  @BindView(R.id.tv_error)
  TextView errorView;
  @BindView(R.id.pb_loading)
  ProgressBar loadingIndicator;
  private MoviesAdapter adapter;
  private int gridColumnsNumber;
  private int gridRowHeight;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    // TODO Download subsequent pages and update adapter upon scrolling recycler view

    initGridDimens();
    GridLayoutManager layoutManager = new GridLayoutManager(this, gridColumnsNumber);
    moviesView.setLayoutManager(layoutManager);
    adapter = new MoviesAdapter(this, gridRowHeight);
    moviesView.setAdapter(adapter);

    if (isOnline()) {
      showMovies();
      getLoaderManager().initLoader(0, null, this);
    } else {
      showError();
    }
  }

  @Override
  public void onClick(Movie movie) {
    Log.i(TAG, "Item clicked");
    Intent intent = new Intent(this, DetailActivity.class);
    intent.putExtra(Movie.MOVIE_KEY, movie);
    startActivity(intent);
  }

  // -----------------------------------------------------------------------------------------------
  // Loader

  @Override
  public Loader<Movie[]> onCreateLoader(int id, Bundle args) {
    loadingIndicator.setVisibility(View.VISIBLE);
    URL url = HttpUtils.buildDiscoverQueryUrl("popularity.desc", 1);
    return new MoviesLoader(this, url);
  }

  @Override
  public void onLoadFinished(Loader<Movie[]> loader, Movie[] data) {
    loadingIndicator.setVisibility(View.INVISIBLE);
    if (!isOnline() || data == null || data.length == 0) {
      showError();
    } else {
      adapter.setMoviesData(data);
    }
  }

  @Override
  public void onLoaderReset(Loader<Movie[]> loader) {
    loadingIndicator.setVisibility(View.INVISIBLE);
    adapter.setMoviesData(null);
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
    // TODO Make menu choice affect discovery URL
    // TODO Change activity title to reflect sorting mode
    // TODO Save options and restore on app restart
    int id = item.getItemId();
    if (id == R.id.action_most_popular) {
      Log.i(TAG, "Most popular sorting selected");
      return true;
    } else if (id == R.id.action_top_rated) {
      Log.i(TAG, "Top rated sorting selected");
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
}
