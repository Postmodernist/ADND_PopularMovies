package com.udacity.popularmovies.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.udacity.popularmovies.repositories.MovieRepository;

import javax.inject.Inject;

public class MovieViewModelFactory extends ViewModelProvider.NewInstanceFactory {

  @Inject MovieRepository movieRepository;
  @Inject SharedPreferences sharedPrefs;

  @Inject
  public MovieViewModelFactory() {
  }

  @SuppressWarnings("unchecked")
  @NonNull
  @Override
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new MovieViewModel(movieRepository, sharedPrefs);
  }
}
