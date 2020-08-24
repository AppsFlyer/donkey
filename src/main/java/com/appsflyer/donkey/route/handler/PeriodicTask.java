package com.appsflyer.donkey.route.handler;

public interface PeriodicTask
{
  PeriodicTask start();
  
  PeriodicTask cancel();
}

