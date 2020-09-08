package com.appsflyer.donkey.client;

public interface ClientRequest<T> {
  
  void send();
  
  void send(byte[] bytes);
  
  void sendForm(T keyValuePairs);
}
