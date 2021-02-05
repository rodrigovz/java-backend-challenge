package com.altruist.trade;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TradeSide {
  BUY, SELL;

  @JsonCreator
  public static TradeSide fromString(String value) {
    return TradeSide.valueOf(value.toUpperCase());
  }
}
