package com.altruist.trade;

import lombok.Data;

@Data
public class TradeResponseDto {
  public String symbol;
  public Integer quantity;
  public String price;
  public String side;
  public String status;
  public String accountId;
}
