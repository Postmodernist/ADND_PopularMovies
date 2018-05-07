package com.udacity.popularmovies.model.discover;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MoviePage {

  @SerializedName("results")
  @Expose
  private List<MovieItem> results = null;

  public List<MovieItem> getResults() {
    return results;
  }
}
