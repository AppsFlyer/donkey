package com.appsflyer.donkey.client.ring;

import clojure.lang.IPersistentMap;
import com.appsflyer.donkey.client.ClientRequest;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;

public interface RingClientRequest extends ClientRequest<IPersistentMap> {
  static RingClientRequest create(HttpRequest<Buffer> impl) {
    return new RingClientRequestImpl(impl);
  }
}
