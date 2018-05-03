package com.udacity.popularmovies.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.udacity.popularmovies.repositories.MoviesRepository;

public class MoviesViewModelFactory extends ViewModelProvider.NewInstanceFactory {

  private MoviesRepository moviesRepository;
  private SharedPreferences sharedPrefs;

  public MoviesViewModelFactory(MoviesRepository moviesRepository, SharedPreferences sharedPrefs) {
    this.moviesRepository = moviesRepository;
    this.sharedPrefs = sharedPrefs;
  }

  @NonNull
  @Override
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    // noinspection unchecked
    return (T) new MoviesViewModel(moviesRepository, sharedPrefs);
  }
}
