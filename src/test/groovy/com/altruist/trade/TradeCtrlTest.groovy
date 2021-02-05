package com.altruist.trade

import com.altruist.account.Account
import com.altruist.account.AccountRepo
import com.altruist.account.AccountSrv
import com.altruist.core.snippets.TradeSnippetsFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
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
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
import static org.springframework.restdocs.payload.PayloadDocumentation.*
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [TradeCtrl])
@AutoConfigureRestDocs
class TradeCtrlTest extends Specification {
    @Autowired
    MockMvc mvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    AccountSrv mockAccountSrv

    @Autowired
    TradeSrv mockTradeSrv

    private TradeSnippetsFactory snippetPathFactory = new TradeSnippetsFactory()

    def "Should accept post trade requests"() {
        given: "a trade request"
        Account account = new Account()
        TradeRequestDto req = new TradeRequestDto(
                "symbol": "APPL",
                "quantity": 3,
                "price": new BigDecimal("28.34"),
                "side": TradeSide.BUY,
                "status": TradeStatus.SUBMITTED
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
        ResultActions results = mvc.perform(post('/accounts/{accountId}/trades', accountId)
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

        and: "generate docs"
        results.andDo(document(snippetPathFactory.create("1.0.0", 'post-success'),
                pathParameters(
                        parameterWithName("accountId").description("Account Id")),
                requestFields(
                        fieldWithPath("symbol").description("Required. Symbol of the Trade"),
                        fieldWithPath("quantity").description("Required. Number of trades"),
                        fieldWithPath("price").description("Required. Should be a decimal number represented as String"),
                        fieldWithPath("side").description("Required. Possible values: buy, sell"),
                        fieldWithPath("status").description("Required. Possible values: submitted, cancelled, completed, failed")),
                responseFields(
                        fieldWithPath("id").description("Id of the Trade"))
        ))
    }

    @Unroll
    def "Should validate missing field for post request"() {
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
        ResultActions results = mvc.perform(post('/accounts/{accountId}/trades', accountId)
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "response is conflict"
        results.andExpect(status().isBadRequest())

        and: "expect an error message"
        results.andExpect(jsonPath(field).value(error))

        and: "generate docs"
        results.andDo(document(snippetPathFactory.create("1.0.0", 'post-bad-requests')))

        where:
        symbol | quantity | price                   | side          | status                | field        | error
        null   | 4        | new BigDecimal("28.56") | TradeSide.BUY | TradeStatus.SUBMITTED | '$.symbol'   | "Symbol is mandatory"
        "APPL" | null     | new BigDecimal("28.56") | TradeSide.BUY | TradeStatus.SUBMITTED | '$.quantity' | "Quantity is mandatory"
        "APPL" | 4        | null                    | TradeSide.BUY | TradeStatus.SUBMITTED | '$.price'    | "Price is mandatory"
        "APPL" | 4        | new BigDecimal("28.56") | null          | TradeStatus.SUBMITTED | '$.side'     | "Side is mandatory"
        "APPL" | 4        | new BigDecimal("28.56") | TradeSide.BUY | null                  | '$.status'   | "Status is mandatory"
    }

    def "Should validate existing account for post request"() {
        given: "a trade request"
        TradeRequestDto req = new TradeRequestDto(
                "symbol": "APPL",
                "quantity": 3,
                "price": new BigDecimal("28.34"),
                "side": TradeSide.BUY,
                "status": TradeStatus.SUBMITTED
        )
        UUID accountId = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(post('/accounts/{accountId}/trades', accountId)
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "the request is processed"
        1 * mockAccountSrv.findAccount(accountId) >> null

        and: "expect conflict status"
        results.andExpect(status().isNotFound())

        and: "expect an error message"
        results.andExpect(jsonPath('$.error').value("AccountId not found"))

        and: "generate docs"
        results.andDo(document(snippetPathFactory.create("1.0.0", 'post-missing-account')))
    }

    def "Should validate wrong accountId format for post request"() {
        given: "a trade request"
        TradeRequestDto req = new TradeRequestDto(
                "symbol": "APPL",
                "quantity": 3,
                "price": new BigDecimal("28.34"),
                "side": TradeSide.BUY,
                "status": TradeStatus.SUBMITTED
        )
        String accountId = "1234"

        when: "the request is submitted"
        ResultActions results = mvc.perform(post('/accounts/{accountId}/trades', accountId)
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "expect bad request status"
        results.andExpect(status().isBadRequest())

        and: "generate docs"
        results.andDo(document(snippetPathFactory.create("1.0.0", 'post-invalid-account-format')))
    }

    @Unroll
    def "Should validate positive number for post request"() {
        given: "a trade request"
        TradeRequestDto req = new TradeRequestDto(
                "symbol": "APPL",
                "quantity": quantity,
                "price": price,
                "side": TradeSide.BUY,
                "status": TradeStatus.SUBMITTED
        )
        UUID accountId = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(post('/accounts/{accountId}/trades', accountId)
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "expect bad request status"
        results.andExpect(status().isBadRequest())

        and: "expect an error message"
        results.andExpect(jsonPath(field).value(error))

        and: "generate docs"
        results.andDo(document(snippetPathFactory.create("1.0.0", 'post-not-positive-values')))

        where:
        price                   | quantity | field        | error
        new BigDecimal("28.46") | 0        | '$.quantity' | "Quantity must be greater than zero"
        new BigDecimal("0.0")   | 4        | '$.price'    | "Price must be greater than zero"
    }

    def "Should accept get trade requests"() {
        given: "an account and tradeId"
        Account account = new Account()
        UUID accountId = UUID.randomUUID()
        account.accountUuid = accountId
        UUID tradeId = UUID.randomUUID()

        and: 'a TradeResponseDto'
        TradeResponseDto responseDto = new TradeResponseDto(
                quantity: 3,
                price: "3.56",
                side: TradeSide.BUY.toString(),
                status: TradeStatus.SUBMITTED.toString(),
                symbol: "GOOG",
                accountId: accountId as String
        )

        when: "the request is submitted"
        ResultActions results = mvc.perform(get('/accounts/{accountId}/trades/{tradeId}',
                accountId, tradeId)
                .header("Accept-Version", "1.0.0")
                .contentType(APPLICATION_JSON)
        )

        then: "an account is found"
        1 * mockAccountSrv.findAccount(accountId) >> account

        and: "a trade is found"
        1 * mockTradeSrv.getTradeResponseDto(tradeId) >> responseDto

        and: "expect ok response"
        results.andExpect(status().isOk())

        and: "expect response body"
        results.andExpect(jsonPath('$.symbol').value("GOOG"))
        results.andExpect(jsonPath('$.quantity').value(3))
        results.andExpect(jsonPath('$.price').value("3.56"))
        results.andExpect(jsonPath('$.side').value(TradeSide.BUY.toString()))
        results.andExpect(jsonPath('$.status').value(TradeStatus.SUBMITTED.toString()))
        results.andExpect(jsonPath('$.accountId').value(accountId as String))

        and: "generate docs"
        results.andDo(document(snippetPathFactory.create("1.0.0", 'get-success'),
                pathParameters(
                        parameterWithName("accountId").description("Account Id"),
                        parameterWithName("tradeId").description("Trade Id")),
                responseFields(
                        fieldWithPath("symbol").description("Symbol of the Trade"),
                        fieldWithPath("quantity").description("Number of trades"),
                        fieldWithPath("price").description("Price of the Trade"),
                        fieldWithPath("side").description("Possible values: buy, sell"),
                        fieldWithPath("status").description("Possible values: submitted, cancelled, completed, failed"),
                        fieldWithPath("accountId").description("Trade's account id")
                )
        ))
    }

    def "Should validate trade not found for get request"() {
        given: "an account and tradeId"
        Account account = new Account()
        UUID accountId = UUID.randomUUID()
        account.accountUuid = accountId
        UUID tradeId = UUID.randomUUID()

        and: 'a TradeResponseDto'
        TradeResponseDto responseDto = new TradeResponseDto(
                quantity: 3,
                price: "3.56",
                side: TradeSide.BUY.toString(),
                status: TradeStatus.SUBMITTED.toString(),
                symbol: "GOOG",
                accountId: accountId as String
        )

        when: "the request is submitted"
        ResultActions results = mvc.perform(get('/accounts/{accountId}/trades/{tradeId}',
                accountId, tradeId)
                .header("Accept-Version", "1.0.0")
                .contentType(APPLICATION_JSON)
        )

        then: "an account is found"
        1 * mockAccountSrv.findAccount(accountId) >> account

        and: "a trade is not found"
        1 * mockTradeSrv.getTradeResponseDto(tradeId) >> null

        and: "expect not found response"
        results.andExpect(status().isNotFound())

        and: "expect an error message"
        results.andExpect(jsonPath('$.error').value('tradeId not found'))

        and: "generate docs"
        results.andDo(document(snippetPathFactory.create("1.0.0", 'get-tradeId-not-found')))
    }

    def "Should accept patch trade requests"() {
        given: "an account"
        Account account = new Account()
        UUID accountId = UUID.randomUUID()
        account.accountUuid = accountId

        and: "a trade"
        UUID tradeId = UUID.randomUUID()
        Trade trade = new Trade(
                tradeUuid: tradeId,
                status: TradeStatus.SUBMITTED
        )

        and: "a status request"
        StatusRequestDto req = new StatusRequestDto(
                status: TradeStatus.CANCELLED
        )

        when: "the request is submitted"
        ResultActions results = mvc.perform(patch('/accounts/{accountId}/trades/{tradeId}',
                accountId, tradeId)
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "account is found"
        1 * mockAccountSrv.findAccount(accountId) >> account

        and: "a trade is found"
        1 * mockTradeSrv.findTrade(tradeId) >> trade

        and: "trade status is changed successfully"
        1 * mockTradeSrv.changeStatusToCancelled(trade) >> true

        and: "a no content response is returned"
        results.andExpect(status().isNoContent())

        and: "generate docs"
        results.andDo(document(snippetPathFactory.create("1.0.0", 'patch-success'),
                pathParameters(
                        parameterWithName("accountId").description("Account Id"),
                        parameterWithName("tradeId").description("Trade Id")),
                requestFields(
                        fieldWithPath("status").description("Possible values: submitted, cancelled, completed, failed"))
        ))
    }

    def "Should validate trade not found for patch request"() {
        given: "an account"
        Account account = new Account()
        UUID accountId = UUID.randomUUID()
        account.accountUuid = accountId

        and: "a trade"
        UUID tradeId = UUID.randomUUID()
        Trade trade = new Trade(
                tradeUuid: tradeId,
                status: TradeStatus.SUBMITTED
        )

        and: "a status request"
        StatusRequestDto req = new StatusRequestDto(
                status: TradeStatus.CANCELLED
        )

        when: "the request is submitted"
        ResultActions results = mvc.perform(patch('/accounts/{accountId}/trades/{tradeId}',
                accountId, tradeId)
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "account is found"
        1 * mockAccountSrv.findAccount(accountId) >> account

        and: "a trade is not found"
        1 * mockTradeSrv.findTrade(tradeId) >> null

        and: "return not found"
        results.andExpect(status().isNotFound())

        and: "generate docs"
        results.andDo(document(snippetPathFactory.create("1.0.0", 'patch-not-found')))
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
