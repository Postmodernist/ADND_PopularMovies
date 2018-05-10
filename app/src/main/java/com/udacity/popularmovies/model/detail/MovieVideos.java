
package com.udacity.popularmovies.model.detail;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MovieVideos {

  @SerializedName("results")
  @Expose
  private List<MovieVideo> results = null;

  public List<MovieVideo> getResults() {
    return results;
  }
}
