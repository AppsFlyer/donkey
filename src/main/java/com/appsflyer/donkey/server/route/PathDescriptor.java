package com.appsflyer.donkey.server.route;

public interface PathDescriptor {
  
  static PathDescriptor create(String value) {
    return new PathDescriptorImpl(value);
  }
  
  static PathDescriptor create(String value, MatchType matchType) {
    return new PathDescriptorImpl(value, matchType);
  }
  
  String value();
  
  MatchType matchType();
  
  enum MatchType {
    REGEX, SIMPLE
  }
}
