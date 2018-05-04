package com.udacity.popularmovies.fragments;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.activities.MainActivity;
import com.udacity.popularmovies.adapters.MoviesAdapter;
import com.udacity.popularmovies.api.ApiUtils;
import com.udacity.popularmovies.repositories.MoviesRepository;
import com.udacity.popularmovies.utils.EndlessRecyclerViewScrollListener;
import com.udacity.popularmovies.viewmodels.MoviesViewModel;
import com.udacity.popularmovies.viewmodels.MoviesViewModelFactory;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DiscoveryFragment extends Fragment {

  private static final String TAG = "TAG_" + DiscoveryFragment.class.getSimpleName();

  @BindView(R.id.rv_movies)
  RecyclerView moviesView;
  @BindView(R.id.tv_error)
  TextView errorView;
  @BindView(R.id.pb_loading)
  ProgressBar progressBar;

  private int gridColumnsNumber;
  private int gridRowHeight;

  private MainActivity mainActivity;
  private MoviesAdapter adapter;
  private MoviesViewModel viewModel;
  private EndlessRecyclerViewScrollListener scrollListener;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_discovery, container, false);
    ButterKnife.bind(this, view);

    setupGrid();

    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mainActivity = (MainActivity) Objects.requireNonNull(getActivity());
    setupViewModel();

    // Change title according to sort order
    if (viewModel.getSortBy().equals(ApiUtils.SORT_BY_POPULARITY)) {
      mainActivity.setTitle(getString(R.string.most_popular_title));
    } else {
      mainActivity.setTitle(getString(R.string.top_rated_title));
    }

  }

  private void setupGrid() {
    initGridDimens();
    GridLayoutManager layoutManager = new GridLayoutManager(getContext(), gridColumnsNumber);

    adapter = new MoviesAdapter(gridRowHeight, (movie, position) -> {
      if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
        mainActivity.showMovieDetails(movie, position);
      }
    });

    scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
      @Override
      public void onLoadMore() {
        if (mainActivity.isOnline()) {
          Log.d(TAG, "Requesting more data...");
          viewModel.loadMore();
        }
      }
    };

    moviesView.setLayoutManager(layoutManager);
    moviesView.setAdapter(adapter);
    moviesView.addOnScrollListener(scrollListener);
  }

  private void setupViewModel() {
    MoviesRepository repository = MoviesRepository.getInstance();
    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);

    viewModel = ViewModelProviders
        .of(mainActivity, new MoviesViewModelFactory(repository, sharedPrefs))
        .get(MoviesViewModel.class);
    viewModel.init();
    viewModel.getMoviesList().observe(this, moviesList -> adapter.submitList(moviesList));
    viewModel.getLoadingStatus().observe(this, this::updateViewsVisibility);
  }

  // -----------------------------------------------------------------------------------------------
  // Options menu

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.main, menu);
    super.onCreateOptionsMenu(menu, inflater);
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
      changeSortOrder(ApiUtils.SORT_BY_POPULARITY, R.string.most_popular_title);
      return true;
    } else if (id == R.id.action_top_rated) {
      Log.d(TAG, "Top rated sorting selected");
      changeSortOrder(ApiUtils.SORT_BY_RATING, R.string.top_rated_title);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Change movies sorting order
   */
  private void changeSortOrder(String sortBy, int titleId) {
    if (viewModel.setSortBy(sortBy)) {
      mainActivity.setTitle(getString(titleId));
      refreshUi();
    }
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
