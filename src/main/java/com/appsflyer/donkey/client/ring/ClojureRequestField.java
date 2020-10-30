package com.appsflyer.donkey.client.ring;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import com.appsflyer.donkey.ValueExtractor;
import com.appsflyer.donkey.server.ring.handler.HttpMethodMapping;
import io.vertx.core.http.HttpMethod;
import org.jetbrains.annotations.Nullable;

/**
 * The Enum class encapsulates the logic of extracting data from a Clojure
 * map representing a client request.
 * Each element corresponds to a field in the request. It implements getting the
 * field's name as a {@link Keyword}, and extracting the corresponding value
 * from the request map.
 */
public enum ClojureRequestField implements ValueExtractor<IPersistentMap> {
  
  METHOD("method") {
    @Nullable
    @Override
    public HttpMethod from(IPersistentMap req) {
      var method = req.valAt(keyword(), null);
      return method == null ? null : HttpMethodMapping.get((Keyword) method);
    }
  },
  URI("uri") {
    @Override
    public String from(IPersistentMap req) {
      return (String) req.valAt(keyword(), "/");
    }
  },
  URL("url") {
    @Override
    public String from(IPersistentMap req) {
      return (String) req.valAt(keyword(), null);
    }
  },
  HOST("host") {
    @Nullable
    @Override
    public String from(IPersistentMap req) {
      return (String) req.valAt(keyword(), null);
    }
  },
  PORT("port") {
    @Nullable
    @Override
    public Integer from(IPersistentMap req) {
      var port = (Number) req.valAt(keyword(), null);
      return port == null ? null : port.intValue();
    }
  },
  SSL("ssl") {
    @Nullable
    @Override
    public Boolean from(IPersistentMap req) {
      return (Boolean) req.valAt(keyword(), null);
    }
  },
  TIMEOUT("idle-timeout-seconds") {
    @Nullable
    @Override
    public Long from(IPersistentMap req) {
      var timeout = (Number) req.valAt(keyword(), null);
      return timeout == null ? null : timeout.longValue();
    }
  },
  BEARER_TOKEN("bearer-token") {
    @Nullable
    @Override
    public String from(IPersistentMap req) {
      return (String) req.valAt(keyword(), null);
    }
  },
  BASIC_AUTH("basic-auth") {
    @Nullable
    @Override
    public IPersistentMap from(IPersistentMap req) {
      return (IPersistentMap) req.valAt(keyword(), null);
    }
  },
  QUERY_PARAMS("query-params") {
    @Nullable
    @Override
    public IPersistentMap from(IPersistentMap req) {
      return (IPersistentMap) req.valAt(keyword(), null);
    }
  },
  HEADERS("headers") {
    @Nullable
    @Override
    public IPersistentMap from(IPersistentMap req) {
      return (IPersistentMap) req.valAt(keyword(), null);
    }
  };
  
  private final Keyword keyword;
  
  ClojureRequestField(String field) {
    keyword = Keyword.intern(field);
  }
  
  /**
   * @return The field name as a Keyword
   */
  public Keyword keyword() {
    return keyword;
  }
}
