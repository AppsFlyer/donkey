package com.appsflyer.donkey.server.exception;

public class ServerInitializationException extends Exception {
  
  private static final long serialVersionUID = 3714770258151648841L;
  
  public ServerInitializationException(String message) {
    super(message);
  }
  
  public ServerInitializationException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public ServerInitializationException(Throwable cause) {
    super(cause);
  }
}
