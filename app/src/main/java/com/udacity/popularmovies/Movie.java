package com.udacity.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Movie data container
 */

public class Movie implements Parcelable {

  public static final String MOVIE_KEY = "MOVIE";
  public static final String TOTAL_PAGES_KEY = "TOTAL_PAGES";
  public static final String RUNTIME_KEY = "RUNTIME";
  public static final Creator<Movie> CREATOR = new Creator<Movie>() {
    @Override
    public Movie createFromParcel(Parcel in) {
      return new Movie(in);
    }

    @Override
    public Movie[] newArray(int size) {
      return new Movie[size];
    }
  };

  private int id;
  private String title;
  private String posterUrl;
  private String overview;
  private double voteAverage;
  private int releaseDate;
  private int runtime = Integer.MIN_VALUE;
  private boolean updated = false;

  public Movie(int id, String title, String posterUrl, String overview, double voteAverage, int releaseDate) {
    this.id = id;
    this.title = title;
    this.posterUrl = posterUrl;
    this.overview = overview;
    this.voteAverage = voteAverage;
    this.releaseDate = releaseDate;
  }

  private Movie(Parcel in) {
    id = in.readInt();
    title = in.readString();
    posterUrl = in.readString();
    overview = in.readString();
    voteAverage = in.readDouble();
    releaseDate = in.readInt();
    runtime = in.readInt();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(id);
    dest.writeString(title);
    dest.writeString(posterUrl);
    dest.writeString(overview);
    dest.writeDouble(voteAverage);
    dest.writeInt(releaseDate);
    dest.writeInt(runtime);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public int getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getPosterUrl() {
    return posterUrl;
  }

  public String getOverview() {
    return overview;
  }

  public double getVoteAverage() {
    return voteAverage;
  }

  public int getReleaseDate() {
    return releaseDate;
  }

  public int getRuntime() {
    return runtime;
  }

  public void setRuntime(int runtime) {
    this.runtime = runtime;
  }

  public boolean isUpdated() {
    return updated;
  }

  public void setUpdated() {
    this.updated = true;
  }
}
