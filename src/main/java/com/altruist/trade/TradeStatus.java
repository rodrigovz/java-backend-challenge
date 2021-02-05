package com.altruist.trade;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TradeStatus {
  SUBMITTED, CANCELLED, COMPLETED, FAILED;

  @JsonCreator
  public static TradeStatus fromString(String value) {
    return TradeStatus.valueOf(value.toUpperCase());
  }
}
