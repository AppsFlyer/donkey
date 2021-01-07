/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.appsflyer.donkey.server.ring.handler;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import com.appsflyer.donkey.server.handler.ErrorHandler;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.appsflyer.donkey.server.ring.handler.RingHandler.RING_HANDLER_RESULT;

public final class RingErrorHandler implements ErrorHandler<IFn> {
  
  private static final Keyword PATH = Keyword.intern("path");
  private static final Keyword CAUSE = Keyword.intern("cause");
  private static final Keyword STATUS = RingResponseField.STATUS.keyword();
  private final Handler<RoutingContext> responseAdapter;
  private final Map<Integer, Handler<RoutingContext>> handlers = new HashMap<>();
  
  public static RingErrorHandler create() {
    return new RingErrorHandler();
  }
  
  private RingErrorHandler() {
    responseAdapter = RingAdapterFactory.create().responseAdapter();
  }
  
  @Override
  public RingErrorHandler add(int statusCode, IFn handler) {
    handlers.put(statusCode, ctx -> {
      var response = (IPersistentMap) handler.invoke(payload(ctx));
      if (!response.containsKey(STATUS)) {
        response = response.assoc(STATUS, statusCode);
      }
      ctx.put(RING_HANDLER_RESULT, response);
      responseAdapter.handle(ctx);
    });
    return this;
  }
  
  @Override
  public void forEach(BiConsumer<Integer, Handler<RoutingContext>> consumer) {
    handlers.forEach(consumer);
  }
  
  private IPersistentMap payload(RoutingContext ctx) {
    var payload = RT.map(PATH, ctx.normalizedPath());
    if (ctx.failure() != null) {
      payload = payload.assoc(CAUSE, ctx.failure());
    }
    return payload;
  }
}
