package com.agilesprintplus.agilesprint.exception;

public class PasswordChangeRequiredException extends RuntimeException {
  public PasswordChangeRequiredException(String message) {
    super(message);
  }
}