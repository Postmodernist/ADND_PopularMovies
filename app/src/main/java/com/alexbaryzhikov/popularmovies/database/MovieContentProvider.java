package com.alexbaryzhikov.popularmovies.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.alexbaryzhikov.popularmovies.database.MovieContract.MovieEntry;

public class MovieContentProvider extends ContentProvider {

  private static final int MOVIES = 100;
  private static final int MOVIE_WITH_ID = 101;

  private static final UriMatcher uriMatcher = buildUriMatcher();

  private MovieDbHelper movieDbHelper;

  private static UriMatcher buildUriMatcher() {
    UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES, MOVIES);
    uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES + "/#", MOVIE_WITH_ID);
    return uriMatcher;
  }

  @Override
  public boolean onCreate() {
    movieDbHelper = new MovieDbHelper(getContext());
    return true;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                      @Nullable String[] selectionArgs, @Nullable String sortOrder) {
    final SQLiteDatabase db = movieDbHelper.getReadableDatabase();
    Cursor returnCursor;

    switch (uriMatcher.match(uri)) {
      case MOVIES:
        String limit = uri.getQueryParameter("limit");
        returnCursor = db.query(MovieEntry.TABLE_NAME, projection, selection, selectionArgs,
            null, null, sortOrder, limit);
        break;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    if (getContext() != null) {
      returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
    }

    return returnCursor;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    switch (uriMatcher.match(uri)) {
      case MOVIES:
        return "vnd.android.cursor.dir" + "/" + MovieContract.AUTHORITY + "/" + MovieContract.PATH_MOVIES;
      case MOVIE_WITH_ID:
        return "vnd.android.cursor.item" + "/" + MovieContract.AUTHORITY + "/" + MovieContract.PATH_MOVIES;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
    SQLiteDatabase db = movieDbHelper.getWritableDatabase();
    Uri returnUri;

    switch (uriMatcher.match(uri)) {
      case MOVIES:
        long newRowId = db.insert(MovieEntry.TABLE_NAME, null, values);
        if (newRowId > 0) {
          returnUri = ContentUris.withAppendedId(MovieEntry.CONTENT_URI, newRowId);
        } else {
          throw new SQLException("Failed to insert row into " + uri);
        }
        break;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    if (getContext() != null) {
      getContext().getContentResolver().notifyChange(uri, null);
    }

    return returnUri;
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
    final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
    int tasksDeleted; // starts as 0

    switch (uriMatcher.match(uri)) {
      case MOVIE_WITH_ID:
        String id = uri.getPathSegments().get(1);
        tasksDeleted = db.delete(MovieEntry.TABLE_NAME, "_id=?", new String[]{id});
        break;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    if (tasksDeleted != 0 && getContext() != null) {
      getContext().getContentResolver().notifyChange(uri, null);
    }

    return tasksDeleted;
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                    @Nullable String[] selectionArgs) {
    final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
    int tasksUpdated;

    switch (uriMatcher.match(uri)) {
      case MOVIE_WITH_ID:
        String id = uri.getPathSegments().get(1);
        tasksUpdated = db.update(MovieEntry.TABLE_NAME, values, "_id=?", new String[]{id});
        break;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    if (tasksUpdated != 0 && getContext() != null) {
      getContext().getContentResolver().notifyChange(uri, null);
    }

    return tasksUpdated;
  }
}
