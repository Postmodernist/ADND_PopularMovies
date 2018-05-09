package com.udacity.popularmovies.utils;

import android.support.annotation.NonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Runs func() using funcExecutor.
 * When it's finished runs onSuccess()/onFailure() using callbackExecutor.
 */
public abstract class AsyncFuncWithCallback<T> {

  private Executor funcExecutor;
  private Executor callbackExecutor;

  protected AsyncFuncWithCallback(Executor funcExecutor, Executor callbackExecutor) {
    this.funcExecutor = funcExecutor;
    this.callbackExecutor = callbackExecutor;
  }

  public abstract T func();

  public abstract void onSuccess(@NonNull T result);

  public abstract void onFailure(@NonNull Throwable t);

  public void run() {
    try {
      ListeningExecutorService service = MoreExecutors.listeningDecorator((ExecutorService) funcExecutor);
      ListenableFuture<T> future = service.submit(this::func);
      Futures.addCallback(future, new FutureCallback<T>() {

        @Override
        public void onSuccess(@NonNull T result) {
          AsyncFuncWithCallback.this.onSuccess(result);
        }

        @Override
        public void onFailure(@NonNull Throwable t) {
          AsyncFuncWithCallback.this.onFailure(t);
        }
      }, callbackExecutor);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
