package com.esclient.ratingservice.service;

public class RatingServiceException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public RatingServiceException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
