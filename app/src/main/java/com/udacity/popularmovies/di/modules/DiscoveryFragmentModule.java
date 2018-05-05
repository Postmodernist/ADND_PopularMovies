package com.udacity.popularmovies.di.modules;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.GridLayoutManager;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.activities.MainActivity;
import com.udacity.popularmovies.adapters.MoviesAdapter;
import com.udacity.popularmovies.di.scopes.DiscoveryFragmentScope;
import com.udacity.popularmovies.fragments.DiscoveryFragment;
import com.udacity.popularmovies.viewmodels.MoviesViewModel;
import com.udacity.popularmovies.viewmodels.MoviesViewModelFactory;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class DiscoveryFragmentModule {

  @Provides
  @DiscoveryFragmentScope
  MoviesViewModel provideMoviesViewModel(MainActivity mainActivity, MoviesViewModelFactory viewModelFactory) {
    return ViewModelProviders.of(mainActivity, viewModelFactory).get(MoviesViewModel.class);
  }

  @Provides
  GridLayoutManager provideGridLayoutManager(Context context, @Named("columns number") int columnsNumber) {
    return new GridLayoutManager(context, columnsNumber);
  }

  @Provides
  MoviesAdapter provideMoviesAdapter(DiscoveryFragment fragment, MainActivity activity, @Named("row height") int rowHeight) {
    return new MoviesAdapter(rowHeight, (movie, position) -> {
      if (fragment.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
        activity.showMovieDetails(movie, position);
      }
    });
  }

  @Provides
  @Named("columns number")
  int provideColumnsNumber(Context context) {
    int displayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    int displayHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    int gridColumnsPortrait = context.getResources().getInteger(R.integer.grid_columns_portrait);
    int gridColumnsLandscape = context.getResources().getInteger(R.integer.grid_columns_landscape);
    return displayWidth < displayHeight ? gridColumnsPortrait : gridColumnsLandscape;
  }

  @Provides
  @Named("row height")
  int provideRowHeight(Context context, @Named("columns number") int columnsNumber) {
    int displayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    int widthRatio = context.getResources().getInteger(R.integer.poster_width_ratio);
    int heightRatio = context.getResources().getInteger(R.integer.poster_height_ratio);
    return displayWidth * heightRatio / (columnsNumber * widthRatio);
  }

  @Provides
  Context provideContext(DiscoveryFragment fragment) {
    return fragment.getContext();
  }

  @Provides
  MainActivity provideMainActivity(DiscoveryFragment fragment) {
    return (MainActivity) fragment.getActivity();
  }
}
