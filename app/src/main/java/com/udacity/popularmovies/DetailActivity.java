package com.udacity.popularmovies;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

  private static final String TAG = DetailActivity.class.getSimpleName();

  @BindView(R.id.tv_error)
  TextView errorView;
  @BindView(R.id.movie_detail_layout)
  RelativeLayout movieDetailLayout;
  @BindView(R.id.iv_poster)
  ImageView posterView;
  @BindView(R.id.tv_release_date)
  TextView releaseDateView;
  @BindView(R.id.tv_runtime)
  TextView runtimeView;
  @BindView(R.id.tv_vote_average)
  TextView voteAverageView;
  @BindView(R.id.tv_overview)
  TextView overviewView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail);
    ButterKnife.bind(this);

    Intent intent = getIntent();
    if (intent != null && intent.hasExtra(Movie.extraKey)) {
      setPosterDimens();
      populateViews((Movie) intent.getParcelableExtra(Movie.extraKey));
    } else {
      showError();
    }
  }

  /**
   * Return RecyclerView grid row height
   */
  private void setPosterDimens() {
    int displayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    int displayHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    int widthRatio = getResources().getInteger(R.integer.poster_width_ratio);
    int heightRatio = getResources().getInteger(R.integer.poster_height_ratio);
    int width;
    int height;
    if (displayWidth < displayHeight) {
      // Portrait
      width = displayWidth / 2;
      height = width * heightRatio / widthRatio;
    } else {
      // Landscape
      int contentViewTop = getStatusAndAppBarHeight();
      int margins = (int) (getResources().getDimension(R.dimen.application_margin) * 2 + 0.5f);
      height = displayHeight - contentViewTop - margins;
      width = height * widthRatio / heightRatio;
    }
    posterView.setLayoutParams(new LayoutParams(width, height));
  }

  /**
   * Get status bar height + app bar height
   */
  private int getStatusAndAppBarHeight() {
    // Status bar height
    int statusBarHeight = 0;
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen",
        "android");
    if (resourceId > 0) {
      statusBarHeight = getResources().getDimensionPixelSize(resourceId);
    }
    // App bar height
    int[] attrs = new int[]{android.R.attr.actionBarSize};
    final TypedArray styledAttributes = getTheme().obtainStyledAttributes(attrs);
    int appBarHeight = (int) styledAttributes.getDimension(0, 0);
    styledAttributes.recycle();
    return statusBarHeight + appBarHeight;
  }

  /**
   * Populate views with movie data
   */
  private void populateViews(Movie movie) {
    setTitle(movie.getTitle());
    Log.i(TAG, movie.getPosterUrl());
    Picasso.get().load(movie.getPosterUrl()).into(posterView);
    releaseDateView.setText(String.valueOf(movie.getReleaseDate()));
    runtimeView.setText(getString(R.string.runtime_text, movie.getRuntime()));
    voteAverageView.setText(getString(R.string.vote_average_text, movie.getVoteAverage()));
    overviewView.setText(movie.getOverview());
  }

  /**
   * Show error message
   */
  private void showError() {
    movieDetailLayout.setVisibility(View.INVISIBLE);
    errorView.setVisibility(View.VISIBLE);
  }
}
