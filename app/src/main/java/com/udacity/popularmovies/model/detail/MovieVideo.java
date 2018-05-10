package com.udacity.popularmovies.model.detail;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MovieVideo {

  @SerializedName("key")
  @Expose
  private String key;
  @SerializedName("name")
  @Expose
  private String name;
  @SerializedName("site")
  @Expose
  private String site;
  @SerializedName("type")
  @Expose
  private String type;

  public String getKey() {
    return key;
  }

  public String getName() {
    return name;
  }

  public String getSite() {
    return site;
  }

  public String getType() {
    return type;
  }
}
