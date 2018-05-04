package com.udacity.popularmovies.di.modules;

import com.udacity.popularmovies.activities.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainActivityInjectorModule {

  @ContributesAndroidInjector(modules = {DiscoveryFragmentInjectorModule.class, DetailFragmentInjectorModule.class})
  public abstract MainActivity contributeMainActivityInjector();
}
