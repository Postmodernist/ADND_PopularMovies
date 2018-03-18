package com.udacity.popularmovies;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.udacity.popularmovies.utils.HttpUtils;
import com.udacity.popularmovies.utils.JsonUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class MoviesLoader extends AsyncTaskLoader<Movie[]> {

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
  public Movie[] loadInBackground() {
    Movie[] movies = new Movie[0];
    try {
      String response = HttpUtils.getQueryResponse(url);
      movies = JsonUtils.getMovieArray(response);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return movies;
  }
}
