package com.udacity.popularmovies.api;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.udacity.popularmovies.BuildConfig;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

public final class HttpUtils {

  // Sorting modes
  public static final String SORT_BY_POPULARITY = "popularity.desc";
  public static final String SORT_BY_RATING = "vote_average.desc";

  // TheMovieDB API base URL
  public static final String BASE_URL = "https://api.themoviedb.org/3/";
  private static final String TAG = "TAG_" + HttpUtils.class.getSimpleName();
  private static final Uri BASE_URI = Uri.parse(BASE_URL);

  // Poster URL
  private static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
  private static final String POSTER_FILE_SIZE = "w342";
  private static final Uri POSTER_URI = Uri.parse(POSTER_BASE_URL + POSTER_FILE_SIZE);
  private static final String IMAGE_PLACEHOLDER = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Image_placeholder.svg/500px-Image_placeholder.svg.png";

  // Request relative paths
  public static final String DISCOVER_PATH = "discover/movie";
  private static final String DETAIL_PATH = "movie";

  // Query parameter names
  private static final String API_KEY_PARAM = "api_key";
  private static final String LANGUAGE_PARAM = "language";
  private static final String SORT_PARAM = "sort_by";
  private static final String ADULT_PARAM = "include_adult";
  private static final String VIDEO_PARAM = "include_video";
  private static final String PAGE_PARAM = "page";
  private static final String VOTE_COUNT_MIN_PARAM = "vote_count.gte";

  // Default query parameter values
  private static final String api_key = BuildConfig.MY_MOVIE_DB_API_KEY;
  private static final String language = "en-US";
  private static final String adult = "false";
  private static final String video = "false";
  private static final String vote_count_min = "100";

  // HTTP connection parameters
  private static final int READ_TIMEOUT = 10000;  // milliseconds
  private static final int CONNECT_TIMEOUT = 15000;  // milliseconds
  private static final String REQUEST_METHOD = "GET";

  private HttpUtils() {  // Prevents instantiation
  }

  /**
   * Build query options for DiscoverApi
   *
   * @param sortBy Sort mode, e.g. "popularity.desc" or "vote_average.desc"
   * @param page   Page number
   */
  public static Map<String, String> discoveryQueryOptions(String sortBy, int page) {
    Map<String, String> options = new ArrayMap<>(7);
    options.put(API_KEY_PARAM, api_key);
    options.put(LANGUAGE_PARAM, language);
    options.put(SORT_PARAM, sortBy);
    options.put(ADULT_PARAM, adult);
    options.put(VIDEO_PARAM, video);
    options.put(PAGE_PARAM, String.valueOf(page));
    if (sortBy.equals(SORT_BY_RATING)) {
      options.put(VOTE_COUNT_MIN_PARAM, vote_count_min);
    }
    return options;
  }

  /**
   * Build detail query URL
   *
   * @param movieId Movie ID (from the discover response)
   */
  public static URL buildDetailQueryUrl(int movieId) {
    Uri uri = BASE_URI.buildUpon()
        .appendEncodedPath(DETAIL_PATH)
        .appendEncodedPath(String.valueOf(movieId))
        .appendQueryParameter(API_KEY_PARAM, api_key)
        .appendQueryParameter(LANGUAGE_PARAM, language)
        .build();
    URL url = null;
    try {
      url = new URL(uri.toString());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    Log.i(TAG, "Detail URI: " + url);
    return url;
  }

  /**
   * Build poster URL
   */
  @NonNull
  public static String posterUrl(String posterPath) {
    return posterPath != null ?
        POSTER_URI.buildUpon()
            .appendEncodedPath(posterPath.substring(1))
            .build()
            .toString()
        : IMAGE_PLACEHOLDER;
  }

  /**
   * Make HTTP query and return the response
   */
  @Nullable
  public static String getQueryResponse(URL url) throws IOException {
    if (url == null) {
      return null;
    }
    HttpURLConnection connection = null;
    String response = null;
    try {
      // Establish a HTTP connection
      connection = (HttpURLConnection) url.openConnection();
      connection.setReadTimeout(READ_TIMEOUT);
      connection.setConnectTimeout(CONNECT_TIMEOUT);
      connection.setRequestMethod(REQUEST_METHOD);
      connection.connect();
      // Check if the request was successful
      if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
        // Convert input stream to string
        InputStream in = new BufferedInputStream(connection.getInputStream());
        Scanner scanner = new Scanner(in);
        scanner.useDelimiter("\\A");
        if (scanner.hasNext()) {
          response = scanner.next();
        }
      } else {
        Log.e(TAG, "Error response code: " + connection.getResponseCode());
      }
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
    return response;
  }
}
