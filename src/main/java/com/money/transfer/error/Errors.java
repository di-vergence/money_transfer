package com.money.transfer.error;

public enum Errors {
  NOT_ENOUGH_MONEY("Transaction couldn't be proceed, not enough money"),
  ACCOUNT_NOT_EXIST("Account doesn't exist"),
  WRONG_MONEY("Amount should be positive number"),
  FAILED_TO_ENCODE("Failed to encode");

  private String description;

  Errors(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
