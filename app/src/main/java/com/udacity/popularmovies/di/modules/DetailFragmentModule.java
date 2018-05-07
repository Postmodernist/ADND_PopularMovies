package com.udacity.popularmovies.di.modules;

import android.arch.lifecycle.ViewModelProviders;

import com.udacity.popularmovies.activities.MainActivity;
import com.udacity.popularmovies.di.scopes.DetailFragmentScope;
import com.udacity.popularmovies.fragments.DetailFragment;
import com.udacity.popularmovies.viewmodels.MovieViewModel;
import com.udacity.popularmovies.viewmodels.MovieViewModelFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class DetailFragmentModule {

  @Provides
  @DetailFragmentScope
  MovieViewModel provideMoviesViewModel(MainActivity mainActivity, MovieViewModelFactory viewModelFactory) {
    return ViewModelProviders.of(mainActivity, viewModelFactory).get(MovieViewModel.class);
  }

  @Provides
  MainActivity provideMainActivity(DetailFragment fragment) {
    return (MainActivity) fragment.getActivity();
  }
}
