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
import com.udacity.popularmovies.database.MovieContract.MovieEntry;
import com.udacity.popularmovies.di.qualifiers.DiskExecutor;
import com.udacity.popularmovies.di.qualifiers.MainThreadExecutor;
import com.udacity.popularmovies.di.qualifiers.NetworkExecutor;
import com.udacity.popularmovies.model.detail.MovieDetail;
import com.udacity.popularmovies.model.detail.MovieVideo;
import com.udacity.popularmovies.model.detail.MovieVideos;
import com.udacity.popularmovies.model.discover.MovieItem;
import com.udacity.popularmovies.model.discover.MoviePage;
import com.udacity.popularmovies.utils.ApiUtils;
import com.udacity.popularmovies.utils.AsyncFuncWithCallback;

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
  private MutableLiveData<List<MovieVideo>> liveMovieVideos = new MutableLiveData<>();

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

  public void loadMoviesPage(String sortBy, int page) {
    Call<MoviePage> call = movieApi.getMovies(ApiUtils.discoveryQueryOptions(sortBy, page));
    liveLoadingStatus.setValue(true);
    networkExecutor.execute(() -> call.enqueue(new Callback<MoviePage>() {
      @Override
      public void onResponse(@NonNull Call<MoviePage> call, @NonNull Response<MoviePage> response) {
        if (response.isSuccessful()) {
          MoviePage moviePage = response.body();
          liveMoviesPage.postValue(moviePage != null ? moviePage.getResults() : null);
          Log.d(TAG, "Finished loading page " + page);
        } else {
          Log.d(TAG, "Failed to load page " + page + ". Error code: " + response.code());
        }
        liveLoadingStatus.postValue(false);
      }

      @Override
      public void onFailure(@NonNull Call<MoviePage> call, @NonNull Throwable t) {
        Log.d(TAG, "Failed to load page " + page + ". Call " + call.toString());
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
          Log.d(TAG, "Finished loading movie detail, movie_id " + movieId);
        } else {
          Log.d(TAG, "Failed to load movie detail, movie_id " + movieId + ". Error code: " + response.code());
        }
      }

      @Override
      public void onFailure(@NonNull Call<MovieDetail> call, @NonNull Throwable t) {
        Log.d(TAG, "Failed to load movie detail, call " + call.toString());
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
          Log.d(TAG, "Finished loading movie videos, movie_id " + movieId);
        } else {
          Log.d(TAG, "Failed to load movie videos, movie_id " + movieId + ". Error code: " + response.code());
        }
      }

      @Override
      public void onFailure(@NonNull Call<MovieVideos> call, @NonNull Throwable t) {
        Log.d(TAG, "Failed to load movie videos, call " + call.toString());
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
          Log.d(TAG, "Rows returned: " + cursor.getCount());
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
        Log.d(TAG, "Reading from star database: movie_id " + movie.getId());
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
        Log.e(TAG, "Failed to read from database. Movie id " + movie.getId());
      }
    }.run();
  }

  /** Save movie to local star db and return row id */
  public void starMovie(@NonNull MovieDetail movie, ConsumerCallback<Long> callback) {
    new AsyncFuncWithCallback<Long>(diskExecutor, mainThreadExecutor) {
      @Override
      public Long func() {
        // WARNING: Assumes the movie object is legit
        Log.d(TAG, "Starring movie, movie_id " + movie.getId());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieEntry.COLUMN_MOVIE_ID, movie.getId());
        contentValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        contentValues.put(MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        contentValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
        contentValues.put(MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        contentValues.put(MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        Uri uri = application.getContentResolver().insert(MovieEntry.CONTENT_URI, contentValues);
        if (uri != null) {
          Log.d(TAG, "Starred: " + uri);
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
        Log.e(TAG, "Failed to write to database. Movie id " + movie.getId());
      }
    }.run();
  }

  /** Remove starred movie from local star database */
  public void unstarMovie(long id, ConsumerCallback<Void> callback) {
    new AsyncFuncWithCallback<Void>(diskExecutor, mainThreadExecutor) {
      @Override
      public Void func() {
        Log.d(TAG, "Unstarring movie, database_id " + id);
        int rowsRemoved = application.getContentResolver().delete(
            ContentUris.withAppendedId(MovieEntry.CONTENT_URI, id),
            null,
            null);
        Log.d(TAG, "Rows removed: " + rowsRemoved);
        return null;
      }

      @Override
      public void onSuccess(@NonNull Void result) {
        callback.accept(result);
      }

      @Override
      public void onFailure(@NonNull Throwable t) {
        Log.e(TAG, "Failed to delete movie from database. Movie database id " + id);
      }
    }.run();
  }

  /** Custom consumer to use with older API */
  public interface ConsumerCallback<T> {
    void accept(T t);
  }
}
