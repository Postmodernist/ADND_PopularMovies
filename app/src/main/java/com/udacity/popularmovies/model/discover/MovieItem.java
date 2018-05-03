package com.udacity.popularmovies.model.discover;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MovieItem {

  @SerializedName("id")
  @Expose
  private Integer id;
  @SerializedName("poster_path")
  @Expose
  private String posterPath;
  @SerializedName("original_title")
  @Expose
  private String originalTitle;

  public Integer getId() {
    return id;
  }

  public String getPosterPath() {
    return posterPath;
  }

  public String getOriginalTitle() {
    return originalTitle;
  }
}
