package com.appsflyer.donkey.route;

public interface PathDescriptor {
  
  static PathDescriptor create(String value) {
    return create(value, MatchType.SIMPLE);
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
