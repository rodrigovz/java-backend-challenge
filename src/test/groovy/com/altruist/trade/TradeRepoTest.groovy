package com.altruist.trade

import com.altruist.account.Account
import com.altruist.account.AccountRepo
import com.altruist.account.Address
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Shared
import spock.lang.Specification

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TradeRepoTest extends Specification {
    @Autowired
    TradeRepo tradeRepo

    @Autowired
    AccountRepo accountRepo

    @Shared
    Trade trade

    def setup() {
        tradeRepo.deleteAll()
        accountRepo.deleteAll()
    }

    def "Should change trade status"() {
        given: "a trade with an account is created"
        Account account = new Account(
                username: "username123",
                email: "email@example.com",
                address: new Address(
                    name: "Some Name",
                    street: "Some street",
                    city: "Some city",
                    state: "CA",
                    zipcode: 99999
                )
        )
        Trade trade = new Trade(
                quantity: 3,
                price: new BigDecimal("12.54"),
                symbol: "APPL",
                side: TradeSide.BUY,
                status: TradeStatus.SUBMITTED
        )
        trade.account = accountRepo.save(account)
        trade = tradeRepo.save(trade)

        when: "change trade status"
        int result =  tradeRepo.changeStatus(trade.tradeUuid, TradeStatus.CANCELLED)

        and: "reload trade entity"
        trade = tradeRepo.findByTradeUuid(trade.tradeUuid)

        then: "one row was modified"
        result == 1

        and: "status was changed to Cancelled"
        trade.status == TradeStatus.CANCELLED
    }
}
