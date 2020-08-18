package com.appsflyer.donkey.route.handler.ring;

import clojure.lang.*;

import java.util.Iterator;
import java.util.Map;

/**
 * Mutable hash map implementation that can be used in Clojure code.
 */
public class ClojureMutableHashMap<K, V> extends ClojureMapWrapper<Map<K, V>>
{
  
  private static final long serialVersionUID = 3835764978957570205L;
  private final Map<K, V> store;
  
  public ClojureMutableHashMap(Map<K, V> impl)
  {
    store = impl;
  }
  
  @Override
  public boolean containsKey(Object key)
  {
    return store.containsKey(key);
  }
  
  @Override
  public IMapEntry entryAt(Object key)
  {
    return MapEntry.create(key, valAt(key));
  }
  
  @Override
  public IPersistentMap assoc(Object key, Object val)
  {
    store.put((K) key, (V) val);
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
    store.remove(key);
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
    return store.getOrDefault(key, (V) notFound);
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
    return ArraySeq.create(store.entrySet().toArray());
  }
  
  @Override
  public Iterator<MapEntry> iterator()
  {
    return iterator(store.entrySet().iterator());
  }
  
  @Override
  public Map<K, V> impl()
  {
    return store;
  }
  
  @Override
  public String toString()
  {
    return RT.printString(store);
  }
}
