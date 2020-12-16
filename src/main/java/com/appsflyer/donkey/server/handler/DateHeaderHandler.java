/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appsflyer.donkey.server.handler;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * Handler that adds a {@code Date} header to the response.
 */
public final class DateHeaderHandler implements Handler<RoutingContext>, PeriodicTask, Supplier<String> {
  
  private static final String DATE_HEADER = "Date";
  private final Vertx vertx;
  private String date;
  private Long id;
  
  public static DateHeaderHandler create(Vertx vertx) {
    return new DateHeaderHandler(vertx).start();
  }
  
  private DateHeaderHandler(Vertx vertx) {
    this.vertx = vertx;
  }
  
  @Override
  public void handle(RoutingContext ctx) {
    ctx.addHeadersEndHandler(v -> ctx.response().putHeader(DATE_HEADER, date));
    ctx.next();
  }
  
  @Override
  public DateHeaderHandler start() {
    update(0L);
    id = vertx.setPeriodic(1000, this::update);
    return this;
  }
  
  @Override
  public DateHeaderHandler cancel() {
    vertx.cancelTimer(id);
    return this;
  }
  
  @Override
  public String get() {
    return date;
  }
  
  private void update(Long timerId) {
    date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now());
  }
  
}

