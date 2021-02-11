package com.altruist.trade.dto;

import lombok.Data;

@Data
public class TradeResponseDto {
  public String tradeUuid;
  public String symbol;
  public Integer quantity;
  public String price;
  public String side;
  public String status;
  public String accountId;
}
