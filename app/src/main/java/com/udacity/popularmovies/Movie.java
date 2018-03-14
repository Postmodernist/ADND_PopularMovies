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
  private int voteAverage;
  private long releaseDate;

  Movie(String title, String posterUrl, String overview, int voteAverage, long releaseDate) {
    this.title = title;
    this.posterUrl = posterUrl;
    this.overview = overview;
    this.voteAverage = voteAverage;
    this.releaseDate = releaseDate;
  }

  private Movie(Parcel in) {
    title = in.readString();
    posterUrl = in.readString();
    overview = in.readString();
    voteAverage = in.readInt();
    releaseDate = in.readLong();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(title);
    dest.writeString(posterUrl);
    dest.writeString(overview);
    dest.writeInt(voteAverage);
    dest.writeLong(releaseDate);
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

  int getVoteAverage() {
    return voteAverage;
  }

  long getReleaseDate() {
    return releaseDate;
  }
}
