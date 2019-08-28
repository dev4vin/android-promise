package dev4vin.promise.model;

import dev4vin.promise.data.Store;

/** Created on 7/17/18 by yoctopus. */
public abstract class ChainProcessor {
  public abstract List<Store<?, ?, Throwable>> stores();

  public void terminate() {
    for (Store<?, ?, Throwable> store : stores())
      store.clear(new ResponseCallBack<Boolean, Throwable>());
  }
}
