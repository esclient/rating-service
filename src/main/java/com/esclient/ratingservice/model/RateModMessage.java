package com.esclient.ratingservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rates")
public class RateModMessage {
  @Id private long mod_id;
  private long author_id;
  private int rate;

  // Default constructor required by JPA
  public RateModMessage() {}

  public RateModMessage(long mod_id, long author_id, int rate) {
    this.mod_id = mod_id;
    this.author_id = author_id;
    this.rate = rate;
  }

  public long getModId() {
    return mod_id;
  }

  public long getAuthorId() {
    return author_id;
  }

  public int getRate() {
    return rate;
  }

  // Setters for JPA
  public void setModId(long mod_id) {
    this.mod_id = mod_id;
  }

  public void setAuthorId(long author_id) {
    this.author_id = author_id;
  }

  public void setRate(int rate) {
    this.rate = rate;
  }
}
