package com.money.transfer.dao;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.money.transfer.dto.AccountDto;
import com.money.transfer.dto.generated.tables.Account;
import com.money.transfer.error.Errors;
import com.money.transfer.error.VerticleRuntimeException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class AccountDao {

  private static final Account ACC = Account.ACCOUNT;

  private String jdbcUrl;
  private String user;
  private String password;
  private DSLContext dsl;

  @Inject
  public AccountDao(@Named("JDBC URL") String jdbcUrl,
      @Named("JDBC user") String user,
      @Named("JDBC password") String password,
      DSLContext dsl
  ) {
    this.jdbcUrl = jdbcUrl;
    this.user = user;
    this.password = password;
    this.dsl = dsl;
  }


  public List<AccountDto> getAllAccounts() {
    return dsl.selectFrom(ACC)
        .fetch()
        .into(AccountDto.class);
  }

  public long getAmount(int accountId) {
    return dsl.selectFrom(ACC)
        .where(ACC.ID.equal(accountId))
        .fetchOptional(ACC.AMOUNT)
        .orElseThrow(() -> new VerticleRuntimeException(404, Errors.ACCOUNT_NOT_EXIST));
  }

  public void addAmount(int accountId, long amount) {
    long accountAmount = dsl.selectFrom(ACC)
        .where(ACC.ID.equal(accountId))
        .forUpdate()
        .fetchOptional(ACC.AMOUNT)
        .orElseThrow(() -> new VerticleRuntimeException(404, Errors.ACCOUNT_NOT_EXIST));

    int updatedRecord = dsl.update(ACC)
        .set(ACC.AMOUNT, accountAmount + amount)
        .where(ACC.ID.eq(accountId))
        .execute();
    if (updatedRecord == 0) {
      throw new VerticleRuntimeException(404, Errors.ACCOUNT_NOT_EXIST);
    }
  }

  public void transferMoney(int fromAccount, int toAccount, long amount) {
    try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
      conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
      DSL.using(conn, SQLDialect.H2)
          .transaction(configuration -> {
            if (fromAccount > toAccount) {
              long amountOnFromAccount = DSL.using(configuration)
                  .selectFrom(ACC)
                  .where(ACC.ID.eq(fromAccount))
                  .forUpdate()
                  .fetchOne(ACC.AMOUNT);

              long amountOnToAccount = DSL.using(configuration)
                  .selectFrom(ACC)
                  .where(ACC.ID.eq(toAccount))
                  .forUpdate()
                  .fetchOne(ACC.AMOUNT);

              if (amountOnFromAccount < amount) {
                throw new VerticleRuntimeException(400, Errors.NOT_ENOUGH_MONEY);
              }
              DSL.using(configuration)
                  .update(ACC)
                  .set(ACC.AMOUNT, amountOnFromAccount - amount)
                  .where(ACC.ID.eq(fromAccount))
                  .execute();

              DSL.using(configuration)
                  .update(ACC)
                  .set(ACC.AMOUNT, amountOnToAccount + amount)
                  .where(ACC.ID.eq(toAccount))
                  .execute();
            } else {
              long amountOnToAccount = DSL.using(configuration)
                  .selectFrom(ACC)
                  .where(ACC.ID.eq(toAccount))
                  .forUpdate()
                  .fetchOne(ACC.AMOUNT);

              long amountOnFromAccount = DSL.using(configuration)
                  .selectFrom(ACC)
                  .where(ACC.ID.eq(fromAccount))
                  .forUpdate()
                  .fetchOne(ACC.AMOUNT);

              if (amountOnFromAccount < amount) {
                throw new VerticleRuntimeException(400, Errors.NOT_ENOUGH_MONEY);
              }
              DSL.using(configuration)
                  .update(ACC)
                  .set(ACC.AMOUNT, amountOnToAccount + amount)
                  .where(ACC.ID.eq(toAccount))
                  .execute();

              DSL.using(configuration)
                  .update(ACC)
                  .set(ACC.AMOUNT, amountOnFromAccount - amount)
                  .where(ACC.ID.eq(fromAccount))
                  .execute();
            }
          });

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void createAccounts() {
    dsl.insertInto(ACC)
        .set(ACC.AMOUNT, 0L)
        .execute();
  }

}
