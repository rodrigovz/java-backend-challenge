package com.altruist.trade;

import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class TradeSrv {

    private final TradeRepo tradeRepo;

    public TradeSrv(TradeRepo tradeRepo) {
        this.tradeRepo = tradeRepo;
    }

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

    public TradeResponseDto getTradeResponseDto(UUID tradeUuid) {
        TradeResponseDto responseDto = new TradeResponseDto();
        Trade trade = tradeRepo.findByTradeUuid(tradeUuid);
        if (trade == null) {
            return null;
        }
        responseDto.symbol = trade.symbol;
        responseDto.quantity = trade.quantity;
        responseDto.price = NumberFormat.getNumberInstance(Locale.US).format(trade.price.setScale(2));
        responseDto.status = trade.status.toString();
        responseDto.side = trade.side.toString();
        responseDto.accountId = trade.account.accountUuid.toString();
        return responseDto;
    }

    public boolean changeStatusToCancelled(Trade trade) {
        return tradeRepo.changeStatus(trade.tradeUuid, TradeStatus.CANCELLED) > 0;
    }

    public Trade findTrade(UUID tradeUuid) {
        return tradeRepo.findByTradeUuid(tradeUuid);
    }
}
