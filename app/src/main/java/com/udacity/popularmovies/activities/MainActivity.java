package com.udacity.popularmovies.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.fragments.DetailFragment;
import com.udacity.popularmovies.fragments.DiscoveryFragment;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

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

  @Override
  public boolean onSupportNavigateUp() {
    getSupportFragmentManager().popBackStack();
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(false);
    }
    return true;
  }

  public void showBackInActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      boolean enabled = getSupportFragmentManager().getBackStackEntryCount() > 0;
      actionBar.setDisplayHomeAsUpEnabled(enabled);
    }
  }

  public void showMovieDetails(int movieId, int position) {
    DetailFragment fragment = DetailFragment.forMovie(movieId, position);
    getSupportFragmentManager()
        .beginTransaction()
        .addToBackStack("movie")
        .replace(R.id.fragment_container, fragment, null)
        .commit();
  }
}
