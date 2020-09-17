package com.appsflyer.donkey.server.ring.middleware;

import clojure.lang.IPersistentMap;

@FunctionalInterface
public interface RingMiddleware {
  
  IPersistentMap handle(IPersistentMap request);
  
}
