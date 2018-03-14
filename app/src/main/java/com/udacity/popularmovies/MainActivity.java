package com.udacity.popularmovies;

import android.content.Intent;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesOnClickHandler {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MoviesAdapter adapter;
  private int gridColumnsNumber = 2;

  @BindView(R.id.rv_movies)
  RecyclerView moviesView;
  @BindView(R.id.tv_error)
  TextView errorView;
  @BindView(R.id.pb_loading)
  ProgressBar loadingIndicator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    GridLayoutManager layoutManager = new GridLayoutManager(this, gridColumnsNumber);
    moviesView.setLayoutManager(layoutManager);
    adapter = new MoviesAdapter(this, getGridRowHeight());
    moviesView.setAdapter(adapter);

    if (isOnline()) {
      loadMovies();
    } else {
      showError();
    }
  }

  @Override
  public void onClick(Movie movie) {
    Log.i(TAG, "Item clicked");
    Intent intent = new Intent(this, DetailActivity.class);
    intent.putExtra(Movie.extraKey, movie);
    startActivity(intent);
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
   * Run background task to get movies data and feed it to the adapter
   */
  private void loadMovies() {
    showMovies();
    // Mock movies data
    Movie[] movies = MoviesMockData.get();
    adapter.setMoviesData(movies);
    // TODO load movies data via AsyncTask
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
   * Show movies grid
   */
  private void showError() {
    moviesView.setVisibility(View.INVISIBLE);
    errorView.setVisibility(View.VISIBLE);
  }

  /**
   * Return RecyclerView grid row height
   */
  private int getGridRowHeight() {
    int displayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    int gridItemWidth = 2;
    int gridItemHeight = 3;
    return displayWidth * gridItemHeight / (gridColumnsNumber * gridItemWidth);
  }
}
