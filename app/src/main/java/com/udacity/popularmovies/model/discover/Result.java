package com.udacity.popularmovies.model.discover;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Result implements Parcelable {

  public final static Parcelable.Creator<Result> CREATOR = new Creator<Result>() {

    public Result createFromParcel(Parcel in) {
      return new Result(in);
    }

    public Result[] newArray(int size) {
      return (new Result[size]);
    }
  };

  @SerializedName("vote_count")
  @Expose
  private int voteCount;
  @SerializedName("id")
  @Expose
  public int id;
  @SerializedName("video")
  @Expose
  private boolean video;
  @SerializedName("vote_average")
  @Expose
  private double voteAverage;
  @SerializedName("title")
  @Expose
  public String title;
  @SerializedName("popularity")
  @Expose
  private double popularity;
  @SerializedName("poster_path")
  @Expose
  private String posterPath;
  @SerializedName("original_language")
  @Expose
  private String originalLanguage;
  @SerializedName("original_title")
  @Expose
  private String originalTitle;
  @SerializedName("genre_ids")
  @Expose
  private List<Integer> genreIds = null;
  @SerializedName("backdrop_path")
  @Expose
  private String backdropPath;
  @SerializedName("adult")
  @Expose
  private boolean adult;
  @SerializedName("overview")
  @Expose
  private String overview;
  @SerializedName("release_date")
  @Expose
  private String releaseDate;

  private Result(Parcel in) {
    this.voteCount = ((int) in.readValue((int.class.getClassLoader())));
    this.id = ((int) in.readValue((int.class.getClassLoader())));
    this.video = ((boolean) in.readValue((boolean.class.getClassLoader())));
    this.voteAverage = ((double) in.readValue((double.class.getClassLoader())));
    this.title = ((String) in.readValue((String.class.getClassLoader())));
    this.popularity = ((double) in.readValue((double.class.getClassLoader())));
    this.posterPath = ((String) in.readValue((String.class.getClassLoader())));
    this.originalLanguage = ((String) in.readValue((String.class.getClassLoader())));
    this.originalTitle = ((String) in.readValue((String.class.getClassLoader())));
    in.readList(this.genreIds, (java.lang.Integer.class.getClassLoader()));
    this.backdropPath = ((String) in.readValue((String.class.getClassLoader())));
    this.adult = ((boolean) in.readValue((boolean.class.getClassLoader())));
    this.overview = ((String) in.readValue((String.class.getClassLoader())));
    this.releaseDate = ((String) in.readValue((String.class.getClassLoader())));
  }

  public Result() {
  }

  public Result(int voteCount, int id, boolean video, double voteAverage, String title,
                double popularity, String posterPath, String originalLanguage,
                String originalTitle, List<Integer> genreIds, String backdropPath,
                boolean adult, String overview, String releaseDate) {
    super();
    this.voteCount = voteCount;
    this.id = id;
    this.video = video;
    this.voteAverage = voteAverage;
    this.title = title;
    this.popularity = popularity;
    this.posterPath = posterPath;
    this.originalLanguage = originalLanguage;
    this.originalTitle = originalTitle;
    this.genreIds = genreIds;
    this.backdropPath = backdropPath;
    this.adult = adult;
    this.overview = overview;
    this.releaseDate = releaseDate;
  }

  public void writeToParcel(Parcel dest, int flags) {
    dest.writeValue(voteCount);
    dest.writeValue(id);
    dest.writeValue(video);
    dest.writeValue(voteAverage);
    dest.writeValue(title);
    dest.writeValue(popularity);
    dest.writeValue(posterPath);
    dest.writeValue(originalLanguage);
    dest.writeValue(originalTitle);
    dest.writeList(genreIds);
    dest.writeValue(backdropPath);
    dest.writeValue(adult);
    dest.writeValue(overview);
    dest.writeValue(releaseDate);
  }

  public int describeContents() {
    return 0;
  }

  public int getVoteCount() {
    return voteCount;
  }

  public int getId() {
    return id;
  }

  public boolean isVideo() {
    return video;
  }

  public double getVoteAverage() {
    return voteAverage;
  }

  public String getTitle() {
    return title;
  }

  public double getPopularity() {
    return popularity;
  }

  public String getPosterPath() {
    return posterPath;
  }

  public String getOriginalLanguage() {
    return originalLanguage;
  }

  public String getOriginalTitle() {
    return originalTitle;
  }

  public List<Integer> getGenreIds() {
    return genreIds;
  }

  public String getBackdropPath() {
    return backdropPath;
  }

  public boolean isAdult() {
    return adult;
  }

  public String getOverview() {
    return overview;
  }

  public String getReleaseDate() {
    return releaseDate;
  }
}
