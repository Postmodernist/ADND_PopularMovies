/*
 * Original solution:
 * https://gist.github.com/nesquena/d09dc68ff07e845cc622
 */
package com.udacity.popularmovies.utils;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

  // The minimum amount of items to have below current scroll position before loading more
  private static final int REMAINING_ROWS_THRESHOLD = 5;
  private int remainingItemsThreshold;

  private GridLayoutManager layoutManager;

  // Sets the starting page index
  private int startingPage;

  // The current offset index of loaded data
  private int page = 1;

  // The total number of items in the data set after the last load
  private int previousTotalItemCount = 0;

  // True if we are still waiting for the last set of data to load
  private boolean loading = true;

  protected EndlessRecyclerViewScrollListener(GridLayoutManager layoutManager, int startingPage) {
    this.layoutManager = layoutManager;
    this.startingPage = startingPage;
    remainingItemsThreshold = REMAINING_ROWS_THRESHOLD * layoutManager.getSpanCount();
  }

  @Override
  public void onScrolled(RecyclerView view, int dx, int dy) {
    int totalItemCount = layoutManager.getItemCount();
    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

    // If the total item count is zero and the previous isn't, assume the list is invalidated
    // and should be reset back to initial state.
    if (totalItemCount < previousTotalItemCount) {
      page = startingPage;
      previousTotalItemCount = totalItemCount;
      if (totalItemCount == 0) {
        loading = true;
      }
    }

    // If it’s still loading, we check to see if the data set count has changed,
    // if so we conclude it has finished loading and update total item count.
    if (loading && (totalItemCount > previousTotalItemCount)) {
      previousTotalItemCount = totalItemCount;
      loading = false;
    }

    // If it isn’t currently loading, we check to see if we have breached
    // the remainingItemsThreshold and need to load more data.
    if (!loading && totalItemCount - lastVisibleItemPosition < remainingItemsThreshold) {
      onLoadMore(++page);
      loading = true;
    }
  }

  /**
   * Reset the ScrollListener's state
   */
  public void resetState() {
    page = startingPage;
    previousTotalItemCount = 0;
    loading = true;
  }

  /**
   * This method is responsible for loading new data
   */
  public abstract void onLoadMore(int page);
}
