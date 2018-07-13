package com.alexbaryzhikov.popularmovies.di.modules;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.alexbaryzhikov.popularmovies.api.MovieApi;
import com.alexbaryzhikov.popularmovies.di.qualifiers.DiskExecutor;
import com.alexbaryzhikov.popularmovies.di.qualifiers.MainThreadExecutor;
import com.alexbaryzhikov.popularmovies.di.qualifiers.NetworkExecutor;
import com.alexbaryzhikov.popularmovies.api.ApiUtils;

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
  @DiskExecutor
  Executor provideDiskExecutor() {
    return Executors.newSingleThreadExecutor();
  }

  @Provides
  @Singleton
  @NetworkExecutor
  Executor provideNetworkExecutor() {
    return Executors.newFixedThreadPool(3);
  }

  @Provides
  @Singleton
  @MainThreadExecutor
  Executor provideMainThreadExecutor() {
    return new Executor() {
      private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

      @Override
      public void execute(@NonNull Runnable command) {
        mainThreadHandler.post(command);
      }
    };
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
        .baseUrl(ApiUtils.TMDB_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
  }

  @Provides
  @Singleton
  SharedPreferences provideSharedPreferences(Application application) {
    return PreferenceManager.getDefaultSharedPreferences(application);
  }
}
