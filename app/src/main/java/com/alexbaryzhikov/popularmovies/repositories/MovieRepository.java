package com.alexbaryzhikov.popularmovies.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.alexbaryzhikov.popularmovies.api.ApiUtils;
import com.alexbaryzhikov.popularmovies.api.MovieApi;
import com.alexbaryzhikov.popularmovies.database.MovieContract.MovieEntry;
import com.alexbaryzhikov.popularmovies.di.qualifiers.DiskExecutor;
import com.alexbaryzhikov.popularmovies.di.qualifiers.MainThreadExecutor;
import com.alexbaryzhikov.popularmovies.di.qualifiers.NetworkExecutor;
import com.alexbaryzhikov.popularmovies.model.detail.MovieDetail;
import com.alexbaryzhikov.popularmovies.model.detail.MovieReview;
import com.alexbaryzhikov.popularmovies.model.detail.MovieReviews;
import com.alexbaryzhikov.popularmovies.model.detail.MovieVideo;
import com.alexbaryzhikov.popularmovies.model.detail.MovieVideos;
import com.alexbaryzhikov.popularmovies.model.discover.MovieItem;
import com.alexbaryzhikov.popularmovies.model.discover.MoviePage;
import com.alexbaryzhikov.popularmovies.utils.AsyncFuncWithCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public final class MovieRepository {

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
  private MutableLiveData<List<MovieVideo>> liveMovieVideos = new MutableLiveData<>();
  private MutableLiveData<List<MovieReview>> liveMovieReviews = new MutableLiveData<>();

  @Inject
  MovieRepository() {
    liveLoadingStatus.setValue(false);
  }

  public LiveData<List<MovieItem>> getMoviesPage() {
    return liveMoviesPage;
  }

  public LiveData<Boolean> getLoadingStatus() {
    return liveLoadingStatus;
  }

  public LiveData<MovieDetail> getMovieDetail() {
    return liveMovieDetail;
  }

  public LiveData<List<MovieVideo>> getMovieVideos() {
    return liveMovieVideos;
  }

  public LiveData<List<MovieReview>> getMovieReviews() {
    return liveMovieReviews;
  }

  public void loadMoviesPage(String sortBy, int page) {
    Call<MoviePage> call = movieApi.getMovies(ApiUtils.discoveryQueryOptions(sortBy, page));
    liveLoadingStatus.setValue(true);
    networkExecutor.execute(() -> call.enqueue(new Callback<MoviePage>() {
      @Override
      public void onResponse(@NonNull Call<MoviePage> call, @NonNull Response<MoviePage> response) {
        if (response.isSuccessful()) {
          MoviePage moviePage = response.body();
          liveMoviesPage.postValue(moviePage != null ? moviePage.getResults() : null);
        }
        liveLoadingStatus.postValue(false);
      }

      @Override
      public void onFailure(@NonNull Call<MoviePage> call, @NonNull Throwable t) {
        liveLoadingStatus.postValue(false);
      }
    }));
  }

  public void loadMovieDetail(int movieId) {
    Call<MovieDetail> call = movieApi.getMovieDetail(movieId, ApiUtils.detailQueryOptions());
    networkExecutor.execute(() -> call.enqueue(new Callback<MovieDetail>() {
      @Override
      public void onResponse(@NonNull Call<MovieDetail> call, @NonNull Response<MovieDetail> response) {
        if (response.isSuccessful()) {
          liveMovieDetail.postValue(response.body());
        }
      }

      @Override
      public void onFailure(@NonNull Call<MovieDetail> call, @NonNull Throwable t) {
      }
    }));
  }

  public void loadMovieVideos(int movieId) {
    Call<MovieVideos> call = movieApi.getMovieVideos(movieId, ApiUtils.detailQueryOptions());
    networkExecutor.execute(() -> call.enqueue(new Callback<MovieVideos>() {
      @Override
      public void onResponse(@NonNull Call<MovieVideos> call, @NonNull Response<MovieVideos> response) {
        if (response.isSuccessful()) {
          MovieVideos movieVideos = response.body();
          liveMovieVideos.postValue(movieVideos != null ? movieVideos.getResults() : null);
        }
      }

      @Override
      public void onFailure(@NonNull Call<MovieVideos> call, @NonNull Throwable t) {
      }
    }));
  }

  public void loadMovieReviews(int movieId) {
    Call<MovieReviews> call = movieApi.getMovieReviews(movieId, ApiUtils.reviewsQueryOptions());
    networkExecutor.execute(() -> call.enqueue(new Callback<MovieReviews>() {
      @Override
      public void onResponse(@NonNull Call<MovieReviews> call, @NonNull Response<MovieReviews> response) {
        if (response.isSuccessful()) {
          MovieReviews movieReviews = response.body();
          List<MovieReview> results = movieReviews != null ? movieReviews.getResults() : null;
          liveMovieReviews.postValue(results != null ? results : new ArrayList<>(0));
        }
      }

      @Override
      public void onFailure(@NonNull Call<MovieReviews> call, @NonNull Throwable t) {
      }
    }));
  }

  // -----------------------------------------------------------------------------------------------
  // Stars

  /** Load a page of starred movies starting from movie with lastMovieId */
  public void loadStarred(final int lastMovieId, final int pageSize) {
    liveLoadingStatus.setValue(true);
    diskExecutor.execute(() -> {

      final int id = getDatabaseId(lastMovieId);

      final Uri uri = MovieEntry.CONTENT_URI.buildUpon()
          .appendQueryParameter("limit", String.valueOf(pageSize))
          .build();

      try (Cursor cursor = application.getContentResolver().query(
          uri,
          null,
          MovieEntry._ID + " > ?",
          new String[]{String.valueOf(id)},
          MovieEntry._ID)) {
        if (cursor != null) {
          List<MovieItem> movies = new ArrayList<>(pageSize);
          while (cursor.moveToNext()) {
            Integer movieId = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID));
            Double voteAverage = cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE));
            String posterPath = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH));
            String originalTitle = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_TITLE));
            String overview = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW));
            String releaseDate = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
            movies.add(new MovieItem(movieId, voteAverage, posterPath, originalTitle, overview, releaseDate));
          }
          liveMoviesPage.postValue(movies);
        } else {
          throw new SQLException("Failed to read database");
        }
        liveLoadingStatus.postValue(false);
      }
    });
  }

  /** Get database id of movie by its TMDB id */
  private int getDatabaseId(int movieId) {
    if (movieId == -1) {
      // First page
      return movieId;
    } else try (Cursor cursor = application.getContentResolver().query(
        MovieEntry.CONTENT_URI,
        new String[]{MovieEntry._ID},
        MovieEntry.COLUMN_MOVIE_ID + " = ?",
        new String[]{String.valueOf(movieId)},
        null)) {
      if (cursor != null) {
        if (cursor.moveToNext()) {
          return cursor.getInt(cursor.getColumnIndex(MovieEntry._ID));
        } else {
          throw new IllegalArgumentException("Movie not found in star database: " + movieId);
        }
      } else {
        throw new SQLException("Failed to read database");
      }
    }
  }

  /** Load starred movie by its movieId */
  public void getStarredMovie(@NonNull MovieDetail movie, ConsumerCallback<Cursor> callback) {
    new AsyncFuncWithCallback<Cursor>(diskExecutor, mainThreadExecutor) {
      @Override
      public Cursor func() {
        Cursor cursor = application.getContentResolver().query(
            MovieEntry.CONTENT_URI,
            null,
            MovieEntry.COLUMN_MOVIE_ID + " = ?",
            new String[]{String.valueOf(movie.getId())},
            null);
        if (cursor == null) {
          throw new SQLException("Failed to read database");
        }
        return cursor;
      }

      @Override
      public void onSuccess(@NonNull Cursor result) {
        callback.accept(result);
      }

      @Override
      public void onFailure(@NonNull Throwable t) {
      }
    }.run();
  }

  /** Save movie to local star db and return row id */
  public void starMovie(@NonNull MovieDetail movie, ConsumerCallback<Long> callback) {
    new AsyncFuncWithCallback<Long>(diskExecutor, mainThreadExecutor) {
      @Override
      public Long func() {
        // WARNING: Assumes the movie object is legit
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieEntry.COLUMN_MOVIE_ID, movie.getId());
        contentValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        contentValues.put(MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        contentValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
        contentValues.put(MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        contentValues.put(MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        Uri uri = application.getContentResolver().insert(MovieEntry.CONTENT_URI, contentValues);
        if (uri != null) {
          return ContentUris.parseId(uri);
        }
        return -1L;
      }

      @Override
      public void onSuccess(@NonNull Long result) {
        callback.accept(result);
      }

      @Override
      public void onFailure(@NonNull Throwable t) {
      }
    }.run();
  }

  /** Remove starred movie from local star database */
  public void unstarMovie(long id, ConsumerCallback<Void> callback) {
    new AsyncFuncWithCallback<Void>(diskExecutor, mainThreadExecutor) {
      @Override
      public Void func() {
        application.getContentResolver().delete(
            ContentUris.withAppendedId(MovieEntry.CONTENT_URI, id), null, null);
        return null;
      }

      @Override
      public void onSuccess(@NonNull Void result) {
        callback.accept(result);
      }

      @Override
      public void onFailure(@NonNull Throwable t) {
      }
    }.run();
  }

  /** Custom consumer to use with older API */
  @FunctionalInterface
  public interface ConsumerCallback<T> {
    void accept(T t);
  }
}
