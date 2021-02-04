package com.altruist.trade

import com.altruist.account.Account
import com.altruist.account.AccountRepo
import com.altruist.account.AccountSrv
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import spock.lang.Specification
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

import static org.hamcrest.Matchers.containsString
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [TradeCtrl])
class TradeCtrlTest extends Specification {
    @Autowired
    MockMvc mvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    AccountSrv mockAccountSrv

    @Autowired
    TradeSrv mockTradeSrv

    def "Should accept trade requests"() {
        given: "a trade request"
        Account account = new Account()
        TradeRequestDto req = new TradeRequestDto(
                "symbol": "APPL",
                "quantity": 3,
                "price": new BigDecimal("28.34"),
                "side": "buy",
                "status": "submitted"
        )
        TradeDto dto = new TradeDto(
                "symbol": "APPL",
                "quantity": 3,
                "price": new BigDecimal("28.34"),
                "side": TradeSide.BUY,
                "status": TradeStatus.SUBMITTED,
                account: account
        )
        UUID accountId = UUID.randomUUID()
        UUID expectedId = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(post("/accounts/$accountId/trades")
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "account is found"
        1 * mockAccountSrv.findAccount(accountId) >> account

        and: "trade is saved"
        1 * mockTradeSrv.createTrade(dto) >> expectedId

        and: "a Created response is returned"
        results.andExpect(status().isCreated())

        and: "the order ID is returned"
        results.andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/accounts/$accountId/trades/$expectedId")))
        results.andExpect(content().json("""{"id":"$expectedId"}"""))
    }

    @Unroll
    def "Should validate for missing field"() {
        given: "a trade request"
        TradeRequestDto req = new TradeRequestDto(
                "symbol": symbol,
                "quantity": quantity,
                "price": price,
                "side": side,
                "status": status
        )
        UUID accountId = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(post("/accounts/$accountId/trades")
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "response is conflict"
        results.andExpect(status().isBadRequest())

        and: "expect an error message"
        results.andExpect(jsonPath(field).value(error))

        where:
        symbol | quantity | price                   | side  | status      | field        | error
        null   | 4        | new BigDecimal("28.56") | "buy" | "submitted" | '$.symbol'   | "Symbol is mandatory"
        "APPL" | null     | new BigDecimal("28.56") | "buy" | "submitted" | '$.quantity' | "Quantity is mandatory"
        "APPL" | 4        | null                    | "buy" | "submitted" | '$.price'    | "Price is mandatory"
        "APPL" | 4        | new BigDecimal("28.56") | null  | "submitted" | '$.side'     | "Side is mandatory"
        "APPL" | 4        | new BigDecimal("28.56") | "buy" | null        | '$.status'   | "Status is mandatory"
    }

    def "Should validate existing account"() {
        given: "a trade request"
        TradeRequestDto req = new TradeRequestDto(
                "symbol": "APPL",
                "quantity": 3,
                "price": new BigDecimal("28.34"),
                "side": "buy",
                "status": "submitted"
        )
        UUID accountId = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(post("/accounts/$accountId/trades")
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "the request is processed"
        1 * mockAccountSrv.findAccount(accountId) >> null

        and: "expect conflict status"
        results.andExpect(status().isConflict())

        and: "expect an error message"
        results.andExpect(jsonPath('$.error').value("AccountId not found"))
    }

    def "Should validate wrong format for accountId"() {
        given: "a trade request"
        TradeRequestDto req = new TradeRequestDto(
                "symbol": "APPL",
                "quantity": 3,
                "price": new BigDecimal("28.34"),
                "side": "buy",
                "status": "submitted"
        )
        String accountId = "1234"

        when: "the request is submitted"
        ResultActions results = mvc.perform(post("/accounts/$accountId/trades")
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "expect bad request status"
        results.andExpect(status().isBadRequest())

        and: "expect an error message"
        results.andExpect(jsonPath('$.error').value("Wrong accountId format"))
    }

    @Unroll
    def "Should validate positive number"() {
        given: "a trade request"
        TradeRequestDto req = new TradeRequestDto(
                "symbol": "APPL",
                "quantity": quantity,
                "price": price,
                "side": "buy",
                "status": "submitted"
        )
        UUID accountId = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(post("/accounts/$accountId/trades")
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "expect bad request status"
        results.andExpect(status().isBadRequest())

        and: "expect an error message"
        results.andExpect(jsonPath(field).value(error))

        where:
        price                   | quantity | field        | error
        new BigDecimal("28.46") | 0        | '$.quantity' | "Quantity must be greater than zero"
        new BigDecimal("0.0")   | 4        | '$.price'    | "Price must be greater than zero"
    }

    @TestConfiguration
    static class TestConfig {
        DetachedMockFactory factory = new DetachedMockFactory()

        @Bean
        AccountSrv accountSrv() {
            factory.Mock(AccountSrv)
        }

        @Bean
        AccountRepo accountRepo() {
            factory.Mock(AccountRepo)
        }

        @Bean
        TradeSrv tradeSrv() {
            factory.Mock(TradeSrv)
        }

        @Bean
        TradeRepo TradeRepo() {
            factory.Mock(TradeRepo)
        }
    }
}
