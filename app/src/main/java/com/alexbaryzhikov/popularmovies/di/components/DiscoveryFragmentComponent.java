package com.alexbaryzhikov.popularmovies.di.components;

import android.support.v7.widget.GridLayoutManager;

import com.alexbaryzhikov.popularmovies.di.modules.DiscoveryFragmentModule;
import com.alexbaryzhikov.popularmovies.di.scopes.DiscoveryFragmentScope;
import com.alexbaryzhikov.popularmovies.ui.DiscoveryFragment;

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
