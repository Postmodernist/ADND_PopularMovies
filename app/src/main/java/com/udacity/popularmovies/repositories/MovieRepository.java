package com.udacity.popularmovies.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.udacity.popularmovies.api.MovieApi;
import com.udacity.popularmovies.database.MovieContract;
import com.udacity.popularmovies.model.detail.MovieDetail;
import com.udacity.popularmovies.model.discover.MovieItem;
import com.udacity.popularmovies.model.discover.MoviePage;
import com.udacity.popularmovies.utils.ApiUtils;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public final class MovieRepository {

  private static final String TAG = "TAG_" + MovieRepository.class.getSimpleName();

  @Inject Application application;
  @Inject MovieApi movieApi;
  @Inject Executor executor;
  private MutableLiveData<List<MovieItem>> liveMoviesPage = new MutableLiveData<>();
  private MutableLiveData<Boolean> liveLoadingStatus = new MutableLiveData<>();
  private MutableLiveData<MovieDetail> liveMovieDetail = new MutableLiveData<>();

  @Inject
  MovieRepository() {
    liveLoadingStatus.setValue(false);
  }

  // -----------------------------------------------------------------------------------------------
  // Movies list

  public void loadMoviesPage(String sortBy, int page) {
    Call<MoviePage> discoverApiCall = movieApi.getMovies(ApiUtils.discoveryQueryOptions(sortBy, page));
    liveLoadingStatus.setValue(true);
    executor.execute(() -> discoverApiCall.enqueue(new Callback<MoviePage>() {

      @Override
      public void onResponse(@NonNull Call<MoviePage> call,
                             @NonNull Response<MoviePage> response) {
        if (response.isSuccessful()) {
          MoviePage moviePage = response.body();
          liveMoviesPage.setValue(moviePage != null ? moviePage.getResults() : null);
          Log.d(TAG, "Finished loading page " + page);
        } else {
          Log.d(TAG, "Failed to load page " + page + ". Error code: " + response.code());
        }
        liveLoadingStatus.postValue(false);
      }

      @Override
      public void onFailure(@NonNull Call<MoviePage> call,
                            @NonNull Throwable t) {
        Log.d(TAG, "Failed to load page " + page);
        liveLoadingStatus.postValue(false);
      }
    }));
  }

  public LiveData<List<MovieItem>> getMoviesPage() {
    return liveMoviesPage;
  }

  public LiveData<Boolean> getLoadingStatus() {
    return liveLoadingStatus;
  }

  // -----------------------------------------------------------------------------------------------
  // Movie detail

  public void loadMovieDetail(int movieId) {
    Call<MovieDetail> movieApiCall = movieApi.getMovieDetail(movieId, ApiUtils.detailQueryOptions());
    executor.execute(() -> movieApiCall.enqueue(new Callback<MovieDetail>() {

      @Override
      public void onResponse(@NonNull Call<MovieDetail> call,
                             @NonNull Response<MovieDetail> response) {
        if (response.isSuccessful()) {
          liveMovieDetail.setValue(response.body());
          Log.d(TAG, "Finished loading movie detail, id " + movieId);
        } else {
          Log.d(TAG, "Failed to load movie detail, id " + movieId + ". Error code: " + response.code());
        }
      }

      @Override
      public void onFailure(@NonNull Call<MovieDetail> call,
                            @NonNull Throwable t) {
        Log.d(TAG, "Failed to load movie detail, id " + movieId);
      }
    }));
  }

  public LiveData<MovieDetail> getMovieDetail() {
    return liveMovieDetail;
  }

  /** Load starred movie by its movieId */
  @NonNull
  public Cursor getStarredMovie(@NonNull MovieDetail movie) {
    Cursor cursor = application.getContentResolver().query(
        MovieContract.MovieEntry.CONTENT_URI,
        null,
        "movie_id=?",
        new String[]{String.valueOf(movie.getId())},
        null);
    if (cursor == null) {
      throw new SQLException("Failed to read database");
    }
    return cursor;
  }

  /** Save movie to local star db and return row id */
  public long starMovie(MovieDetail movie) {
    // WARNING: Assumes the movie object is legit
    ContentValues contentValues = new ContentValues();
    contentValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getOriginalTitle());
    contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getId());
    Uri uri = application.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);
    if (uri != null) {
      Log.d(TAG, "Starred: " + uri);
      return ContentUris.parseId(uri);
    }
    return -1;
  }

  /** Remove starred movie from local star db */
  public void unstarMovie(long id) {
    int rowsRemoved = application.getContentResolver().delete(
        ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, id),
        null,
        null);
    Log.d(TAG, "Rows removed: " + rowsRemoved);
  }
}
