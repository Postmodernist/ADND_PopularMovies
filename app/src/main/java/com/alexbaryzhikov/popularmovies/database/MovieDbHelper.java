package com.alexbaryzhikov.popularmovies.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alexbaryzhikov.popularmovies.database.MovieContract.MovieEntry;

public final class MovieDbHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "MoviesDb.db";
  private static final int DATABASE_VERSION = 3;

  MovieDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " +
        MovieEntry.TABLE_NAME + " (" +
        MovieEntry._ID + " INTEGER PRIMARY KEY, " +
        MovieEntry.COLUMN_MOVIE_ID + " TEXT, " +
        MovieEntry.COLUMN_VOTE_AVERAGE + " DOUBLE, " +
        MovieEntry.COLUMN_POSTER_PATH + " TEXT, " +
        MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT, " +
        MovieEntry.COLUMN_OVERVIEW + " TEXT, " +
        MovieEntry.COLUMN_RELEASE_DATE + " TEXT)"
    );
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
    onCreate(db);
  }
}
