package com.money.transfer;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.money.transfer.config.DbConfig;
import com.money.transfer.config.ServiceBinder;
import com.money.transfer.controller.AccountController;
import com.money.transfer.controller.AccountController.ACCOUNT_API;
import com.money.transfer.service.AccountService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;

public class ServerVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> fut) {
    Injector injector = Guice.createInjector(new ServiceBinder());
    AccountService accountService = injector.getInstance(AccountService.class);
    AccountController accountController = new AccountController(vertx, accountService);
    DbConfig dbConfig = injector.getInstance(DbConfig.class);
    Router mainRouter = Router.router(vertx);
    Router router = accountController.getRouter();
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

    mainRouter.route().consumes("application/json");
    mainRouter.route().produces("application/json");
    mainRouter.get("/").handler(ctx -> ctx.response()
        .end("<h1>Money Transfer</h1>"));
    mainRouter.mountSubRouter(ACCOUNT_API.BASE, router);

    dbConfig.dropTable();
    dbConfig.initDb();

    vertx
        .createHttpServer()
        .requestHandler(mainRouter::accept)
        .listen(config().getInteger("http.port", 8080), result -> {
          if (result.succeeded()) {
            fut.complete();
          } else {
            fut.fail(result.cause());
          }
        });
  }

}
