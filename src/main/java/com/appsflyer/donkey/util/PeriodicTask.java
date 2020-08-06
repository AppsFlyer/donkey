package com.appsflyer.donkey.util;

public interface PeriodicTask
{
  PeriodicTask start();
  
  PeriodicTask cancel();
}
