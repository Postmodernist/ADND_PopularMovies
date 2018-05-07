package com.udacity.popularmovies.di.modules;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.udacity.popularmovies.api.MovieApi;
import com.udacity.popularmovies.repositories.MovieRepository;
import com.udacity.popularmovies.utils.ApiUtils;
import com.udacity.popularmovies.viewmodels.MovieViewModelFactory;

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
  Executor provideExecutor() {
    return Executors.newFixedThreadPool(3);
  }

  @Provides
  @Singleton
  MovieApi provideMoviesApi(Retrofit retrofit) {
    return retrofit.create(MovieApi.class);
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
