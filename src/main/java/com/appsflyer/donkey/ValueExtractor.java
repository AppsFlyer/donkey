package com.appsflyer.donkey;

@FunctionalInterface
public interface ValueExtractor<T> {
  
  Object from(T source);
}
