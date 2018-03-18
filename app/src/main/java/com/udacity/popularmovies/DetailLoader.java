package com.udacity.popularmovies;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;

import com.udacity.popularmovies.utils.HttpUtils;
import com.udacity.popularmovies.utils.JsonUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class DetailLoader extends AsyncTaskLoader<Bundle> {

  private final URL url;

  DetailLoader(Context context, URL url) {
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
    Bundle movieDetail = null;
    try {
      String response = HttpUtils.getQueryResponse(url);
      movieDetail = JsonUtils.getMovieDetail(response);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return movieDetail;
  }
}
