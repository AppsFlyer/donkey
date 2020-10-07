package com.appsflyer.donkey.client.ring;

import clojure.lang.IPersistentMap;
import com.appsflyer.donkey.client.Client;
import com.appsflyer.donkey.client.ClientConfig;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;

public interface RingClient extends Client<IPersistentMap> {
  
  static RingClient create(ClientConfig config) {
    return new RingClientImpl(config);
  }
  
  Future<IPersistentMap> send(HttpRequest<Buffer> request, Object body);
  
  Future<IPersistentMap> sendForm(HttpRequest<Buffer> request, IPersistentMap body);
  
  Future<IPersistentMap> sendMultiPartForm(HttpRequest<Buffer> request, IPersistentMap body);
}
