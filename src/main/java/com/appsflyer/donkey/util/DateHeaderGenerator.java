package com.appsflyer.donkey.util;

import io.vertx.core.Vertx;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

public class DateHeaderGenerator implements PeriodicTask, Supplier<String>
{
  private final Vertx vertx;
  private String date;
  private Long id;
  
  public DateHeaderGenerator(Vertx vertx)
  {
    this.vertx = vertx;
  }
  
  @Override
  public DateHeaderGenerator start()
  {
    update(0L);
    id = vertx.setPeriodic(1000, this::update);
    return this;
  }
  
  @Override
  public DateHeaderGenerator cancel()
  {
    vertx.cancelTimer(id);
    return this;
  }
  
  @Override
  public String get()
  {
    return date;
  }
  
  private void update(Long timerId)
  {
    date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now());
  }
  
}
