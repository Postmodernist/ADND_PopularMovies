package com.udacity.popularmovies.di.modules;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.udacity.popularmovies.api.MoviesApi;
import com.udacity.popularmovies.repositories.MoviesRepository;
import com.udacity.popularmovies.utils.ApiUtils;
import com.udacity.popularmovies.viewmodels.MoviesViewModelFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ApplicationModule {

  @Provides
  @Singleton
  MoviesViewModelFactory provideMoviesViewModelFactory(MoviesRepository moviesRepository, SharedPreferences sharedPreferences) {
    return new MoviesViewModelFactory(moviesRepository, sharedPreferences);
  }

  @Provides
  @Singleton
  MoviesRepository provideMoviesRepository(MoviesApi moviesApi, Executor executor) {
    return new MoviesRepository(moviesApi, executor);
  }

  @Provides
  @Singleton
  Executor provideExecutor() {
    return Executors.newFixedThreadPool(2);
  }

  @Provides
  @Singleton
  MoviesApi provideMoviesApi(Retrofit retrofit) {
    return retrofit.create(MoviesApi.class);
  }

  @Provides
  @Singleton
  Retrofit provideRetrofit() {
    return new Retrofit.Builder()
        .baseUrl(ApiUtils.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
  }

  @Provides
  @Singleton
  SharedPreferences provideSharedPreferences(Application application) {
    return PreferenceManager.getDefaultSharedPreferences(application);
  }
}
