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
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.udacity.popularmovies.api.MovieApi;
import com.udacity.popularmovies.database.MovieContract;
import com.udacity.popularmovies.di.qualifiers.DiskExecutor;
import com.udacity.popularmovies.di.qualifiers.MainThreadExecutor;
import com.udacity.popularmovies.di.qualifiers.NetworkExecutor;
import com.udacity.popularmovies.model.detail.MovieDetail;
import com.udacity.popularmovies.model.discover.MovieItem;
import com.udacity.popularmovies.model.discover.MoviePage;
import com.udacity.popularmovies.utils.ApiUtils;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

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

  @Inject
  @DiskExecutor
  Executor diskExecutor;

  @Inject
  @NetworkExecutor
  Executor networkExecutor;

  @Inject
  @MainThreadExecutor
  Executor mainThreadExecutor;

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
    Call<MoviePage> discoverApiCall =
        movieApi.getMovies(ApiUtils.discoveryQueryOptions(sortBy, page));
    liveLoadingStatus.setValue(true);
    networkExecutor.execute(() -> discoverApiCall.enqueue(new Callback<MoviePage>() {

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
    Call<MovieDetail> movieApiCall =
        movieApi.getMovieDetail(movieId, ApiUtils.detailQueryOptions());
    networkExecutor.execute(() -> movieApiCall.enqueue(new Callback<MovieDetail>() {

      @Override
      public void onResponse(@NonNull Call<MovieDetail> call,
                             @NonNull Response<MovieDetail> response) {
        if (response.isSuccessful()) {
          liveMovieDetail.setValue(response.body());
          Log.d(TAG, "Finished loading movie detail, movie_id " + movieId);
        } else {
          Log.d(TAG, "Failed to load movie detail, movie_id " + movieId + ". Error code: " + response.code());
        }
      }

      @Override
      public void onFailure(@NonNull Call<MovieDetail> call,
                            @NonNull Throwable t) {
        Log.d(TAG, "Failed to load movie detail, movie_id " + movieId);
      }
    }));
  }

  public LiveData<MovieDetail> getMovieDetail() {
    return liveMovieDetail;
  }

  /** Load starred movie by its movieId */
  public void getStarredMovie(@NonNull MovieDetail movie, StarInitCallback callback) {
    runWithCallback(() -> {
      Log.d(TAG, "Reading from star database: movie_id " + movie.getId());
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
    }, callback, "Failed to read from database");
  }

  /** Save movie to local star db and return row id */
  public void starMovie(@NonNull MovieDetail movie, StarCallback callback) {
    runWithCallback(() -> {
      // WARNING: Assumes the movie object is legit
      Log.d(TAG, "Starring movie, movie_id " + movie.getId());
      ContentValues contentValues = new ContentValues();
      contentValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getOriginalTitle());
      contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getId());
      Uri uri = application.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);
      if (uri != null) {
        Log.d(TAG, "Starred: " + uri);
        return ContentUris.parseId(uri);
      }
      return -1L;
    }, callback, "Failed to write to database");
  }

  /** Remove starred movie from local star db */
  public void unstarMovie(long id, UnstarCallback callback) {
    runWithCallback(() -> {
      Log.d(TAG, "Unstarring movie, database_id " + id);
      int rowsRemoved = application.getContentResolver().delete(
          ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, id),
          null,
          null);
      Log.d(TAG, "Rows removed: " + rowsRemoved);
      return null;
    }, callback, "Failed to delete movie from database");
  }

  /** Run callbackFunc on the main thread after asyncFunc execution is finished */
  private <T> void runWithCallback(@NonNull Callable<T> asyncFunc,
                                   @NonNull CallbackConsumer<T> callbackFunc,
                                   @Nullable String errorMessage) {
    try {
      ListeningExecutorService service = MoreExecutors.listeningDecorator((ExecutorService) diskExecutor);
      ListenableFuture<T> future = service.submit(asyncFunc);
      Futures.addCallback(future, new FutureCallback<T>() {

        @Override
        public void onSuccess(@NonNull T result) {
          callbackFunc.accept(result);
        }

        @Override
        public void onFailure(@NonNull Throwable t) {
          if (errorMessage != null) {
            Log.e(TAG, errorMessage);
          }
        }
      }, mainThreadExecutor);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Callback to finish star button initialization */
  public interface StarInitCallback extends CallbackConsumer<Cursor> {
    void accept(Cursor cursor);
  }

  /** Callback to finish star button setup */
  public interface StarCallback extends CallbackConsumer<Long> {
    void accept(Long id);
  }

  /** Callback to finish star button setup */
  public interface UnstarCallback extends CallbackConsumer<Void> {
    void accept(Void v);
  }

  /** Custom consumer to use with older API */
  public interface CallbackConsumer<T> {
    void accept(T t);
  }
}
