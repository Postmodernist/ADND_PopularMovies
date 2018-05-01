package com.udacity.popularmovies.di;

import android.app.Application;

import com.udacity.popularmovies.MoviesApplication;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Component
@Singleton
public interface ApplicationComponent {

  void inject(MoviesApplication application);

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder application(Application application);

    ApplicationComponent build();
  }
}
