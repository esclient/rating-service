package ratingservice.constants;

@SuppressWarnings({"checkstyle:HideUtilityClassConstructor", "PMD.UseUtilityClass"})
public final class Constants {

  public static final int RATE_1 = 1;
  public static final int RATE_2 = 2;
  public static final int RATE_3 = 3;
  public static final int RATE_4 = 4;
  public static final int RATE_5 = 5;

  public static final int MIN_RATING = RATE_1;
  public static final int MAX_RATING = RATE_5;

  private Constants() {
    throw new UnsupportedOperationException("Utility class");
  }
}

