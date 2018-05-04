package com.udacity.popularmovies.di.modules;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.udacity.popularmovies.R;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class DiscoveryFragmentModule {

  @Provides
  @Named("columns number")
  int provideColumnsNumber(Context context) {
    int displayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    int displayHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    int gridColumnsPortrait = context.getResources().getInteger(R.integer.grid_columns_portrait);
    int gridColumnsLandscape = context.getResources().getInteger(R.integer.grid_columns_landscape);
    return displayWidth < displayHeight ? gridColumnsPortrait : gridColumnsLandscape;
  }

  @Provides
  @Named("row height")
  int provideRowHeight(Context context, @Named("columns number") int columnsNumber) {
    int displayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    int widthRatio = context.getResources().getInteger(R.integer.poster_width_ratio);
    int heightRatio = context.getResources().getInteger(R.integer.poster_height_ratio);
    return displayWidth * heightRatio / (columnsNumber * widthRatio);
  }

  @Provides
  Context provideContext(Application application) {
    return  application.getApplicationContext();
  }
}
