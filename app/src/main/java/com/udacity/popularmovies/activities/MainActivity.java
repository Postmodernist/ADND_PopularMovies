package com.udacity.popularmovies.activities;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.fragments.DetailsFragment;
import com.udacity.popularmovies.fragments.DiscoveryFragment;
import com.udacity.popularmovies.model.discover.MovieItem;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "TAG_" + MainActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Add movies list fragment if this is a first creation
    if (savedInstanceState == null) {
      Log.d(TAG, "Starting discovery fragment");
      DiscoveryFragment fragment = new DiscoveryFragment();
      getSupportFragmentManager().beginTransaction()
          .add(R.id.fragment_container, fragment)
          .commit();
    }
  }

  /**
   * Show movie details fragment
   */
  public void showMovieDetails(int movieId, int position) {
    DetailsFragment fragment = DetailsFragment.forMovie(movieId, position);
    getSupportFragmentManager()
        .beginTransaction()
        .addToBackStack("movie")
        .replace(R.id.fragment_container, fragment, null)
        .commit();
  }

  /**
   * Check internet connection
   */
  public boolean isOnline() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = cm != null ? cm.getActiveNetworkInfo() : null;
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
  }

}
