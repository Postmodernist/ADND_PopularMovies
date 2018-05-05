package com.udacity.popularmovies.di.components;

import com.udacity.popularmovies.di.modules.DetailFragmentModule;
import com.udacity.popularmovies.di.scopes.DetailFragmentScope;
import com.udacity.popularmovies.fragments.DetailFragment;

import dagger.BindsInstance;
import dagger.Component;

@Component(modules = DetailFragmentModule.class, dependencies = ApplicationComponent.class)
@DetailFragmentScope
public interface DetailFragmentComponent {

  void inject(DetailFragment fragment);

  @Component.Builder
  interface Builder {

    Builder appComponent(ApplicationComponent component);

    @BindsInstance
    Builder fragment(DetailFragment fragment);

    DetailFragmentComponent build();
  }
}
