package com.money.transfer.controller;

import com.money.transfer.dto.AddMoneyRequest;
import com.money.transfer.dto.TransferRequest;
import com.money.transfer.error.Errors;
import com.money.transfer.error.VerticleRuntimeException;
import com.money.transfer.service.AccountService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class AccountController {

  private Vertx vertx;
  private AccountService accountService;
  private Router router;

  public AccountController(Vertx vertx, AccountService accountService) {
    this.vertx = vertx;
    this.accountService = accountService;
  }

  public Router getRouter() {
    if (router == null) {
      router = Router.router(vertx);
      router.route().handler(BodyHandler.create());
      router.get(ACCOUNT_API.GET_ALL_ACCOUNTS).handler(this::getAllAccounts);
      router.put(ACCOUNT_API.ADD_MONEY).handler(this::addMoneyToAccount);
      router.get(ACCOUNT_API.SHOW_MONEY).handler(this::getAccountMoney);
      router.put(ACCOUNT_API.TRANSFER_MONEY).handler(this::transferMoney);
      router.post(ACCOUNT_API.CREATE_ACCOUNT).handler(this::createAccount);

      router.route().failureHandler(ctx -> {
        if (ctx.failure() instanceof VerticleRuntimeException) {
          VerticleRuntimeException exception = (VerticleRuntimeException) ctx.failure();

          final JsonObject error = new JsonObject()
              .put("timestamp", System.nanoTime())
              .put("status", exception.getStatus())
              .put("error", exception.getError().getDescription())
              .put("path", ctx.normalisedPath());

          if (exception.getMessage() != null) {
            error.put("message", exception.getMessage());
          }

          ctx.response().setStatusCode(exception.getStatus());
          ctx.response().end(error.encode());
        } else {
          ctx.response().setStatusCode(500);
          ctx.response().end();
        }

      });
    }
    return router;
  }

  private void getGreetings(RoutingContext ctx) {
    ;
  }

  private void createAccount(RoutingContext ctx) {
    vertx.executeBlocking(
        fut -> {
          accountService.createAccount();
          fut.complete("success");
        },
        false,
        res -> handleAsyncResponse(res, ctx)
    );

  }

  private void getAllAccounts(RoutingContext ctx) {
    vertx.executeBlocking(
        fut -> fut.complete(accountService.getAllAccounts()),
        false,
        res -> handleAsyncResponse(res, ctx)
    );
  }

  private void getAccountMoney(RoutingContext ctx) {
    int accountId = Integer.parseInt(ctx.request().getParam("accountId"));
    vertx.executeBlocking(
        fut -> fut.complete(accountService.getAmount(accountId)),
        false,
        res -> handleAsyncResponse(res, ctx)
    );
  }

  private void addMoneyToAccount(RoutingContext ctx) {
    int accountId = Integer.parseInt(ctx.request().getParam("accountId"));
    AddMoneyRequest addMoneyRequest = Json
        .decodeValue(ctx.getBodyAsString(), AddMoneyRequest.class);
    if (addMoneyRequest.getAmount() < 1) {
      throw new VerticleRuntimeException(400, Errors.WRONG_MONEY);
    }
    vertx.executeBlocking(
        fut -> {
          accountService.addMoney(accountId, addMoneyRequest.getAmount());
          fut.complete();
        },
        false,
        res -> handleAsyncResponse(res, ctx)
    );
  }

  private void transferMoney(RoutingContext ctx) {
    int fromAccount = Integer.parseInt(ctx.request().getParam("accountId"));
    TransferRequest transferRequest = Json
        .decodeValue(ctx.getBodyAsString(), TransferRequest.class);
    if (transferRequest.getAmount() < 1) {
      throw new VerticleRuntimeException(400, Errors.WRONG_MONEY);
    }
    vertx.executeBlocking(
        fut -> {
          accountService.transferMoney(fromAccount, transferRequest.getToAccount(),
              transferRequest.getAmount());
          fut.complete();
        },
        false,
        res -> handleAsyncResponse(res, ctx)
    );
  }

  private void handleAsyncResponse(AsyncResult<Object> res, RoutingContext ctx) {
    if (res.succeeded()) {
      try {
        ctx.response().end(Json.encode(res.result()));
      } catch (EncodeException e) {
        ctx.fail(new VerticleRuntimeException(500, Errors.FAILED_TO_ENCODE));
      }
    } else {
      ctx.fail(res.cause());
    }
  }

  public static class ACCOUNT_API {

    public static final String BASE = "/accounts";

    public static final String GET_ALL_ACCOUNTS = "/";
    public static final String CREATE_ACCOUNT = "/account";
    public static final String ADD_MONEY = "/:accountID/amount";
    public static final String SHOW_MONEY = "/:accountID/amount";
    public static final String TRANSFER_MONEY = "/:accountID/transfer";
  }
}
