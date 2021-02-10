package com.altruist.trade.dto;

import com.altruist.account.Account;
import com.altruist.trade.TradeSide;
import com.altruist.trade.TradeStatus;
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
