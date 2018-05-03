package com.udacity.popularmovies.model.detail;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MovieDetail {

  @SerializedName("id")
  @Expose
  private Integer id;
  @SerializedName("original_title")
  @Expose
  private String originalTitle;
  @SerializedName("overview")
  @Expose
  private String overview;
  @SerializedName("poster_path")
  @Expose
  private String posterPath;
  @SerializedName("release_date")
  @Expose
  private String releaseDate;
  @SerializedName("runtime")
  @Expose
  private Integer runtime;
  @SerializedName("vote_average")
  @Expose
  private Double voteAverage;

  public Integer getId() {
    return id;
  }

  public String getOriginalTitle() {
    return originalTitle;
  }

  public String getOverview() {
    return overview;
  }

  public String getPosterPath() {
    return posterPath;
  }

  public String getReleaseDate() {
    return releaseDate;
  }

  public Integer getRuntime() {
    return runtime;
  }

  public Double getVoteAverage() {
    return voteAverage;
  }
}
