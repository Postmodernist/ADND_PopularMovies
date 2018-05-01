package com.udacity.popularmovies.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.udacity.popularmovies.repositories.MoviesRepository;

public class MoviesViewModelFactory extends ViewModelProvider.NewInstanceFactory {

  private MoviesRepository moviesRepository;

  public MoviesViewModelFactory(MoviesRepository moviesRepository) {
    this.moviesRepository = moviesRepository;
  }

  @SuppressWarnings("unchecked")
  @NonNull
  @Override
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new MoviesViewModel(moviesRepository);
  }
}
