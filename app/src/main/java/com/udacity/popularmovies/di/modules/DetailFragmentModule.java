package com.udacity.popularmovies.di.modules;

import android.arch.lifecycle.ViewModelProviders;

import com.udacity.popularmovies.activities.MainActivity;
import com.udacity.popularmovies.di.scopes.DetailFragmentScope;
import com.udacity.popularmovies.fragments.DetailFragment;
import com.udacity.popularmovies.viewmodels.MoviesViewModel;
import com.udacity.popularmovies.viewmodels.MoviesViewModelFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class DetailFragmentModule {

  @Provides
  @DetailFragmentScope
  MoviesViewModel provideMoviesViewModel(MainActivity mainActivity, MoviesViewModelFactory viewModelFactory) {
    return ViewModelProviders.of(mainActivity, viewModelFactory).get(MoviesViewModel.class);
  }

  @Provides
  MainActivity provideMainActivity(DetailFragment fragment) {
    return (MainActivity) fragment.getActivity();
  }
}
