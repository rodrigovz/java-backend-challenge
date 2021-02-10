package com.altruist.trade;

import com.altruist.account.Account;
import com.altruist.trade.dto.TradeDto;
import com.altruist.trade.dto.TradeResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

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
        Trade trade = tradeRepo.findByTradeUuid(tradeUuid);
        if (trade == null) {
            return null;
        }
        return getTradeResponseDto(trade);
    }

    public List<TradeResponseDto> listTradesByAccount(Account account, int page, int size) {
        List<Trade> trades = tradeRepo.findAllByAccount(account, PageRequest.of(page, size));
        List<TradeResponseDto> result = trades.stream()
                .map(trade -> getTradeResponseDto(trade))
                .collect(Collectors.toList());
        return result;
    }

    public boolean changeStatusToCancelled(Trade trade) {
        return tradeRepo.changeStatus(trade.tradeUuid, TradeStatus.CANCELLED) > 0;
    }

    public Trade findTrade(UUID tradeUuid) {
        return tradeRepo.findByTradeUuid(tradeUuid);
    }

    //
    // Private
    //
    private TradeResponseDto getTradeResponseDto(Trade trade) {
        TradeResponseDto responseDto = new TradeResponseDto();
        responseDto.tradeUuid = trade.tradeUuid.toString();
        responseDto.symbol = trade.symbol;
        responseDto.quantity = trade.quantity;
        responseDto.price = NumberFormat.getNumberInstance(Locale.US).format(trade.price.setScale(2));
        responseDto.status = trade.status.toString();
        responseDto.side = trade.side.toString();
        responseDto.accountId = trade.account.accountUuid.toString();
        return responseDto;
    }
}
