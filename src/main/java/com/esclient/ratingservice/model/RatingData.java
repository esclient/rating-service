package com.esclient.ratingservice.model;

public final class RatingData {
  private final long totalRates;
  private final long dislikes;
  private final long likes;

  public RatingData(final long totalRates, final long dislikes, final long likes) {
    this.totalRates = totalRates;
    this.dislikes = dislikes;
    this.likes = likes;
  }

  public long getTotalRates() {
    return totalRates;
  }

  public long getDislikes() {
    return dislikes;
  }

  public long getLikes() {
    return likes;
  }
}
