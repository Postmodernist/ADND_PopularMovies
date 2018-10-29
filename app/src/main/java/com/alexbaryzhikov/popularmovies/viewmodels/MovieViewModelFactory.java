package com.alexbaryzhikov.popularmovies.viewmodels;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.alexbaryzhikov.popularmovies.repositories.MovieRepository;

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
