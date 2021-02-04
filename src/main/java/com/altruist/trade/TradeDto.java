package com.altruist.trade;

import com.altruist.account.Account;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradeDto {
  public Account account;
  public String symbol;
  public Integer quantity;
  public BigDecimal price;
  public TradeSide side;
  public TradeStatus status;
}
