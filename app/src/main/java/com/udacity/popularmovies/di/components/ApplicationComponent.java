package com.udacity.popularmovies.di.components;

import android.app.Application;

import com.udacity.popularmovies.MoviesApplication;
import com.udacity.popularmovies.di.modules.ApplicationModule;
import com.udacity.popularmovies.di.modules.DiscoveryFragmentModule;
import com.udacity.popularmovies.di.modules.MainActivityInjectorModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Component(modules = {
    MainActivityInjectorModule.class,
    ApplicationModule.class,
    DiscoveryFragmentModule.class
})
@Singleton
public interface ApplicationComponent {

  void inject(MoviesApplication moviesApplication);

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder application(Application application);

    ApplicationComponent build();
  }
}
