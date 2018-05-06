package com.udacity.popularmovies.fragments;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.MoviesApplication;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.activities.MainActivity;
import com.udacity.popularmovies.database.MovieContract.MovieEntry;
import com.udacity.popularmovies.di.components.DaggerDetailFragmentComponent;
import com.udacity.popularmovies.model.detail.MovieDetail;
import com.udacity.popularmovies.utils.ApiUtils;
import com.udacity.popularmovies.viewmodels.MoviesViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailFragment extends Fragment {

  private static final String TAG = "TAG_" + DetailFragment.class.getSimpleName();
  private static final String KEY_MOVIE_ID = "movie_id";
  private static final String KEY_POSITION = "position";

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
  @BindView(R.id.b_star)
  Button starButton;

  @Inject
  MoviesViewModel viewModel;

  private MainActivity mainActivity;

  /**
   * Create details fragment for specific movie
   */
  public static DetailFragment forMovie(int movieId, int position) {
    DetailFragment fragment = new DetailFragment();
    Bundle args = new Bundle();
    args.putInt(KEY_MOVIE_ID, movieId);
    args.putInt(KEY_POSITION, position);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onAttach(Context context) {
    setupDagger();
    super.onAttach(context);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_details, container, false);
    ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mainActivity = (MainActivity) Objects.requireNonNull(getActivity());
    mainActivity.showBackInActionBar();
    Bundle args = getArguments();
    if (args != null && args.containsKey(KEY_MOVIE_ID) && args.containsKey(KEY_POSITION)) {
      final int movieId = args.getInt(KEY_MOVIE_ID);
      final int position = args.getInt(KEY_POSITION);
      setupPosterDimens();
      setupViewModel(movieId, position);
    } else {
      showError();
    }
  }

  private void setupDagger() {
    DaggerDetailFragmentComponent.builder()
        .appComponent(MoviesApplication.getAppComponent(Objects.requireNonNull(getContext())))
        .fragment(this)
        .build()
        .inject(this);
  }

  private void setupViewModel(int movieId, int position) {
    viewModel.getMovieDetail(movieId, position).observe(this, this::populateViews);
  }

  /**
   * If movie is not starred set button to "Star", otherwise set button to "Unstar"
   */
  private void setupStarButton(final MovieDetail movie) {
    if (movie == null) {
      throw new NullPointerException("Movie object is null");
    }

    // Find out if this movie is already starred

    Cursor cursor = mainActivity.getContentResolver().query(
        MovieEntry.CONTENT_URI,
        null,
        "movie_id=?",
        new String[]{String.valueOf(movie.getId())},
        null);
    if (cursor == null) {
      throw new SQLException("Failed to read database");
    }

    setupStarButtonCallback(movie, cursor);
  }

  private void setupStarButtonCallback(MovieDetail movie, Cursor cursor) {

    // Setup button accordingly

    if (cursor.getCount() == 0) {

      // *** Unstarred ***

      starButton.setText(R.string.star_button_text);
      starButton.setOnClickListener(v -> {
        // Add to favorites
        // WARNING: Assumes the movie object is legit
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieEntry.COLUMN_TITLE, movie.getOriginalTitle());
        contentValues.put(MovieEntry.COLUMN_MOVIE_ID, movie.getId());
        Uri uri = mainActivity.getContentResolver().insert(MovieEntry.CONTENT_URI, contentValues);
        if (uri != null) {
          Log.d(TAG, "Starred: " + uri);
        }
        // Make star button great again
        setupStarButton(movie);
      });

    } else {

      // *** Starred ***

      cursor.moveToNext();
      int id = cursor.getInt(cursor.getColumnIndex(MovieEntry._ID));
      starButton.setText(R.string.unstar_button_text);
      starButton.setOnClickListener(v -> {
        // Remove from favorites
        int rowsRemoved = mainActivity.getContentResolver().delete(
            ContentUris.withAppendedId(MovieEntry.CONTENT_URI, id),
            null,
            null);
        Log.d(TAG, "Rows removed: " + rowsRemoved);
        // Make star button great again
        setupStarButton(movie);
      });
    }

    cursor.close();
  }

  /**
   * Set poster width and height
   */
  private void setupPosterDimens() {
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
    posterView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
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
    final TypedArray styledAttributes = Objects.requireNonNull(getActivity()).getTheme().obtainStyledAttributes(attrs);
    int appBarHeight = (int) styledAttributes.getDimension(0, 0);
    styledAttributes.recycle();
    return statusBarHeight + appBarHeight;
  }

  /**
   * Populate views with movie data
   */
  private void populateViews(MovieDetail movie) {
    if (movie == null) {
      return;
    }

    setupStarButton(movie);

    final String NOT_AVAILABLE = getString(R.string.not_available);
    String sTmp;
    Integer iTmp;
    Double dTmp;

    // Get values
    final String posterPath = movie.getPosterPath();

    final String title = !TextUtils.isEmpty(sTmp = movie.getOriginalTitle()) ?
        sTmp : NOT_AVAILABLE;

    final String releaseDate = (iTmp = extractYear(movie.getReleaseDate())) != Integer.MIN_VALUE ?
        String.valueOf(iTmp) : NOT_AVAILABLE;

    final String runtime = ((iTmp = movie.getRuntime()) != null) && (iTmp != Integer.MIN_VALUE) ?
        getString(R.string.runtime_text, iTmp) : NOT_AVAILABLE;

    final String voteAverage = (dTmp = movie.getVoteAverage()) != null ?
        getString(R.string.vote_average_text, dTmp) : NOT_AVAILABLE;

    final String overview = !TextUtils.isEmpty(sTmp = movie.getOverview()) ? sTmp : NOT_AVAILABLE;

    // Update views
    Picasso.get().load(ApiUtils.posterUrl(posterPath)).into(posterView);
    releaseDateView.setText(releaseDate);
    runtimeView.setText(runtime);
    voteAverageView.setText(voteAverage);
    overviewView.setText(overview);

    // Change title
    mainActivity.setTitle(title);
  }

  /**
   * Extract year from a string of form yyyy-MM-dd
   */
  private int extractYear(String dateStr) {
    if (TextUtils.isEmpty(dateStr)) {
      return Integer.MIN_VALUE;
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    Calendar calendar = new GregorianCalendar();
    try {
      calendar.setTime(sdf.parse(dateStr));
      return calendar.get(Calendar.YEAR);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return Integer.MIN_VALUE;
  }

  /**
   * Show error message
   */
  private void showError() {
    movieDetailLayout.setVisibility(View.INVISIBLE);
    errorView.setVisibility(View.VISIBLE);
  }
}
