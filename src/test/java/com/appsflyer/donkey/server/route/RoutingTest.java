package com.appsflyer.donkey.server.route;

import com.appsflyer.donkey.TestUtil;
import com.appsflyer.donkey.server.ServerConfig;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.appsflyer.donkey.TestUtil.DEFAULT_PORT;

@Tag("integration")
@ExtendWith(VertxExtension.class)
public class RoutingTest extends AbstractRoutingTest {
  
  private static final RouteSupplier routeSupplier = new RouteSupplierImpl();
  
  @Override
  protected ServerConfig newServerConfig(Vertx vertx, RouteList routeList) {
    return ServerConfig
        .builder()
        .vertx(vertx)
        .instances(4)
        .serverOptions(new HttpServerOptions().setPort(DEFAULT_PORT))
        .routeList(routeList)
        .routeCreatorFactory(TestUtil::newRouteCreator)
        .build();
  }
  
  @Override
  protected RouteSupplier routeSupplier() {
    return routeSupplier;
  }
}
