package com.udacity.popularmovies.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.udacity.popularmovies.model.discover.Result;
import com.udacity.popularmovies.repositories.MoviesRepository;

import java.util.List;

public class MoviesViewModel extends ViewModel {

  private MoviesRepository moviesRepository;
  private LiveData<List<Result>> moviesList;
  private LiveData<Boolean> loading;

  MoviesViewModel(MoviesRepository moviesRepository) {
    this.moviesRepository = moviesRepository;
  }

  public void init() {
    if (moviesList != null) {
      return;
    }
    moviesList = moviesRepository.getMoviesList();
    loading = moviesRepository.getLoading();
  }

  public void refresh() {
    moviesRepository.refresh();
  }

  public void loadMore() {
    moviesRepository.loadMore();
  }

  public LiveData<List<Result>> getMoviesList() {
    return moviesList;
  }

  public LiveData<Boolean> getLoading() {
    return loading;
  }

  public String getSortBy() {
    return moviesRepository.getSortBy();
  }

  public void setSortBy(String sortBy) {
    moviesRepository.setSortBy(sortBy);
  }

}
