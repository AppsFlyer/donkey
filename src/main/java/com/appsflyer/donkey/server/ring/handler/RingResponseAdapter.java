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

import clojure.lang.IMapEntry;
import clojure.lang.IPersistentMap;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static com.appsflyer.donkey.server.ring.handler.RingResponseField.*;
import static com.appsflyer.donkey.util.TypeConverter.toBuffer;

public final class RingResponseAdapter implements RingHandler {
  
  private static final Logger logger = LoggerFactory.getLogger(RingResponseAdapter.class.getName());
  
  public static RingHandler create() {
    return new RingResponseAdapter();
  }
  
  private RingResponseAdapter() {}
  
  @Override
  public void handle(RoutingContext ctx) {
    IPersistentMap ringResponse = ctx.get(RING_HANDLER_RESULT);
    
    if (ringResponse == null) {
      ctx.response().end();
      return;
    }
    
    HttpServerResponse serverResponse = ctx.response();
    addHeaders(serverResponse, ringResponse);
    setStatus(serverResponse, ringResponse);
    sendResponse(serverResponse, ringResponse);
  }
  
  private void addHeaders(HttpServerResponse serverResponse, IPersistentMap ringResponse) {
    var headers = (IPersistentMap) HEADERS.from(ringResponse);
    if (headers != null) {
      for (var obj : headers) {
        var pair = (IMapEntry) obj;
        serverResponse.putHeader((CharSequence) pair.getKey(), (CharSequence) pair.getValue());
      }
    }
  }
  
  private void setStatus(HttpServerResponse serverResponse, IPersistentMap ringResponse) {
    serverResponse.setStatusCode((Integer) STATUS.from(ringResponse));
  }
  
  private void sendResponse(HttpServerResponse serverResponse, IPersistentMap ringResponse) {
    var body = BODY.from(ringResponse);
    if (body instanceof File) {
      sendFile(serverResponse, (File) body);
    } else {
      serverResponse.end(toBuffer(body));
    }
  }
  
  private void sendFile(HttpServerResponse serverResponse, File file) {
    try {
      serverResponse.sendFile(file.getCanonicalPath(), 0, file.length())
                    .onFailure(ex -> {
                      logFileFailure(file, ex);
                      serverResponse.setStatusCode(500).end();
                    });
    } catch (IOException ex) {
      logFileFailure(file, ex);
      serverResponse.setStatusCode(500).end();
    }
  }
  
  private void logFileFailure(File file, Throwable ex) {
    logger.error("Failed to send file '{}': {}",
                 file.getPath(), ex.getMessage());
  }
}
