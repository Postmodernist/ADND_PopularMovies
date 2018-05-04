package com.udacity.popularmovies.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static android.content.Context.CONNECTIVITY_SERVICE;

public final class ConnectionUtils {

  private ConnectionUtils() {
    // Prevents instantiation
  }

  /**
   * Check internet connection
   */
  public static boolean isOnline(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = cm != null ? cm.getActiveNetworkInfo() : null;
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
  }
}
