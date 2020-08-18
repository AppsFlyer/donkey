package com.appsflyer.donkey.route.handler.ring;

@FunctionalInterface
public interface ImplWrapper<T>
{
  T impl();
}
