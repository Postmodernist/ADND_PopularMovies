package com.udacity.popularmovies;

import android.app.Application;
import android.content.Context;

import com.udacity.popularmovies.di.components.ApplicationComponent;
import com.udacity.popularmovies.di.components.DaggerApplicationComponent;

public class MovieApplication extends Application {

  private ApplicationComponent applicationComponent;

  public static ApplicationComponent getAppComponent(Context context) {
    return ((MovieApplication) context.getApplicationContext()).applicationComponent;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    applicationComponent = DaggerApplicationComponent.builder().application(this).build();
  }
}
