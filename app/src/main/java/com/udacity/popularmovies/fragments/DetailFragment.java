package com.udacity.popularmovies.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.MovieApplication;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.activities.MainActivity;
import com.udacity.popularmovies.di.components.DaggerDetailFragmentComponent;
import com.udacity.popularmovies.model.detail.MovieDetail;
import com.udacity.popularmovies.model.detail.MovieVideo;
import com.udacity.popularmovies.utils.ApiUtils;
import com.udacity.popularmovies.viewmodels.MovieViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailFragment extends Fragment {

  private static final String TAG = "TAG_" + DetailFragment.class.getSimpleName();
  private static final String KEY_MOVIE_ID = "movie_id";
  private static final String KEY_POSITION = "position";

  @BindView(R.id.tv_error) TextView errorView;
  @BindView(R.id.movie_detail_layout) RelativeLayout movieDetailLayout;
  @BindView(R.id.iv_poster) ImageView posterView;
  @BindView(R.id.tv_release_date) TextView releaseDateView;
  @BindView(R.id.tv_runtime) TextView runtimeView;
  @BindView(R.id.tv_vote_average) TextView voteAverageView;
  @BindView(R.id.tv_overview) TextView overviewView;
  @BindView(R.id.b_star) Button starButton;
  @BindView(R.id.trailers_label) TextView trailersLabel;
  @BindView(R.id.trailers) LinearLayout trailersLayout;

  @Inject MovieViewModel viewModel;

  private MainActivity mainActivity;
  private LiveData<MovieDetail> movieDetailLiveData;
  private Observer<MovieDetail> starObserver;

  /** Create details fragment for specific movie */
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
        .appComponent(MovieApplication.getAppComponent(Objects.requireNonNull(getContext())))
        .fragment(this)
        .build()
        .inject(this);
  }

  private void setupViewModel(int movieId, int position) {
    movieDetailLiveData = viewModel.getMovieDetail(movieId, position);
    starObserver = this::setupStarButton;
    movieDetailLiveData.observe(this, starObserver);
    movieDetailLiveData.observe(this, this::populateViews);
    viewModel.getMovieTrailers(movieId).observe(this, this::populateTrailers);
  }

  /** If movie is not starred set button to "Star", otherwise set button to "Unstar" */
  private void setupStarButton(MovieDetail movie) {
    if (movie == null) {
      Log.e(TAG, "Movie object is null");
      return;
    }

    // This function only needs to be called once, so unsubscribe
    movieDetailLiveData.removeObserver(starObserver);
    disableStarButton();

    // Forward the rest to ViewModel
    viewModel.initStarButton(movie, this::disableStarButton, this::enableStarButton);
  }

  private void enableStarButton(@DrawableRes int icon, @StringRes int text, View.OnClickListener onClickListener) {
    final Drawable star = getResources().getDrawable(icon);
    starButton.setCompoundDrawablesWithIntrinsicBounds(star, null, null, null);
    starButton.setText(text);
    starButton.setTextColor(getResources().getColor(R.color.colorText));
    starButton.setOnClickListener(onClickListener);
  }

  private void disableStarButton() {
    starButton.setTextColor(getResources().getColor(R.color.colorTextDisabled));
    starButton.setOnClickListener(null);
  }

  private void populateViews(MovieDetail movie) {
    if (movie == null) {
      Log.e(TAG, "Movie object is null");
      return;
    }

    final String NOT_AVAILABLE = getString(R.string.not_available);
    String sTmp;
    Integer iTmp;
    Double dTmp;

    // Update views
    voteAverageView.setText((dTmp = movie.getVoteAverage()) != null ?
        getString(R.string.vote_average_text, dTmp) : NOT_AVAILABLE);
    Picasso.get().load(ApiUtils.posterUrl(movie.getPosterPath())).into(posterView);
    mainActivity.setTitle(!TextUtils.isEmpty(sTmp = movie.getOriginalTitle()) ? sTmp : NOT_AVAILABLE);
    overviewView.setText(!TextUtils.isEmpty(sTmp = movie.getOverview()) ? sTmp : NOT_AVAILABLE);
    releaseDateView.setText(
        (iTmp = extractYear(movie.getReleaseDate())) != Integer.MIN_VALUE ? String.valueOf(iTmp) : NOT_AVAILABLE);
    runtimeView.setText(((iTmp = movie.getRuntime()) != null) && (iTmp != Integer.MIN_VALUE) ?
        getString(R.string.runtime_text, iTmp) : NOT_AVAILABLE);
  }

  private void populateTrailers(List<MovieVideo> trailers) {
    LayoutInflater inflater = LayoutInflater.from(getContext());

    if (trailers.size() == 0) {
      View noAvail = inflater.inflate(R.layout.trailer_list_item_no_avail, trailersLayout, false);
      trailersLayout.addView(noAvail);
      return;
    }

    for (MovieVideo trailer : trailers) {
      View trailerView = inflater.inflate(R.layout.trailer_list_item, trailersLayout, false);
      TextView title = trailerView.findViewById(R.id.tv_trailer_title);
      title.setText(trailer.getName());
      trailersLayout.addView(trailerView);
    }
  }

  /** Extract year from a string of form yyyy-MM-dd */
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

  /** Set poster width and height */
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

    final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
    int margin = (int) (getResources().getDimension(R.dimen.application_margin) + 0.5f);
    layoutParams.setMargins(0, 0, margin, 0);
    posterView.setLayoutParams(layoutParams);
  }

  /** Get status bar height + app bar height */
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

  /** Show error message */
  private void showError() {
    movieDetailLayout.setVisibility(View.INVISIBLE);
    errorView.setVisibility(View.VISIBLE);
  }
}
