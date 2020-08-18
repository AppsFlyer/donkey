package com.appsflyer.donkey.route.handler.ring;

import clojure.lang.*;
import io.vertx.core.MultiMap;

import java.util.Iterator;

/**
 * Clojure compatible mutable wrapper over {@link io.vertx.core.MultiMap}
 */
public class ClojureMultiMapWrapper extends ClojureMapWrapper<MultiMap>
{
  
  private static final long serialVersionUID = 7486551801647542071L;
  private final MultiMap store;
  
  ClojureMultiMapWrapper(MultiMap impl)
  {
    store = impl;
  }
  
  @Override
  public boolean containsKey(Object key)
  {
    return store.contains(key.toString());
  }
  
  @Override
  public IMapEntry entryAt(Object key)
  {
    return MapEntry.create(key, valAt(key));
  }
  
  @Override
  public IPersistentMap assoc(Object key, Object val)
  {
    if (val instanceof String) {
      store.add(key.toString(), (String) val);
    }
    else if (val instanceof Iterable) {
      store.add(key.toString(), (Iterable<String>) val);
    }
    else {
      throw new IllegalArgumentException("Value can only be a String or Iterable");
    }
    return this;
  }
  
  @Override
  public IPersistentMap assocEx(Object key, Object val)
  {
    if (containsKey(key)) {
      throw new RuntimeException("Key already present");
    }
    assoc(key, val);
    return this;
  }
  
  @Override
  public IPersistentMap without(Object key)
  {
    store.remove(key.toString());
    return this;
  }
  
  @Override
  public Object valAt(Object key)
  {
    return valAt(key, null);
  }
  
  @Override
  public Object valAt(Object key, Object notFound)
  {
    String res = store.get(key.toString());
    if (res == null) {
      return notFound;
    }
    return res;
  }
  
  @Override
  public int count()
  {
    return store.size();
  }
  
  @Override
  public IPersistentCollection empty()
  {
    store.clear();
    return this;
  }
  
  @Override
  public ISeq seq()
  {
    return ArraySeq.create(store.entries().toArray());
  }
  
  @Override
  public Iterator<MapEntry> iterator()
  {
    return iterator(store.iterator());
  }
  
  @Override
  public MultiMap impl()
  {
    return store;
  }
  
  @Override
  public String toString()
  {
    return RT.printString(store);
  }
}
