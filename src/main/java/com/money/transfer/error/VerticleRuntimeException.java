package com.money.transfer.error;

public class VerticleRuntimeException extends RuntimeException {
    private int status;
    private Errors error;

  public VerticleRuntimeException(String message, int status, Errors error) {
    super(message);
    this.status = status;
    this.error = error;
  }

  public VerticleRuntimeException(int status, Errors error) {
    this.status = status;
    this.error = error;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public Errors getError() {
    return error;
  }

  public void setError(Errors error) {
    this.error = error;
  }
}
