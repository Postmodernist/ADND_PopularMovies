package com.udacity.popularmovies.api;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.udacity.popularmovies.BuildConfig;

import java.util.Map;

public final class ApiUtils {

  // Sorting modes
  public static final String SORT_BY_POPULARITY = "popularity.desc";
  public static final String SORT_BY_RATING = "vote_average.desc";

  // TheMovieDB API base URL
  public static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";
  public static final String DISCOVER_PATH = "discover/movie";
  public static final String DETAIL_PATH = "movie/";

  // Poster URL
  private static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
  private static final String POSTER_FILE_SIZE = "w342";
  private static final Uri POSTER_URI = Uri.parse(POSTER_BASE_URL + POSTER_FILE_SIZE);
  private static final String IMAGE_PLACEHOLDER = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Image_placeholder.svg/500px-Image_placeholder.svg.png";

  // Query parameters
  private static final String API_KEY_PARAM = "api_key";
  private static final String LANGUAGE_PARAM = "language";
  private static final String SORT_PARAM = "sort_by";
  private static final String ADULT_PARAM = "include_adult";
  private static final String VIDEO_PARAM = "include_video";
  private static final String PAGE_PARAM = "page";
  private static final String VOTE_COUNT_MIN_PARAM = "vote_count.gte";

  // Default query parameters values
  private static final String api_key = BuildConfig.MY_MOVIE_DB_API_KEY;
  private static final String language = "en-US";
  private static final String adult = "false";
  private static final String video = "false";
  private static final String vote_count_min = "100";

  // Youtube
  private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
  private static final Uri YOUTUBE_URI = Uri.parse(YOUTUBE_BASE_URL);
  private static final String KEY_PARAM = "v";

  private ApiUtils() {
    // Prevents instantiation
  }

  /**
   * Build query options for discovery query
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

  /** Build query options for movie detail query */
  public static Map<String, String> detailQueryOptions() {
    Map<String, String> options = new ArrayMap<>(2);
    options.put(API_KEY_PARAM, api_key);
    options.put(LANGUAGE_PARAM, language);
    return options;
  }

  /** Build options for reviews query */
  public static Map<String, String> reviewsQueryOptions() {
    Map<String, String> options = new ArrayMap<>(3);
    options.put(API_KEY_PARAM, api_key);
    options.put(LANGUAGE_PARAM, language);
    options.put(PAGE_PARAM, "1");
    return options;
  }

  /** Build poster URL */
  @NonNull
  public static String posterUrl(String posterPath) {
    return posterPath != null ?
        POSTER_URI.buildUpon()
            .appendEncodedPath(posterPath.substring(1))
            .build()
            .toString()
        : IMAGE_PLACEHOLDER;
  }

  /** Build YouTube URI */
  public static Uri youtubeUri(String key) {
    return YOUTUBE_URI.buildUpon()
        .appendQueryParameter(KEY_PARAM, key)
        .build();
  }

}
