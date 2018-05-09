package com.udacity.popularmovies.model.discover;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MovieItem {

  @SerializedName("id")
  @Expose
  private Integer id;
  @SerializedName("vote_average")
  @Expose
  private Double voteAverage;
  @SerializedName("poster_path")
  @Expose
  private String posterPath;
  @SerializedName("original_title")
  @Expose
  private String originalTitle;
  @SerializedName("overview")
  @Expose
  private String overview;
  @SerializedName("release_date")
  @Expose
  private String releaseDate;

  public MovieItem(Integer id, Double voteAverage, String posterPath, String originalTitle, String overview, String releaseDate) {
    this.id = id;
    this.voteAverage = voteAverage;
    this.posterPath = posterPath;
    this.originalTitle = originalTitle;
    this.overview = overview;
    this.releaseDate = releaseDate;
  }

  public Integer getId() {
    return id;
  }

  public Double getVoteAverage() {
    return voteAverage;
  }

  public String getPosterPath() {
    return posterPath;
  }

  public String getOriginalTitle() {
    return originalTitle;
  }

  public String getOverview() {
    return overview;
  }

  public String getReleaseDate() {
    return releaseDate;
  }
}
