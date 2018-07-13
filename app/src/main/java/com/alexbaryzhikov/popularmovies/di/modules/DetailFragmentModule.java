package com.alexbaryzhikov.popularmovies.di.modules;

import android.arch.lifecycle.ViewModelProviders;

import com.alexbaryzhikov.popularmovies.ui.MainActivity;
import com.alexbaryzhikov.popularmovies.di.scopes.DetailFragmentScope;
import com.alexbaryzhikov.popularmovies.ui.DetailFragment;
import com.alexbaryzhikov.popularmovies.viewmodels.MovieViewModel;
import com.alexbaryzhikov.popularmovies.viewmodels.MovieViewModelFactory;

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
