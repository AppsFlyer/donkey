package com.appsflyer.donkey.exception;

public class ServerInitializationException extends Exception
{
  public ServerInitializationException(String message)
  {
    super(message);
  }
  
  public ServerInitializationException(String message, Throwable cause)
  {
    super(message, cause);
  }
  
  public ServerInitializationException(Throwable cause)
  {
    super(cause);
  }
}
