package com.appsflyer.donkey.server.exception;

public class ServerShutdownException extends Exception {
  
  private static final long serialVersionUID = -4242934312861054523L;
  
  public ServerShutdownException(String message) {
    super(message);
  }
  
  public ServerShutdownException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public ServerShutdownException(Throwable cause) {
    super(cause);
  }
}
