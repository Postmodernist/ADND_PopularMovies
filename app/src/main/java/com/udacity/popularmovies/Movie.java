package com.udacity.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Movie data container
 */

class Movie implements Parcelable {

  static final String extraKey = "MOVIE";

  private String title;
  private String posterUrl;
  private String overview;
  private float voteAverage;
  private int releaseDate;
  private int runtime;

  Movie(String title, String posterUrl, String overview, float voteAverage, int releaseDate,
        int runtime) {
    this.title = title;
    this.posterUrl = posterUrl;
    this.overview = overview;
    this.voteAverage = voteAverage;
    this.releaseDate = releaseDate;
    this.runtime = runtime;
  }

  private Movie(Parcel in) {
    title = in.readString();
    posterUrl = in.readString();
    overview = in.readString();
    voteAverage = in.readFloat();
    releaseDate = in.readInt();
    runtime = in.readInt();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(title);
    dest.writeString(posterUrl);
    dest.writeString(overview);
    dest.writeFloat(voteAverage);
    dest.writeInt(releaseDate);
    dest.writeInt(runtime);
  }

  @Override
  public int describeContents() {
    return 0;
  }

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

  String getTitle() {
    return title;
  }

  String getPosterUrl() {
    return posterUrl;
  }

  String getOverview() {
    return overview;
  }

  float getVoteAverage() {
    return voteAverage;
  }

  int getReleaseDate() {
    return releaseDate;
  }

  int getRuntime() {
    return runtime;
  }
}
