package com.alexbaryzhikov.popularmovies.model.detail;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MovieReviews {

  @SerializedName("results")
  @Expose
  private List<MovieReview> results = null;

  public List<MovieReview> getResults() {
    return results;
  }
}
