package com.esclient.ratingservice.constants;

/**
 * Constants for rating values used throughout the rating service. These represent the possible
 * rating values that can be assigned to mods.
 */
public final class RatingConstants {

  /** Rating value for 1-star rating */
  public static final int RATE_1 = 1;

  /** Rating value for 2-star rating */
  public static final int RATE_2 = 2;

  /** Rating value for 3-star rating */
  public static final int RATE_3 = 3;

  /** Rating value for 4-star rating */
  public static final int RATE_4 = 4;

  /** Rating value for 5-star rating */
  public static final int RATE_5 = 5;

  /** Minimum allowed rating value */
  public static final int MIN_RATING = RATE_1;

  /** Maximum allowed rating value */
  public static final int MAX_RATING = RATE_5;

  private RatingConstants() {
    // Prevent instantiation of utility class
  }

}
