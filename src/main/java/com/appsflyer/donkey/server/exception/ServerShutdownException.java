package com.appsflyer.donkey.server.exception;

public class ServerShutdownException extends Exception
{
  public ServerShutdownException(String message)
  {
    super(message);
  }
  
  public ServerShutdownException(String message, Throwable cause)
  {
    super(message, cause);
  }
  
  public ServerShutdownException(Throwable cause)
  {
    super(cause);
  }
}
