package com.appsflyer.donkey.route.handler.ring;

import clojure.lang.APersistentMap;
import clojure.lang.MapEntry;

import java.util.Iterator;

/**
 * An abstract class that should be used to implement a Clojure map compatible
 * wrapper over a mutable data structure.
 *
 * @param <T> The type of the data structure used internally
 */
public abstract class ClojureMapWrapper<T> extends APersistentMap implements ImplWrapper<T>
{
  private static final long serialVersionUID = 5162058197983586012L;
  
  @Override
  public abstract Iterator<MapEntry> iterator();
  
  Iterator<MapEntry> iterator(Iterator<?> impl)
  {
    return new MapIterator(impl);
  }
  
  private static final class MapIterator implements Iterator<MapEntry>
  {
    
    private final Iterator<?> impl;
    
    MapIterator(Iterator<?> impl)
    {
      this.impl = impl;
    }
    
    @Override
    public boolean hasNext()
    {
      return impl.hasNext();
    }
    
    @Override
    public MapEntry next()
    {
      Entry<?, ?> entry = (Entry<?, ?>) impl.next();
      return MapEntry.create(entry.getKey(), entry.getValue());
    }
  }
}
