package com.appsflyer.donkey.client;

public interface Client<T> {
  
  void request(T opts);
  
  void shutdown();
}
