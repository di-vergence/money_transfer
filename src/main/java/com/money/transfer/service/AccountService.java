package com.money.transfer.service;

import com.google.inject.Inject;
import com.money.transfer.dao.AccountDao;
import com.money.transfer.dto.AccountDto;
import java.util.List;

public class AccountService {

  private AccountDao accountDao;

  @Inject
  public AccountService(AccountDao accountDao) {
    this.accountDao = accountDao;
  }

  public List<AccountDto> getAllAccounts() {
    return accountDao.getAllAccounts();
  }

  public long getAmount(int accountId) {
    return accountDao.getAmount(accountId);
  }

  public void addMoney(int accountId, long amount) {
    accountDao.addAmount(accountId, amount);
  }

  public void transferMoney(int fromAccount, int toAccount, long amount) {
    accountDao.transferMoney(fromAccount, toAccount, amount);
  }

  public void createAccount() {
    accountDao.createAccounts();
  }
}
