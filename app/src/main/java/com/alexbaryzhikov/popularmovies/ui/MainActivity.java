package com.alexbaryzhikov.popularmovies.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.alexbaryzhikov.popularmovies.R;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Add movies list fragment if this is a first creation
    if (savedInstanceState == null) {
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
        .addToBackStack("DetailFragment")
        .replace(R.id.fragment_container, fragment, null)
        .commit();
  }
}
