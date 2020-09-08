package com.appsflyer.donkey.client.ring;

import clojure.lang.IPersistentMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;

public class RingClientRequestImpl implements RingClientRequest {
  
  private final HttpRequest<Buffer> impl;
  
  public RingClientRequestImpl(HttpRequest<Buffer> impl) {
    this.impl = impl;
  }
  
  @Override
  public void send() {
    impl.s
  }
  
  @Override
  public void send(byte[] bytes) {
  
  }
  
  @Override
  public void sendForm(IPersistentMap keyValuePairs) {
  
  }
}
