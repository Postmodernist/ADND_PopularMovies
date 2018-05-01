package com.udacity.popularmovies.model.discover;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MoviesPage implements Parcelable {

  public final static Parcelable.Creator<MoviesPage> CREATOR = new Creator<MoviesPage>() {

    public MoviesPage createFromParcel(Parcel in) {
      return new MoviesPage(in);
    }

    public MoviesPage[] newArray(int size) {
      return (new MoviesPage[size]);
    }
  };

  @SerializedName("page")
  @Expose
  private int page;
  @SerializedName("total_results")
  @Expose
  private int totalResults;
  @SerializedName("total_pages")
  @Expose
  private int totalPages;
  @SerializedName("results")
  @Expose
  private List<Result> results = null;

  private MoviesPage(Parcel in) {
    this.page = ((int) in.readValue((int.class.getClassLoader())));
    this.totalResults = ((int) in.readValue((int.class.getClassLoader())));
    this.totalPages = ((int) in.readValue((int.class.getClassLoader())));
    in.readList(this.results, (Result.class.getClassLoader()));
  }

  public MoviesPage() {
  }

  public MoviesPage(int page, int totalResults, int totalPages, List<Result> results) {
    super();
    this.page = page;
    this.totalResults = totalResults;
    this.totalPages = totalPages;
    this.results = results;
  }

  public void writeToParcel(Parcel dest, int flags) {
    dest.writeValue(page);
    dest.writeValue(totalResults);
    dest.writeValue(totalPages);
    dest.writeList(results);
  }

  public int describeContents() {
    return 0;
  }

  public int getPage() {
    return page;
  }

  public int getTotalResults() {
    return totalResults;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public List<Result> getResults() {
    return results;
  }
}
