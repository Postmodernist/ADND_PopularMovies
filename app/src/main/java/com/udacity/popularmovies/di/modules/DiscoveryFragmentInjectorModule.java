package com.udacity.popularmovies.di.modules;

import com.udacity.popularmovies.fragments.DiscoveryFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class DiscoveryFragmentInjectorModule {

  @ContributesAndroidInjector
  public abstract DiscoveryFragment contributeDiscoveryFragmentInjector();
}
