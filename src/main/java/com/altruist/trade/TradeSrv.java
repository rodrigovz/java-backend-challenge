package com.altruist.trade;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TradeSrv {

  private final TradeRepo tradeRepo;

  public TradeSrv(TradeRepo tradeRepo) { this.tradeRepo = tradeRepo; }

  public UUID createTrade(TradeDto tradeDto) {
    Trade trade = new Trade();
    trade.account = tradeDto.account;
    trade.symbol = tradeDto.symbol;
    trade.side = tradeDto.side;
    trade.quantity = tradeDto.quantity;
    trade.price = tradeDto.price;
    trade.status = tradeDto.status;
    trade = tradeRepo.save(trade);
    return trade.tradeUuid;
  }
}
