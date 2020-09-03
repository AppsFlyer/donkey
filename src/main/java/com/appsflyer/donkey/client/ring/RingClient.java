package com.appsflyer.donkey.client.ring;

import clojure.lang.IPersistentMap;
import com.appsflyer.donkey.client.Client;
import com.appsflyer.donkey.client.ClientConfig;

public interface RingClient extends Client<IPersistentMap> {
  
  static RingClient create(ClientConfig config) {
    return new RingClientImpl(config);
  }
  
}
