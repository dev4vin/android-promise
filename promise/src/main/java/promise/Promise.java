/*
 *
 *  * Copyright 2017, Peter Vincent
 *  * Licensed under the Apache License, Version 2.0, Promise.
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package promise;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import promise.data.log.LogUtil;
import promise.data.net.FastParser;
import promise.model.Action;
import promise.model.List;
import promise.model.Message;
import promise.model.ResponseCallBack;
import promise.model.function.MapFunction;
import promise.util.Conditions;

public class Promise {
  public static final String TAG = LogUtil.makeTag(Promise.class);
  public static final String CLEANING_UP_RESOURCES = "Cleaning up resources";
  private static Promise instance;
  private Context context;
  private ExecutorService executor;
  private PublishSubject<Message> bus;
  private Handler handler;
  private final BroadcastReceiver networkChangeReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false))
            send(new Message(TAG, "Network shut down"));
          else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true))
            send(new Message(TAG, FastParser.NETWORK_IS_BACK));
        }
      };
  private CompositeDisposable disposable;
  private List<Disposable> disposables;

  private Promise(Context context) {
    this.context = context;
    disposable = new CompositeDisposable();
    this.context.registerReceiver(
        networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
  }

  public static Promise init(Context context) {
    RxJavaPlugins.setErrorHandler(throwable -> {
      if (throwable instanceof UndeliverableException)
        LogUtil.e(TAG, "undeliverable error: ", throwable);
      else Thread.currentThread().getUncaughtExceptionHandler()
          .uncaughtException(Thread.currentThread(), throwable);
    });
    if (instance != null) throw new IllegalStateException("Promise can only be instantiated once");
    instance = new Promise(context);
    return instance;
  }

  public static Promise instance() {
    if (instance == null) throw new RuntimeException("Initialize promise first");
    return instance;
  }

  public void send(Message object) {
    if (bus == null) bus = PublishSubject.create();
    bus.onNext(object);
  }

  public int listen(final String sender, final ResponseCallBack<Object, Throwable> callBack) {
    if (bus == null) bus = PublishSubject.create();
    if (disposables == null) disposables = new List<>();
    disposables.add(
        bus.subscribeOn(Schedulers.from(executor))
            .observeOn(Schedulers.from(executor))
            .subscribe(
                object -> {
                  if (sender.equals(object.sender())) callBack.response(object);
                },
                callBack::error));
    disposable.add(Conditions.checkNotNull(disposables.last()));
    return disposables.size() - 1;
  }

  public void stopListening(int id) {
    if (bus == null) return;
    if (disposables == null || disposables.isEmpty()) return;
    disposable.remove(disposables.get(id));
  }

  public Context context() {
    return context;
  }

  public void context(Context context) {
    this.context = context;
  }

  public ExecutorService executor() {
    if (executor == null) return Executors.newSingleThreadExecutor();
    return executor;
  }

  public Promise threads(int threads) {
    if (executor == null) executor = Executors.newFixedThreadPool(threads);
    return this;
  }

  public Promise disableErrors() {
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
    });
    return this;
  }

  public void execute(Runnable runnable) {
    executor.execute(runnable);
  }

  public void executeOnUi(Runnable runnable) {
    if (handler == null) handler = new Handler(Looper.getMainLooper());
    handler.post(runnable);
  }

  public void executeRepeatativelyWithSeconds(Runnable runnable, long waitInterval) {
    ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate
        (runnable, 0, waitInterval, TimeUnit.SECONDS);
  }

  public <T> void execute(
      final Action<T> action, final ResponseCallBack<T, Throwable> responseCallBack) {
    if (disposables == null) disposables = new List<>();
    disposables.add(
        Observable.fromCallable(
            action::execute)
            .observeOn(Schedulers.from(executor))
            .subscribeOn(Schedulers.from(executor))
            .subscribe(
                responseCallBack::response,
                responseCallBack::error));
    disposable.add(Conditions.checkNotNull(disposables.last()));
  }

  public <T> void executeOnUi(
      final Action<T> action, final ResponseCallBack<T, Throwable> responseCallBack) {
    if (disposables == null) disposables = new List<>();
    disposables.add(
        Observable.fromCallable(
            action::execute)
            .observeOn(Schedulers.from(executor))
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(
                responseCallBack::response,
                responseCallBack::error));
    disposable.add(Conditions.checkNotNull(disposables.last()));
  }

 /* public <T> void executeAsync(
    final AsyncAction<T> action, final ResponseCallBack<T, Throwable> responseCallBack) {
    if (disposables == null) disposables = new List<>();
    disposables.add(
      Observable.fromCallable(
        new Callable<T>() {
          @Override
          public T call() throws Exception {
            return action.execute();
          }
        })
        .observeOn(Schedulers.from(instance().executor))
        .subscribeOn(Schedulers.from(instance().executor))
        .subscribe(
          new Consumer<T>() {
            @Override
            public void accept(T t) {
              responseCallBack.response(t);
            }
          },
          new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
              responseCallBack.error(throwable);
            }
          }));
    disposable.add(Conditions.checkNotNull(disposables.last()));
  }*/

  public void execute(
      final List<Action<?>> actions, final ResponseCallBack<List<?>, Throwable> responseCallBack) {
    if (disposables == null) disposables = new List<>();
    disposables.add(
        Observable.zip(
            actions.map(
                (MapFunction<ObservableSource<?>, Action<?>>) action -> (ObservableSource<Object>) observer -> {
                  try {
                    observer.onNext(action.execute());
                  } catch (Exception e) {
                    observer.onError(e);
                  }
                }),
            List::fromArray)
            .observeOn(Schedulers.from(executor))
            .subscribeOn(Schedulers.from(executor))
            .subscribe(
                responseCallBack::response,
                responseCallBack::error));
    disposable.add(Conditions.checkNotNull(disposables.last()));
  }

  public void executeOnUi(
      final List<Action<?>> actions, final ResponseCallBack<List<?>, Throwable> responseCallBack) {
    if (disposables == null) disposables = new List<>();
    disposables.add(
        Observable.zip(
            actions.map(
                (MapFunction<ObservableSource<?>, Action<?>>) action -> (ObservableSource<Object>) observer -> {
                  try {
                    observer.onNext(action.execute());
                  } catch (Exception e) {
                    observer.onError(e);
                  }
                }),
            List::fromArray)
            .observeOn(Schedulers.from(executor))
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(
                responseCallBack::response,
                responseCallBack::error));
    disposable.add(Conditions.checkNotNull(disposables.last()));
  }

  public boolean terminate() {
    send(new Message(TAG, CLEANING_UP_RESOURCES));
    context.unregisterReceiver(networkChangeReceiver);
    context = null;
    disposable.dispose();
    disposables.clear();
    bus = null;
    executor().shutdownNow();
    return executor().isShutdown();
  }
}
