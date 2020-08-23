package com.appsflyer.donkey.route;

public class PathDescriptorImpl implements PathDescriptor {
  private final String value;
  private final MatchType matchType;
  
  public PathDescriptorImpl(String value, MatchType matchType)
  {
    this.value = value;
    this.matchType = matchType;
  }
  
  PathDescriptorImpl(String value)
  {
    this(value, MatchType.SIMPLE);
  }
  
  @Override
  public String value()
  {
    return value;
  }
  
  @Override
  public MatchType matchType()
  {
    return matchType;
  }
  
}
