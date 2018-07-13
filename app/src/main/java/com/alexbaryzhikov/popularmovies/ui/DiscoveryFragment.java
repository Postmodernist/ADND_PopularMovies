package com.alexbaryzhikov.popularmovies.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alexbaryzhikov.popularmovies.MovieApplication;
import com.alexbaryzhikov.popularmovies.R;
import com.alexbaryzhikov.popularmovies.di.components.DaggerDiscoveryFragmentComponent;
import com.alexbaryzhikov.popularmovies.di.components.DiscoveryFragmentComponent;
import com.alexbaryzhikov.popularmovies.utils.ConnectionUtils;
import com.alexbaryzhikov.popularmovies.utils.EndlessRecyclerViewScrollListener;
import com.alexbaryzhikov.popularmovies.viewmodels.MovieViewModel;
import com.alexbaryzhikov.popularmovies.viewmodels.MovieViewModel.ListMode;

import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DiscoveryFragment extends Fragment {

  @BindView(R.id.rv_movies) RecyclerView moviesView;
  @BindView(R.id.tv_error) TextView errorView;
  @BindView(R.id.pb_loading) ProgressBar progressBar;

  @Inject MovieViewModel viewModel;
  @Inject MovieAdapter adapter;

  private DiscoveryFragmentComponent discoveryFragmentComponent;
  private MainActivity mainActivity;
  private EndlessRecyclerViewScrollListener scrollListener;

  @Override
  public void onAttach(Context context) {
    setupDagger();
    super.onAttach(context);
  }

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
    switch (viewModel.getListMode()) {
      case MOST_POPULAR:
        mainActivity.setTitle(getString(R.string.most_popular_title));
        break;
      case TOP_RATED:
        mainActivity.setTitle(getString(R.string.top_rated_title));
        break;
      case STARRED:
        mainActivity.setTitle(getString(R.string.starred_title));
        break;
    }
  }

  private void setupDagger() {
    discoveryFragmentComponent = DaggerDiscoveryFragmentComponent.builder()
        .appComponent(MovieApplication.getAppComponent(Objects.requireNonNull(getContext())))
        .fragment(this)
        .build();
    discoveryFragmentComponent.inject(this);
  }

  private void setupGrid() {
    GridLayoutManager layoutManager = discoveryFragmentComponent.gridLayoutManager();
    scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
      @Override
      public void onLoadMore() {
        if (ConnectionUtils.isOnline(mainActivity.getApplication())) {
          viewModel.loadMore();
        }
      }
    };
    moviesView.setLayoutManager(layoutManager);
    moviesView.setAdapter(adapter);
    moviesView.addOnScrollListener(scrollListener);
  }

  private void setupViewModel() {
    viewModel.init();
    viewModel.getMovieList().observe(this, adapter::submitList);
    viewModel.getLoadingStatus().observe(this, this::updateViewsVisibility);
  }

  /**
   * Update UI elements visibility
   */
  public void updateViewsVisibility(Boolean loading) {
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

  // -----------------------------------------------------------------------------------------------
  // Options menu

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.main, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {

      case R.id.action_refresh:
        viewModel.refresh();
        refreshUi();
        return true;

      case R.id.action_most_popular:
        setListMode(ListMode.MOST_POPULAR, R.string.most_popular_title);
        return true;

      case R.id.action_top_rated:
        setListMode(ListMode.TOP_RATED, R.string.top_rated_title);
        return true;

      case R.id.action_starred:
        setListMode(ListMode.STARRED, R.string.starred_title);
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  /**
   * Change movies list mode
   */
  private void setListMode(ListMode mode, int titleId) {
    if (viewModel.setListMode(mode)) {
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

}
