package com.appsflyer.donkey.route.handler;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

public final class DateHeaderGenerator implements Handler<RoutingContext>, PeriodicTask, Supplier<String> {
  
  private static final Object mutex = new Object();
  private static volatile DateHeaderGenerator instance;
  private final Vertx vertx;
  private String date;
  private Long id;
  
  public static DateHeaderGenerator getInstance(Vertx vertx) {
    if (instance == null) {
      //noinspection SynchronizationOnStaticField There shouldn't be any contention here because the method is called once.
      synchronized (mutex) {
        if (instance == null) {
          instance = new DateHeaderGenerator(vertx).start();
        }
      }
    }
    return instance;
  }
  
  private DateHeaderGenerator(Vertx vertx) {
    this.vertx = vertx;
  }
  
  @Override
  public void handle(RoutingContext ctx) {
    ctx.addHeadersEndHandler(v -> ctx.response().putHeader("Date", date));
    ctx.next();
  }
  
  @Override
  public DateHeaderGenerator start() {
    update(0L);
    id = vertx.setPeriodic(1000, this::update);
    return this;
  }
  
  @Override
  public DateHeaderGenerator cancel() {
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

