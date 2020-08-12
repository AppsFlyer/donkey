package com.appsflyer.donkey;

import io.vertx.core.net.SocketAddress;

public final class TestUtil
{
  public static final int DEFAULT_PORT = 16969;
  
  public static SocketAddress getDefaultAddress()
  {
    return SocketAddress.inetSocketAddress(DEFAULT_PORT, "localhost");
  }
}
