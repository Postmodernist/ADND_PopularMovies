package com.udacity.popularmovies.utils;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.udacity.popularmovies.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public final class JsonUtils {

  private static final String TAG = "TAG_" + JsonUtils.class.getSimpleName();
  private static final Movie[] ZERO_MOVIE_ARRAY = new Movie[0];
  private static final String IMAGE_PLACEHOLDER =
      "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Image_placeholder.svg/500px-Image_placeholder.svg.png";

  private JsonUtils() {  // Non-instantiable
  }

  /**
   * Read Movie array from JSON string
   */
  @NonNull
  public static Movie[] getMovieArray(String jsonStr) throws JSONException {

    if (TextUtils.isEmpty(jsonStr)) {
      Log.e(TAG, "Empty JSON string");
      return ZERO_MOVIE_ARRAY;
    }

    final String RESULTS = "results";
    final String ID = "id";
    final String TITLE = "original_title";
    final String POSTER_PATH = "poster_path";
    final String OVERVIEW = "overview";
    final String VOTE_AVERAGE = "vote_average";
    final String RELEASE_DATE = "release_date";
    final String STATUS_MESSAGE = "status_message";

    JSONObject root = new JSONObject(jsonStr);
    if (root.has(STATUS_MESSAGE)) {
      String statusMessage = root.getString(STATUS_MESSAGE);
      Log.e(TAG, "Status message: " + statusMessage);
      return ZERO_MOVIE_ARRAY;
    }

    JSONArray jMovies = getJsonArray(root, RESULTS);
    if (jMovies == null || jMovies.length() == 0) {
      return ZERO_MOVIE_ARRAY;
    }

    Movie[] movies = new Movie[jMovies.length()];
    for (int i = 0; i < jMovies.length(); i++) {
      JSONObject jMovie = jMovies.getJSONObject(i);
      String tmp;
      // Get movie values
      int id = getInt(jMovie, ID);
      String title = getString(jMovie, TITLE);
      tmp = getString(jMovie, POSTER_PATH);
      String posterUrl = !TextUtils.isEmpty(tmp) && !tmp.equals("null") ?
          HttpUtils.buildPosterUrl(tmp).toString() : IMAGE_PLACEHOLDER;
      String overview = getString(jMovie, OVERVIEW);
      double voteAverage = getDouble(jMovie, VOTE_AVERAGE);
      int releaseDate = extractYear(getString(jMovie, RELEASE_DATE));
      // Create Movie object
      movies[i] = new Movie(id, title, posterUrl, overview, voteAverage, releaseDate);
    }
    return movies;
  }

  /**
   * Read extra movie detail from JSON string
   */
  public static Bundle getMovieDetail(String jsonStr) throws JSONException {
    if (TextUtils.isEmpty(jsonStr)) {
      Log.e(TAG, "Empty JSON string");
      return null;
    }

    final String RUNTIME = "runtime";
    final String STATUS_MESSAGE = "status_message";

    JSONObject root = new JSONObject(jsonStr);
    if (root.has(STATUS_MESSAGE)) {
      String statusMessage = root.getString(STATUS_MESSAGE);
      Log.e(TAG, "Status message: " + statusMessage);
      return null;
    }

    // Read detail and put into bundle
    Bundle movieDetail = new Bundle();
    int runtime = getInt(root, RUNTIME);
    movieDetail.putInt(Movie.RUNTIME_KEY, runtime);

    return movieDetail;
  }

  /**
   * Return total pages value from jsonStr
   */
  public static int getTotalPages(String jsonStr) throws JSONException {
    if (TextUtils.isEmpty(jsonStr)) {
      Log.e(TAG, "Empty JSON string");
      return 0;
    }

    final String TOTAL_PAGES = "total_pages";
    final String STATUS_MESSAGE = "status_message";

    JSONObject root = new JSONObject(jsonStr);
    if (root.has(STATUS_MESSAGE)) {
      String statusMessage = root.getString(STATUS_MESSAGE);
      Log.e(TAG, "Status message: " + statusMessage);
      return 0;
    }

    int totalPages = getInt(root, TOTAL_PAGES);
    return totalPages != Integer.MIN_VALUE ? totalPages : 0;
  }

  // -----------------------------------------------------------------------------------------------
  // Helpers

  /**
   * Safely get JSON array, return null otherwise
   */
  private static JSONArray getJsonArray(JSONObject parent, String field) {
    if (parent == null || TextUtils.isEmpty(field)) {
      return null;
    }
    if (!parent.has(field)) {
      Log.e(TAG, "Field '" + field + "' not found");
      return null;
    }
    try {
      return parent.getJSONArray(field);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Safely get string, return null otherwise
   */
  private static String getString(JSONObject parent, String field) {
    if (parent == null || TextUtils.isEmpty(field)) {
      return null;
    }
    if (!parent.has(field)) {
      Log.e(TAG, "Field '" + field + "' not found");
      return null;
    }
    try {
      return parent.getString(field);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Safely get integer, return Integer.MIN_VALUE otherwise
   */
  private static int getInt(JSONObject parent, String field) {
    if (parent == null || TextUtils.isEmpty(field)) {
      return Integer.MIN_VALUE;
    }
    if (!parent.has(field)) {
      Log.e(TAG, "Field '" + field + "' not found");
      return Integer.MIN_VALUE;
    }
    try {
      return parent.getInt(field);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return Integer.MIN_VALUE;
  }

  /**
   * Safely get double, return Double.MIN_VALUE otherwise
   */
  private static double getDouble(JSONObject parent, String field) {
    if (parent == null || TextUtils.isEmpty(field)) {
      return Double.MIN_VALUE;
    }
    if (!parent.has(field)) {
      Log.e(TAG, "Field '" + field + "' not found");
      return Double.MIN_VALUE;
    }
    try {
      return parent.getDouble(field);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return Double.MIN_VALUE;
  }

  /**
   * Extract year from a string of form yyyy-MM-dd
   */
  private static int extractYear(String dateStr) {
    if (TextUtils.isEmpty(dateStr)) {
      return Integer.MIN_VALUE;
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    Calendar calendar = new GregorianCalendar();
    try {
      calendar.setTime(sdf.parse(dateStr));
      return calendar.get(Calendar.YEAR);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return Integer.MIN_VALUE;
  }
}
