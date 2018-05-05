package com.udacity.popularmovies.di.components;

import android.app.Application;

import com.udacity.popularmovies.MoviesApplication;
import com.udacity.popularmovies.di.modules.ApplicationModule;
import com.udacity.popularmovies.viewmodels.MoviesViewModelFactory;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Component(modules = ApplicationModule.class)
@Singleton
public interface ApplicationComponent {

  void inject(MoviesApplication application);

  MoviesViewModelFactory moviesViewModelFactory();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder application(Application application);

    ApplicationComponent build();
  }
}
