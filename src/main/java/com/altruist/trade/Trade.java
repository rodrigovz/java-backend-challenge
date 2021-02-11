package com.altruist.trade;

import com.altruist.account.Account;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "trade", schema = "trade")
public class Trade {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public UUID tradeUuid;

  public String symbol;

  public Integer quantity;

  public BigDecimal price;

  @Enumerated(EnumType.STRING)
  public TradeSide side;

  @Enumerated(EnumType.STRING)
  public TradeStatus status;

  @ManyToOne
  @JoinColumn(name="account_uuid")
  public Account account;
}
