package com.udacity.popularmovies.di.modules;

import com.udacity.popularmovies.fragments.DetailFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class DetailFragmentInjectorModule {

  @ContributesAndroidInjector
  public abstract DetailFragment contributeDetailFragmentInjector();
}
