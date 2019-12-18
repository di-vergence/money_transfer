package com.money.transfer.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Inject;
import com.money.transfer.ServerVerticle;
import com.money.transfer.TestBase;
import com.money.transfer.dao.AccountDao;
import com.money.transfer.dto.ErrorResponse;
import com.money.transfer.error.Errors;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import java.io.IOException;
import java.net.ServerSocket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class AccountControllerTest extends TestBase {

  private Vertx vertx;
  private int port;
  WebClient client;

  @Inject
  private AccountDao accountDao;

  @Before
  public void setUp(TestContext context) throws IOException {
    vertx = Vertx.vertx();
    ServerSocket socket = new ServerSocket(0);
    port = socket.getLocalPort();
    socket.close();
    client = WebClient.create(vertx);
    DeploymentOptions options = new DeploymentOptions()
        .setConfig(new JsonObject().put("http.port", port)
        );
    vertx.deployVerticle(ServerVerticle.class.getName(), options, context.asyncAssertSuccess());
  }

  @Test
  public void whenAddMoneyRequestsThenShouldAddMoneyToAccount(TestContext context) {
    final Async async = context.async();
    long moneyBeforeRequest = accountDao.getAmount(1);
    long transferAmount = 13;
    client
        .put(port, "localhost", "/accounts/1/amount")
        .sendJson(new JsonObject()
                .put("amount", transferAmount),
            ar -> {
              assertThat(ar.result().statusCode()).isEqualTo(200);
              assertThat(ar.result().bodyAsString()).isEqualTo("null");
              assertThat(accountDao.getAmount(1)).isEqualTo(moneyBeforeRequest + transferAmount);
              async.complete();
            });
    int i = 3;
  }

  @Test
  public void whenAddMoneyRequestOnAbsentAccount_ShouldThrowException(TestContext context) {
    final Async async = context.async();
    client
        .put(port, "localhost", "/accounts/3/amount")
        .sendJson(new JsonObject()
                .put("amount", 13),
            ar -> {
              assertThat(ar.result().statusCode()).isEqualTo(404);
              assertThat(ar.result().bodyAsJson(ErrorResponse.class).getError())
                  .isEqualTo(Errors.ACCOUNT_NOT_EXIST.getDescription());
              async.complete();
            });
  }

  @Test
  public void whenAddNegativeMoneyShouldThrowException(TestContext context) {
    final Async async = context.async();
    client
        .put(port, "localhost", "/accounts/1/amount")
        .sendJson(new JsonObject()
                .put("amount", -13),
            ar -> {
              assertThat(ar.result().statusCode()).isEqualTo(400);
              assertThat(ar.result().bodyAsJson(ErrorResponse.class).getError())
                  .isEqualTo(Errors.WRONG_MONEY.getDescription());
              async.complete();
            });
  }

  @Test
  public void whenTransferNegativeMoneyShouldThrowException(TestContext context) {
    final Async async = context.async();
    client
        .put(port, "localhost", "/accounts/1/transfer")
        .sendJson(new JsonObject()
                .put("amount", -13),
            ar -> {
              assertThat(ar.result().statusCode()).isEqualTo(400);
              assertThat(ar.result().bodyAsJson(ErrorResponse.class).getError())
                  .isEqualTo(Errors.WRONG_MONEY.getDescription());
              async.complete();
            });
  }

  @Test
  public void whenGetAccountRequestShouldReturnAllAccounts(TestContext context) {
    final Async async = context.async();
    client
        .get(port, "localhost", "/accounts/")
        .send(ar -> {
          assertThat(ar.result().statusCode()).isEqualTo(200);
          assertThat(ar.result().bodyAsString())
              .isEqualTo(Json.encode(accountDao.getAllAccounts()));
          async.complete();
        });
  }

  @Test
  public void whenTransferRequestShouldTransferMoney(TestContext context) {
    final Async async = context.async();
    long moneyOnFromAccountBeforeTransaction = accountDao.getAmount(1);
    long moneyOnToAccountBeforeTransaction = accountDao.getAmount(2);
    long transferAmount = 100;
    client
        .put(port, "localhost", "/accounts/1/transfer")
        .sendJson(new JsonObject().put("toAccount", 2).put("amount", transferAmount),
            ar -> {
              assertThat(ar.result().statusCode()).isEqualTo(200);
              assertThat(accountDao.getAmount(1))
                  .isEqualTo(moneyOnFromAccountBeforeTransaction - transferAmount);
              assertThat(accountDao.getAmount(2))
                  .isEqualTo(moneyOnToAccountBeforeTransaction + transferAmount);
              async.complete();
            });
  }

  @Test
  public void whenTransferMoneyRequestAndNotEnoughMoney_ShouldThrowException(TestContext context) {
    final Async async = context.async();
    long moneyOnFromAccountBeforeTransaction = accountDao.getAmount(1);
    long moneyOnToAccountBeforeTransaction = accountDao.getAmount(2);
    long transferAmount = 2000;
    client
        .put(port, "localhost", "/accounts/1/transfer")
        .sendJson(new JsonObject().put("toAccount", 2).put("amount", transferAmount),
            ar -> {
              assertThat(ar.result().statusCode()).isEqualTo(400);
              assertThat(ar.result().bodyAsJson(ErrorResponse.class).getError())
                  .isEqualTo(Errors.NOT_ENOUGH_MONEY.getDescription());
              assertThat(accountDao.getAmount(1)).isEqualTo(moneyOnFromAccountBeforeTransaction);
              assertThat(accountDao.getAmount(2)).isEqualTo(moneyOnToAccountBeforeTransaction);
              async.complete();
            });
  }

  @Test
  public void whenCreateAccountRequestShouldCreateAccount(TestContext context) {
    final Async async = context.async();
    int accountsSize = accountDao.getAllAccounts().size();
    client
        .post(port, "localhost", "/accounts/account")
        .sendJson(new JsonObject(),
            ar -> {
              assertThat(ar.result().statusCode()).isEqualTo(200);
              assertThat(accountDao.getAllAccounts().size()).isGreaterThan(accountsSize);
              async.complete();
            });
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }
}