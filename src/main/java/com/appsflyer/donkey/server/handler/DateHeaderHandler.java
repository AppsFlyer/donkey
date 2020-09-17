package com.appsflyer.donkey.server.handler;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

public final class DateHeaderHandler implements Handler<RoutingContext>, PeriodicTask, Supplier<String> {
  
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
    ctx.addHeadersEndHandler(v -> ctx.response().putHeader("Date", date));
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

