package com.udacity.popularmovies.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
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

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.adapters.MoviesAdapter;
import com.udacity.popularmovies.api.DiscoverApi;
import com.udacity.popularmovies.api.HttpUtils;
import com.udacity.popularmovies.database.entity.Movie;
import com.udacity.popularmovies.model.discover.Result;
import com.udacity.popularmovies.repositories.MoviesRepository;
import com.udacity.popularmovies.utils.EndlessRecyclerViewScrollListener;
import com.udacity.popularmovies.viewmodels.MoviesViewModel;
import com.udacity.popularmovies.viewmodels.MoviesViewModelFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesOnClickHandler {

  private static final String TAG = "TAG_" + MainActivity.class.getSimpleName();

  @BindView(R.id.rv_movies)
  RecyclerView moviesView;
  @BindView(R.id.tv_error)
  TextView errorView;
  @BindView(R.id.pb_loading)
  ProgressBar progressBar;

  private MoviesAdapter adapter;
  private MoviesViewModel viewModel;
  private EndlessRecyclerViewScrollListener scrollListener;
  private int gridColumnsNumber;
  private int gridRowHeight;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    // Setup RecyclerView
    initGridDimens();
    GridLayoutManager layoutManager = new GridLayoutManager(this, gridColumnsNumber);
    adapter = new MoviesAdapter(this, gridRowHeight);
    scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
      @Override
      public void onLoadMore() {
        if (isOnline()) {
          Log.d(TAG, "Requesting more data...");
          viewModel.loadMore();
        }
      }
    };
    moviesView.setLayoutManager(layoutManager);
    moviesView.setAdapter(adapter);
    moviesView.addOnScrollListener(scrollListener);

    // Setup ViewModel
    MoviesRepository repository = MoviesRepository.getInstance();
    if (repository == null) {
      Retrofit retrofit = new Retrofit.Builder()
          .baseUrl(HttpUtils.BASE_URL)
          .addConverterFactory(GsonConverterFactory.create())
          .build();
      DiscoverApi discoverApi = retrofit.create(DiscoverApi.class);
      Executor executor = Executors.newSingleThreadExecutor();
      repository = new MoviesRepository(discoverApi, executor);
    }
    SharedPreferences sharedPrefs =
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    viewModel = ViewModelProviders.of(this, new MoviesViewModelFactory(repository, sharedPrefs))
        .get(MoviesViewModel.class);
    viewModel.init();
    viewModel.getMoviesList().observe(this, moviesList -> adapter.submitList(moviesList));
    viewModel.getLoadingStatus().observe(this, this::updateViewsVisibility);

    // Change title according to sort order
    if (viewModel.getSortBy().equals(HttpUtils.SORT_BY_POPULARITY)) {
      setTitle(getString(R.string.most_popular_title));
    } else {
      setTitle(getString(R.string.top_rated_title));
    }
  }

  @Override
  public void onClick(Result movie) {
    Log.d(TAG, "Item clicked: " + movie.getTitle());
    Intent intent = new Intent(this, DetailActivity.class);
    intent.putExtra(Movie.MOVIE_KEY, movie);
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
    if (id == R.id.action_refresh) {
      viewModel.refresh();
      refreshUi();
      return true;
    } else if (id == R.id.action_most_popular) {
      Log.d(TAG, "Most popular sorting selected");
      changeSortOrder(HttpUtils.SORT_BY_POPULARITY, R.string.most_popular_title);
      return true;
    } else if (id == R.id.action_top_rated) {
      Log.d(TAG, "Top rated sorting selected");
      changeSortOrder(HttpUtils.SORT_BY_RATING, R.string.top_rated_title);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  // -----------------------------------------------------------------------------------------------

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
   * Change movies sorting order
   */
  private void changeSortOrder(String sortBy, int titleId) {
    viewModel.setSortBy(sortBy);
    setTitle(getString(titleId));
    refreshUi();
  }

  /**
   * Refresh UI
   */
  private void refreshUi() {
    // Scroll RecyclerView to top
    GridLayoutManager layoutManager = (GridLayoutManager) moviesView.getLayoutManager();
    layoutManager.scrollToPositionWithOffset(0, 0);
    // Reset adapter
    adapter.submitList(null);
    scrollListener.resetState();
  }

  /**
   * Update UI elements visibility
   */
  private void updateViewsVisibility(Boolean loading) {
    if (!loading && adapter.getItemCount() == 0) {
      // Finished loading but nothing to show
      progressBar.setVisibility(View.INVISIBLE);
      errorView.setVisibility(View.VISIBLE);
      moviesView.setVisibility(View.INVISIBLE);

    } else if (!loading && adapter.getItemCount() != 0) {
      // Finished loading successfully
      progressBar.setVisibility(View.INVISIBLE);
      errorView.setVisibility(View.INVISIBLE);
      moviesView.setVisibility(View.VISIBLE);

    } else if (loading && adapter.getItemCount() == 0) {
      // Started loading but nothing to show
      progressBar.setVisibility(View.VISIBLE);
      errorView.setVisibility(View.INVISIBLE);
      moviesView.setVisibility(View.INVISIBLE);

    } else if (loading && adapter.getItemCount() != 0) {
      // Started loading while showing data
      progressBar.setVisibility(View.INVISIBLE);
      errorView.setVisibility(View.INVISIBLE);
      moviesView.setVisibility(View.VISIBLE);
    }
  }
}
