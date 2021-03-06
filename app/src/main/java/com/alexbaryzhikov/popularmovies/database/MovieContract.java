package com.alexbaryzhikov.popularmovies.database;

import android.net.Uri;
import android.provider.BaseColumns;

public final class MovieContract {

  public static final String AUTHORITY = "com.alexbaryzhikov.popularmovies.provider";
  public static final String PATH_MOVIES = "movies";

  private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

  private MovieContract() {
    // Prevents instantiation
  }

  public static class MovieEntry implements BaseColumns {

    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

    public static final String TABLE_NAME = "movies";

    public static final String COLUMN_MOVIE_ID = "movie_id";
    public static final String COLUMN_VOTE_AVERAGE = "vote_average";
    public static final String COLUMN_POSTER_PATH = "poster_path";
    public static final String COLUMN_ORIGINAL_TITLE = "original_title";
    public static final String COLUMN_OVERVIEW = "overview";
    public static final String COLUMN_RELEASE_DATE = "release_date";
  }
}
