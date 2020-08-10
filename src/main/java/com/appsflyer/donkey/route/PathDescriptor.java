package com.appsflyer.donkey.route;

public class PathDescriptor
{
  private final String value;
  private final MatchType matchType;
  
  public PathDescriptor(String value, MatchType matchType)
  {
    this.value = value;
    this.matchType = matchType;
  }
  
  PathDescriptor(String value)
  {
    this(value, MatchType.SIMPLE);
  }
  
  String value()
  {
    return value;
  }
  
  MatchType matchType()
  {
    return matchType;
  }
  
  public enum MatchType
  {
    REGEX, SIMPLE
  }
}
