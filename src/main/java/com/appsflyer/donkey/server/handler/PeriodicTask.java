package com.appsflyer.donkey.server.handler;

public interface PeriodicTask
{
  PeriodicTask start();
  
  PeriodicTask cancel();
}

