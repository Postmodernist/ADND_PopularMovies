package com.alexbaryzhikov.popularmovies.di.components;

import com.alexbaryzhikov.popularmovies.di.modules.DetailFragmentModule;
import com.alexbaryzhikov.popularmovies.di.scopes.DetailFragmentScope;
import com.alexbaryzhikov.popularmovies.ui.DetailFragment;

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
