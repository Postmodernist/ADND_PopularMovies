package com.alexbaryzhikov.popularmovies.di.components;

import android.app.Application;

import com.alexbaryzhikov.popularmovies.MovieApplication;
import com.alexbaryzhikov.popularmovies.di.modules.ApplicationModule;
import com.alexbaryzhikov.popularmovies.viewmodels.MovieViewModelFactory;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Component(modules = ApplicationModule.class)
@Singleton
public interface ApplicationComponent {

  void inject(MovieApplication application);

  MovieViewModelFactory moviesViewModelFactory();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder application(Application application);

    ApplicationComponent build();
  }
}
