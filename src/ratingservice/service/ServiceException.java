package ratingservice.service;

public class ServiceException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ServiceException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
