package ratingservice.model;

public final class Data {
  private final long totalRates;
  private final long rate1Count;
  private final long rate2Count;
  private final long rate3Count;
  private final long rate4Count;
  private final long rate5Count;

  public Data(
      final long totalRates,
      final long rate1Count,
      final long rate2Count,
      final long rate3Count,
      final long rate4Count,
      final long rate5Count) {
    this.totalRates = totalRates;
    this.rate1Count = rate1Count;
    this.rate2Count = rate2Count;
    this.rate3Count = rate3Count;
    this.rate4Count = rate4Count;
    this.rate5Count = rate5Count;
  }

  public long getTotalRates() {
    return totalRates;
  }

  public long getRate1Count() {
    return rate1Count;
  }

  public long getRate2Count() {
    return rate2Count;
  }

  public long getRate3Count() {
    return rate3Count;
  }

  public long getRate4Count() {
    return rate4Count;
  }

  public long getRate5Count() {
    return rate5Count;
  }
}
