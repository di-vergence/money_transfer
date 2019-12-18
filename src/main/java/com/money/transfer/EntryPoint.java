package com.money.transfer;

import com.google.inject.Inject;
import com.money.transfer.config.ServiceBinder;
import com.money.transfer.dao.AccountDao;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class EntryPoint {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    deploy(vertx, ServiceLauncher.class, new DeploymentOptions());
  }

  private static Future<Void> deploy(Vertx vertx, Class verticle,
      DeploymentOptions opts) {
    Future<Void> done = Future.future();
    String deploymentName = "java-guice:" + verticle.getName();
    JsonObject config = new JsonObject()
        .put("guice_binder", ServiceBinder.class.getName());

    opts.setConfig(config);
    vertx.deployVerticle(deploymentName, opts, res -> {
      if (res.failed()) {
        System.out.println("Failed to deploy verticle  cause :" + res.cause());
        done.fail(res.cause());
      } else {
        System.out.println("Deployed: " + verticle );
        done.complete();
      }
    });

    return done;
  }

  public static class ServiceLauncher extends AbstractVerticle {

    @Override
    public void start(Future<Void> done) {
      int WORKER_POOL_SIZE = 10;

      DeploymentOptions opts =
          new DeploymentOptions()
              .setWorkerPoolSize(WORKER_POOL_SIZE);

      String verticle = ServerVerticle.class.getName();

      vertx.deployVerticle(verticle, opts, res -> {
        if (res.failed()) {
          System.out.println("Failed to deploy verticle  cause :" + res.cause());
          done.fail(res.cause());
        } else {
          System.out.println("Deployed: " + verticle);
          done.complete();
        }
      });
    }
  }

}
