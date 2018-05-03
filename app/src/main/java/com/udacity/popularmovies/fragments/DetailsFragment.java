package com.udacity.popularmovies.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.activities.MainActivity;
import com.udacity.popularmovies.api.ApiUtils;
import com.udacity.popularmovies.model.detail.MovieDetail;
import com.udacity.popularmovies.repositories.MoviesRepository;
import com.udacity.popularmovies.viewmodels.MoviesViewModel;
import com.udacity.popularmovies.viewmodels.MoviesViewModelFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsFragment extends Fragment {

  private static final String KEY_MOVIE_ID = "movie_id";

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

  private MainActivity mainActivity;

  /**
   * Create details fragment for specific movie
   */
  public static DetailsFragment forMovie(int movieId) {
    DetailsFragment fragment = new DetailsFragment();
    Bundle args = new Bundle();
    args.putInt(KEY_MOVIE_ID, movieId);
    fragment.setArguments(args);
    return fragment;
  }

  /**
   * Extract year from a string of form yyyy-MM-dd
   */
  private static int extractYear(String dateStr) {
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
    Bundle args = Objects.requireNonNull(getArguments());
    final int movieId = args.getInt(KEY_MOVIE_ID);

    if (mainActivity.isOnline()) {
      setPosterDimens();
      setupViewModel(movieId);
    } else {
      showError();
    }
  }

  private void setupViewModel(int movieId) {
    MoviesRepository repository = MoviesRepository.getInstance();
    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);

    MoviesViewModel viewModel = ViewModelProviders
        .of(mainActivity, new MoviesViewModelFactory(repository, sharedPrefs))
        .get(MoviesViewModel.class);
    viewModel.getMovieDetail(movieId).observe(this, this::populateViews);
  }

  // -----------------------------------------------------------------------------------------------

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

    final String NOT_AVAILABLE = getString(R.string.not_available);
    String sTmp;
    int iTmp;
    double dTmp;

    // Get values
    final String posterPath = movie.getPosterPath();

    final String title = !TextUtils.isEmpty(sTmp = movie.getOriginalTitle()) ?
        sTmp : NOT_AVAILABLE;

    final String releaseDate = (iTmp = extractYear(movie.getReleaseDate())) != Integer.MIN_VALUE ?
        String.valueOf(iTmp) : NOT_AVAILABLE;

    final String runtime = (iTmp = movie.getRuntime()) != Integer.MIN_VALUE ?
        getString(R.string.runtime_text, iTmp) : NOT_AVAILABLE;

    final String voteAverage = (dTmp = movie.getVoteAverage()) != Double.MIN_VALUE ?
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
   * Show error message
   */
  private void showError() {
    movieDetailLayout.setVisibility(View.INVISIBLE);
    errorView.setVisibility(View.VISIBLE);
  }
}
