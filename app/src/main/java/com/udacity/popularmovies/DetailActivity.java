package com.udacity.popularmovies;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.utils.HttpUtils;

import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Bundle> {

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
  private Movie movie;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail);
    ButterKnife.bind(this);

    Intent intent = getIntent();
    if (intent != null && intent.hasExtra(Movie.MOVIE_KEY)) {
      setPosterDimens();
      movie = intent.getParcelableExtra(Movie.MOVIE_KEY);
      populateViews(movie);
      if (!movie.isUpdated() && isOnline()) {
        getLoaderManager().initLoader(0, null, this);
      }
    } else {
      showError();
    }
  }

  // -----------------------------------------------------------------------------------------------
  // Loader

  @Override
  public Loader<Bundle> onCreateLoader(int id, Bundle args) {
    Log.i(TAG, "Loading movie details...");
    int movieId = movie.getId();
    if (movieId == Integer.MIN_VALUE) {
      Log.e(TAG, "Details loading failed: movie ID not available");
      return null;
    }
    URL url = HttpUtils.buildDetailQueryUrl(movieId);
    return new DetailLoader(this, url);
  }

  @Override
  public void onLoadFinished(Loader<Bundle> loader, Bundle data) {
    if (data == null) {
      return;
    }
    // Set extra movie details
    if (data.containsKey(Movie.RUNTIME_KEY)) {
      int runtime = data.getInt(Movie.RUNTIME_KEY);
      Log.i(TAG, "Runtime: " + runtime);
      movie.setRuntime(runtime);
    } else {
      Log.e(TAG, "Key not found: " + Movie.RUNTIME_KEY);
    }
    movie.setUpdated();
    // Populate views with updated movie
    populateViews(movie);
  }

  @Override
  public void onLoaderReset(Loader<Bundle> loader) {
  }

  // -----------------------------------------------------------------------------------------------
  // Utils

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
    final String NOT_AVAILABLE = getString(R.string.not_available);
    String sTmp;
    int iTmp;
    double dTmp;
    // Get values
    String title = !TextUtils.isEmpty(sTmp = movie.getTitle()) ? sTmp : NOT_AVAILABLE;
    String releaseDate = (iTmp = movie.getReleaseDate()) != Integer.MIN_VALUE ?
        String.valueOf(iTmp) : NOT_AVAILABLE;
    String runtime = (iTmp = movie.getRuntime()) != Integer.MIN_VALUE ?
        getString(R.string.runtime_text, iTmp) : NOT_AVAILABLE;
    String voteAverage = (dTmp = movie.getVoteAverage()) != Double.MIN_VALUE ?
        getString(R.string.vote_average_text, dTmp) : NOT_AVAILABLE;
    String overview = !TextUtils.isEmpty(sTmp = movie.getOverview()) ? sTmp : NOT_AVAILABLE;
    // Update views
    setTitle(title);
    Picasso.get().load(movie.getPosterUrl()).into(posterView);
    releaseDateView.setText(releaseDate);
    runtimeView.setText(runtime);
    voteAverageView.setText(voteAverage);
    overviewView.setText(overview);
  }

  /**
   * Check internet connection
   */
  private boolean isOnline() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = cm != null ? cm.getActiveNetworkInfo() : null;
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
  }

  /**
   * Show error message
   */
  private void showError() {
    movieDetailLayout.setVisibility(View.INVISIBLE);
    errorView.setVisibility(View.VISIBLE);
  }
}
