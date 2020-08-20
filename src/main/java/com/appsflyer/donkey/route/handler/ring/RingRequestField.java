package com.appsflyer.donkey.route.handler.ring;

import clojure.lang.Keyword;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.cert.Certificate;
import java.util.Map;

/**
 * The Enum class encapsulates the logic of translating between a Vertx {@link RoutingContext}
 * and a Ring request.
 * Each element corresponds to a Ring request field. It implements getting the field's
 * name as a Keyword or string, and extracting the corresponding value from the RoutingContext
 * in the format / type as described in the Ring spec.
 */
public enum RingRequestField
{
  SERVER_PORT("server-port") {
    @Override
    public Integer get(RoutingContext ctx)
    {
      return ctx.request().localAddress().port();
    }
  },
  SERVER_NAME("server-name") {
    @Override
    public String get(RoutingContext ctx)
    {
      return ctx.request().host();
    }
  },
  REMOTE_ADDRESS("remote-addr") {
    @Override
    public String get(RoutingContext ctx)
    {
      var forwardedFor = ctx.request().getHeader("x-forwarded-for");
      if (forwardedFor != null) {
        return forwardedFor;
      }
      else {
        SocketAddress remoteAddress = ctx.request().remoteAddress();
        if (remoteAddress != null) {
          return remoteAddress.toString();
        }
      }
      return null;
    }
  },
  URI("uri") {
    @Override
    public String get(RoutingContext ctx)
    {
      return ctx.request().path();
    }
  },
  SCHEME("scheme") {
    private final Map<String, Keyword> schemeMapping =
        Map.of("http", Keyword.intern("http"),
               "https", Keyword.intern("https"));
    
    @Override
    public Keyword get(RoutingContext ctx)
    {
      return schemeMapping.get(ctx.request().scheme());
    }
  },
  REQUEST_METHOD("request-method") {
    @Override
    public Object get(RoutingContext ctx)
    {
      return HttpMethodMapping.get(ctx.request().method());
    }
  },
  PROTOCOL("protocol") {
    @Override
    public String get(RoutingContext ctx)
    {
      return HttpProtocolMapping.get(ctx.request().version());
    }
  },
  CLIENT_CERT("ssl-client-cert") {
    @Override
    public Certificate[] get(RoutingContext ctx)
    {
      try {
        if (ctx.request().isSSL()) {
          return ctx.request().sslSession().getPeerCertificates();
        }
      } catch (SSLPeerUnverifiedException e) {
        logger.warn("Caught exception getting SSL peer certificates: {}", e.getMessage());
      }
      return null;
    }
  },
  QUERY_STRING("query-string") {
    @Override
    public String get(RoutingContext ctx)
    {
      return ctx.request().query();
    }
  },
  QUERY_PARAMS("query-params") {
    @Override
    public MultiMap get(RoutingContext ctx)
    {
      try {
        return ctx.queryParams();
      } catch (HttpStatusException ex) {
        logger.warn("{}. Raw query string: {}", ex.getMessage(), ctx.request().query());
        return MultiMap.caseInsensitiveMultiMap();
      }
    }
  },
  PATH_PARAMS("path-params") {
    @Override
    public Map<String, String> get(RoutingContext ctx)
    {
      return ctx.pathParams();
    }
  },
  FORM_PARAMS("form-params") {
    @Override
    public MultiMap get(RoutingContext ctx)
    {
      if (ctx.request().isExpectMultipart()) {
        return ctx.request().formAttributes();
      }
      else {
        return MultiMap.caseInsensitiveMultiMap();
      }
    }
  },
  HEADERS("headers") {
    @Override
    public MultiMap get(RoutingContext ctx)
    {
      return ctx.request().headers();
    }
  },
  BODY("body") {
    @Override
    public byte[] get(RoutingContext ctx)
    {
      Buffer body = ctx.getBody();
      if (body != null) {
        return body.getBytes();
      }
      return BYTES;
    }
  };
  
  private static final Logger logger = LoggerFactory.getLogger(RingRequestField.class.getName());
  private static final Map<Keyword, RingRequestField> keywordToEnumMapping;
  private static final byte[] BYTES = new byte[0];
  private final String field;
  private final Keyword keyword;
  
  static {
    keywordToEnumMapping = Map.ofEntries(
        Map.entry(SERVER_PORT.keyword, SERVER_PORT),
        Map.entry(SERVER_NAME.keyword, SERVER_NAME),
        Map.entry(REMOTE_ADDRESS.keyword, REMOTE_ADDRESS),
        Map.entry(URI.keyword, URI),
        Map.entry(QUERY_STRING.keyword, QUERY_STRING),
        Map.entry(QUERY_PARAMS.keyword, QUERY_PARAMS),
        Map.entry(PATH_PARAMS.keyword, PATH_PARAMS),
        Map.entry(FORM_PARAMS.keyword, FORM_PARAMS),
        Map.entry(SCHEME.keyword, SCHEME),
        Map.entry(REQUEST_METHOD.keyword, REQUEST_METHOD),
        Map.entry(PROTOCOL.keyword, PROTOCOL),
        Map.entry(CLIENT_CERT.keyword, CLIENT_CERT),
        Map.entry(HEADERS.keyword, HEADERS),
        Map.entry(BODY.keyword, BODY));
  }
  
  /**
   * Get a field from a Keyword.
   * <p></p>
   * <b>Note</b>: The argument should be a Keyword since all request fields in Ring are keywords.
   * The signature accepts an `Object` to be compatible with Clojure maps that
   * are not type safe.
   *
   * @param keyword The name of the field to return
   */
  public static RingRequestField from(Object keyword)
  {
    return keywordToEnumMapping.get(keyword);
  }
  
  /**
   * Check if a field exists.
   * <p></p>
   * <b>Note</b>: The argument should be a Keyword since all request fields in Ring are keywords.
   * The signature accepts an `Object` to be compatible with Clojure maps that
   * are not type safe.
   *
   * @param keyword The name of the field to check
   */
  public static boolean exists(Object keyword)
  {
    return keywordToEnumMapping.containsKey(keyword);
  }
  
  /**
   * @return The number of fields
   */
  public static int size()
  {
    return keywordToEnumMapping.size();
  }
  
  RingRequestField(String field)
  {
    this.field = field;
    keyword = Keyword.intern(field);
  }
  
  /**
   * Get the value for the this field from the {@link RoutingContext}
   */
  public abstract Object get(RoutingContext ctx);
  
  /**
   * @return The field name as a string
   */
  public String field()
  {
    return field;
  }
  
  /**
   * @return The field name as a Keyword
   */
  public Keyword keyword()
  {
    return keyword;
  }
}
