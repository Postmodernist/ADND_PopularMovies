package com.udacity.popularmovies.di.components;

import android.support.v7.widget.GridLayoutManager;

import com.udacity.popularmovies.di.modules.DiscoveryFragmentModule;
import com.udacity.popularmovies.di.scopes.DiscoveryFragmentScope;
import com.udacity.popularmovies.fragments.DiscoveryFragment;
import com.udacity.popularmovies.utils.EndlessRecyclerViewScrollListener;

import javax.inject.Named;

import dagger.BindsInstance;
import dagger.Component;

@Component(modules = DiscoveryFragmentModule.class, dependencies = ApplicationComponent.class)
@DiscoveryFragmentScope
public interface DiscoveryFragmentComponent {

  void inject(DiscoveryFragment fragment);

  GridLayoutManager gridLayoutManager();

  @Component.Builder
  interface Builder {

    Builder appComponent(ApplicationComponent component);

    @BindsInstance
    Builder fragment(DiscoveryFragment fragment);

    DiscoveryFragmentComponent build();
  }
}
