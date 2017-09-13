package metroinsight.citadel;

import java.net.URL;
import java.net.URLClassLoader;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import metroinsight.citadel.data.DataRestApi;
import metroinsight.citadel.metadata.MetadataRestApi;

public class RestApiVerticle extends AbstractVerticle {

  protected ServiceDiscovery discovery;
  MetadataRestApi metadataRestApi ;
  DataRestApi dataRestApi;
 
  @Override
  public void start(Future<Void> fut){
    // Init service discovery. Future purpose
    discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));

    // REST API modules
    metadataRestApi = new MetadataRestApi (vertx);
    dataRestApi     = new DataRestApi();
    
    Router router = Router.router(vertx);
    
    // Main page. TODO
    router.route("/").handler(rc -> {
      HttpServerResponse response = rc.response();
      response
          .putHeader("content-type", "text/html")
          .end("<h1>Hello from my first Vert.x 3 application</h1>");
    });
    
    router.route("/*").handler(BodyHandler.create());
    
    // REST API routing for MetaData
    //router.route("/api/point*").handler(BodyHandler.create());
    router.post("/api/point").handler(metadataRestApi::createPoint);
    router.get("/api/point/:uuid").handler(metadataRestApi::getPoint);
    //router.route("/api/query*").handler(BodyHandler.create());
    router.post("/api/query").handler(metadataRestApi::queryPoint);

    // REST API routing for Data
    //router.route("/api/data*").handler(BodyHandler.create());
    router.post("/api/data").blockingHandler(dataRestApi::insertData);
    //router.route("/api/querydata*").handler(BodyHandler.create());
    router.post("/api/querydata").handler(dataRestApi::queryData);
    vertx
        .createHttpServer()
        .requestHandler(router::accept)
        .listen(
            config().getInteger("http.port", 8080),
            result -> {
              if (result.succeeded()) {
                fut.complete();
                System.out.println("REST_API_VERTICLE STARTED on " + Integer.toString(result.result().actualPort()));
              } else {
                fut.fail(result.cause());
                }
              }
        );
  }

}
