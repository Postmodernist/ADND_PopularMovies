package com.udacity.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder> {

  private Movie[] moviesData;
  private MoviesOnClickHandler clickHandler;
  private ViewGroup.LayoutParams itemLayoutParams;

  /**
   * MoviesAdapter constructor
   *
   * @param clickHandler The on-click handler for this adapter.
   * @param itemHeight   The height of the items inflated by the adapter.
   */
  MoviesAdapter(MoviesOnClickHandler clickHandler, int itemHeight) {
    this.clickHandler = clickHandler;
    itemLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
  }

  @NonNull
  @Override
  public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);
    View view = inflater.inflate(R.layout.movie_grid_item, parent, false);
    view.findViewById(R.id.iv_poster).setLayoutParams(itemLayoutParams);  // set row height
    return new MovieViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
    ImageView posterView = holder.getPosterView();
    Picasso.get().load(moviesData[position].getPosterUrl()).into(posterView);
  }

  @Override
  public int getItemCount() {
    if (moviesData == null) {
      return 0;
    }
    return moviesData.length;
  }

  /**
   * Set movies data and refresh adapter
   */
  public void resetMoviesData() {
    this.moviesData = null;
    notifyDataSetChanged();
  }

  /**
   * Append new movies data and refresh adapter
   */
  public void appendMoviesData(Movie[] newMoviesData) {
    if (moviesData == null) {
      moviesData = newMoviesData;
    } else {
      Movie[] tmp = new Movie[moviesData.length + newMoviesData.length];
      System.arraycopy(moviesData, 0, tmp, 0, moviesData.length);
      System.arraycopy(newMoviesData, 0, tmp, moviesData.length, newMoviesData.length);
      moviesData = tmp;
    }
    notifyDataSetChanged();
  }

  /**
   * The interface that receives onClick messages
   */
  public interface MoviesOnClickHandler {
    void onClick(Movie movie);
  }

  /**
   * Cache of the movie grid items
   */
  class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView posterView;

    MovieViewHolder(View itemView) {
      super(itemView);
      posterView = itemView.findViewById(R.id.iv_poster);
      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      clickHandler.onClick(moviesData[getAdapterPosition()]);
    }

    ImageView getPosterView() {
      return posterView;
    }
  }
}
