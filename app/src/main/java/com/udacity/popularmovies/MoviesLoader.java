package com.udacity.popularmovies;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;

import com.udacity.popularmovies.utils.HttpUtils;
import com.udacity.popularmovies.utils.JsonUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class MoviesLoader extends AsyncTaskLoader<Bundle> {

  private final URL url;

  MoviesLoader(Context context, URL url) {
    super(context);
    this.url = url;
  }

  @Override
  protected void onStartLoading() {
    super.onStartLoading();
    forceLoad();
  }

  @Override
  public Bundle loadInBackground() {
    Bundle bundle = new Bundle();
    Movie[] movies = new Movie[0];
    int totalPages = 0;
    try {
      String response = HttpUtils.getQueryResponse(url);
      movies = JsonUtils.getMovieArray(response);
      totalPages = JsonUtils.getTotalPages(response);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    }
    bundle.putParcelableArray(Movie.MOVIE_KEY, movies);
    bundle.putInt(Movie.TOTAL_PAGES_KEY, totalPages);
    return bundle;
  }
}
