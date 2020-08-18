package com.appsflyer.donkey.route.handler.ring;

import clojure.lang.*;
import io.vertx.ext.web.RoutingContext;

import java.util.*;

/**
 * The class implements a mutable Ring request compatible with Clojure's {@link IPersistentMap}
 * interface.
 * It can be used in Clojure code as a regular map, but any operations that would
 * normally cause a new Map to be created and returned, instead mutate the current map.
 * As such this implementation is <b>NOT</b> thread safe.
 * <p>
 * <p>
 * The implementation delegates retrieval of the fields that are part of the Ring
 * spec from the {@link RoutingContext} only when they are requested. Therefore you
 * can consider it as a "lazy" implementation. Once retrieved the values are cached internally
 * and will be returned from the cache the next time they are requested.
 */
public class MutableRingRequestMap extends ClojureMapWrapper<RoutingContext>
{
  private static final long serialVersionUID = -804770015531955274L;
  
  private final RoutingContext ctx;
  private final Map<Object, Object> store;
  private final Map<Object, Object> unknownKeys;
  private final Set<Object> removedKeys;
  
  MutableRingRequestMap(RoutingContext ctx)
  {
    this.ctx = ctx;
    store = new HashMap<>(14);
    unknownKeys = new HashMap<>(4);
    removedKeys = new HashSet<>(4);
  }
  
  @Override
  public RoutingContext impl()
  {
    return ctx;
  }
  
  @Override
  public boolean containsKey(Object key)
  {
    return !removedKeys.contains(key) &&
        (RingRequestField.exists(key) || unknownKeys.containsKey(key));
  }
  
  @Override
  public IMapEntry entryAt(Object key)
  {
    return MapEntry.create(key, valAt(key));
  }
  
  @Override
  public IPersistentMap assoc(Object key, Object val)
  {
    if (RingRequestField.exists(key)) {
      store.put(key, val);
      removedKeys.remove(key);
    }
    else {
      unknownKeys.put(key, val);
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
    if (RingRequestField.exists(key)) {
      store.remove(key);
      removedKeys.add(key);
    }
    else {
      unknownKeys.remove(key);
    }
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
    if (removedKeys.contains(key)) {
      return notFound;
    }
    var field = RingRequestField.from(key);
    if (field == null) {
      return unknownKeys.getOrDefault(key, notFound);
    }
    Object res = getByField(field);
    if (res == null) {
      return notFound;
    }
    return res;
  }
  
  private Object getByField(RingRequestField field)
  {
    return store.computeIfAbsent(field.keyword(), k -> field.get(ctx));
  }
  
  @Override
  public int count()
  {
    return RingRequestField.size() - removedKeys.size() + unknownKeys.size();
  }
  
  @Override
  public IPersistentCollection empty()
  {
    store.clear();
    unknownKeys.clear();
    removedKeys.addAll(Arrays.asList(RingRequestField.values()));
    return this;
  }
  
  @Override
  public ISeq seq()
  {
    Object[] res = new Object[count()];
    RingRequestField[] fields = RingRequestField.values();
    int i = 0;
    for (RingRequestField field : fields) {
      if (!removedKeys.contains(field.keyword())) {
        res[i] = MapEntry.create(field.keyword(), getByField(field));
        i++;
      }
    }
    for (var entry : unknownKeys.entrySet()) {
      res[i] = entry;
      i++;
    }
    return ArraySeq.create(res);
  }
  
  @Override
  public Iterator<MapEntry> iterator()
  {
    return new MapIterator(this);
  }
  
  private static final class MapIterator implements Iterator<MapEntry>
  {
    private MapEntry current;
    private MapEntry next;
    private final MutableRingRequestMap instance;
    private final RingRequestField[] fields;
    private final Iterator<Entry<Object, Object>> unknownIterator;
    private int fieldsCursor = -1;
    
    private MapIterator(MutableRingRequestMap instance)
    {
      this.instance = instance;
      fields = RingRequestField.values();
      unknownIterator = instance.unknownKeys.entrySet().iterator();
      current = getNextFieldsEntry();
      next = getNextFieldsEntry();
    }
    
    private MapEntry getNextFieldsEntry()
    {
      for (fieldsCursor += 1; fieldsCursor < fields.length; fieldsCursor++) {
        var field = fields[fieldsCursor];
        if (!instance.removedKeys.contains(field.keyword())) {
          return MapEntry.create(field.keyword(), instance.getByField(field));
        }
      }
      return null;
    }
    
    @Override
    public boolean hasNext()
    {
      return next != null;
    }
    
    @Override
    public MapEntry next()
    {
      if (current == null) {
        throw new NoSuchElementException();
      }
      
      MapEntry nextNext = getNextFieldsEntry();
      if (nextNext != null) {
        var res = current;
        current = next;
        next = nextNext;
        return res;
      }
      
      if (unknownIterator.hasNext()) {
        Map.Entry<?, ?> entry = unknownIterator.next();
        var res = current;
        current = next;
        next = MapEntry.create(entry.getKey(), entry.getValue());
        return res;
      }
      
      var res = current;
      current = next;
      next = null;
      return res;
    }
  }
}
