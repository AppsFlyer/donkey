package com.appsflyer.donkey.client;

public interface Client<T> {
  
  ClientRequest request(T opts);
  
  void shutdown();
}
