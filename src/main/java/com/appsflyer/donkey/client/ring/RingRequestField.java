package com.appsflyer.donkey.client.ring;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import com.appsflyer.donkey.ValueExtractor;
import com.appsflyer.donkey.route.ring.HttpMethodMapping;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import org.jetbrains.annotations.Nullable;

import static com.appsflyer.donkey.util.TypeConverter.toBuffer;

/**
 * The Enum class encapsulates the logic of extracting data from a Clojure client request.
 * Each element corresponds to a field in the request. It implements getting the field's
 * name as a {@link Keyword}, and extracting the corresponding value from
 * the request.
 */
public enum RingRequestField implements ValueExtractor<IPersistentMap> {
  
  METHOD("method") {
    @Override
    public HttpMethod from(IPersistentMap req) {
      return HttpMethodMapping.get((Keyword) req.valAt(keyword(), null));
    }
  },
  URI("uri") {
    @Override
    public String from(IPersistentMap req) {
      return (String) req.valAt(keyword(), "/");
    }
  },
  HOST("host") {
    @Override
    public String from(IPersistentMap req) {
      return (String) req.valAt(keyword(), null);
    }
  },
  PORT("port") {
    @Override
    public Integer from(IPersistentMap req) {
      return (Integer) req.valAt(keyword(), null);
    }
  },
  TIMEOUT("timeout-milliseconds") {
    @Nullable
    @Override
    public Long from(IPersistentMap req) {
      var timeout = (Number) req.valAt(keyword(), null);
      if (timeout != null) {
        return timeout.longValue();
      }
      return null;
    }
  },
  BEARER_TOKEN("bearer-token") {
    @Override
    public String from(IPersistentMap req) {
      return (String) req.valAt(keyword(), null);
    }
  },
  BASIC_AUTH("basic-auth") {
    @Nullable
    @Override
    public IPersistentMap from(IPersistentMap req) {
      var credentials = req.valAt(keyword(), null);
      if (credentials != null) {
        return (IPersistentMap) credentials;
      }
      return null;
    }
  },
  QUERY_PARAMS("query-params") {
    @Override
    public IPersistentMap from(IPersistentMap req) {
      return (IPersistentMap) req.valAt(keyword(), null);
    }
  },
  HEADERS("headers") {
    @Override
    public IPersistentMap from(IPersistentMap req) {
      return (IPersistentMap) req.valAt(keyword(), null);
    }
  },
  HANDLER("handler") {
    //The handler must be a Handler<AsyncResult<IPersistentMap>> or null
    //If it's not, then we want to throw a ClassCastException here.
    @SuppressWarnings("unchecked")
    @Override
    public Handler<AsyncResult<IPersistentMap>> from(IPersistentMap req) {
      return (Handler<AsyncResult<IPersistentMap>>) req.valAt(keyword(), null);
    }
  },
  BODY("body") {
    @Nullable
    @Override
    public Buffer from(IPersistentMap req) {
      var body = req.valAt(keyword(), null);
      if (body != null) {
        return toBuffer(body);
      }
      return null;
    }
  };
  
  private final Keyword keyword;
  
  RingRequestField(String field) {
    keyword = Keyword.intern(field);
  }
  
  /**
   * @return The field name as a Keyword
   */
  public Keyword keyword() {
    return keyword;
  }
}
