package com.udacity.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DetailActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail);

    Intent intent = getIntent();
    if (intent != null && intent.hasExtra(Movie.extraKey)) {
      Movie movie = intent.getParcelableExtra(Movie.extraKey);
      setTitle(movie.getTitle());
    }
  }
}
