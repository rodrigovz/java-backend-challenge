package com.altruist.trade

import com.altruist.account.Account
import com.altruist.trade.dto.TradeDto
import com.altruist.trade.dto.TradeResponseDto
import spock.lang.Specification

class TradeSrvTest extends Specification {
    TradeRepo mockTradeRepo = Mock()
    TradeSrv srv = new TradeSrv(mockTradeRepo)

    def "Should save a trade"() {
        given: "a TradeDto"
        TradeDto tradeDto = new TradeDto(
                "symbol": "APPL",
                "quantity": 3,
                "price": new BigDecimal("28.34"),
                "side": TradeSide.BUY,
                "status": TradeStatus.SUBMITTED,
                account: new Account()
        )

        UUID expectedTradeId = UUID.randomUUID()

        when:
        srv.createTrade(tradeDto)

        then: "the trade is saved"
        1 * mockTradeRepo.save(_) >> { Trade arg ->
            with(arg){
                symbol == tradeDto.symbol
                quantity == tradeDto.quantity
                price == tradeDto.price
                side == tradeDto.side
                status == tradeDto.status
                account == tradeDto.account
            }

            arg.tradeUuid = expectedTradeId
            arg
        }
    }

    def "Should return a TradeResponseDto"() {
        given: "a trade"
        UUID expectedTradeId = UUID.randomUUID()
        Trade trade = new Trade(
                tradeUuid: expectedTradeId,
                "symbol": "APPL",
                "quantity": 3,
                "price": new BigDecimal("28.34"),
                "side": TradeSide.BUY,
                "status": TradeStatus.SUBMITTED,
                account: new Account(
                        accountUuid: UUID.randomUUID()
                )
        )

        when:
        TradeResponseDto responseDto = srv.getTradeResponseDto(expectedTradeId)

        then: 'expect a trade is found'
        1 * mockTradeRepo.findByTradeUuid(expectedTradeId) >> trade

        and:
        responseDto.symbol == trade.symbol
        responseDto.quantity == trade.quantity
        responseDto.price == "28.34"
        responseDto.side == trade.side as String
        responseDto.status == trade.status as String
        responseDto.accountId == trade.account.accountUuid as String
    }
}
