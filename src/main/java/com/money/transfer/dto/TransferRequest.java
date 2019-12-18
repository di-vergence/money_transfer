package com.money.transfer.dto;

public class TransferRequest {
  private int toAccount;
  private int amount;

  public int getToAccount() {
    return toAccount;
  }

  public void setToAccount(int toAccount) {
    this.toAccount = toAccount;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }
}
