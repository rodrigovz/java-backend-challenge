package com.altruist.trade;

import com.altruist.account.Account;
import com.altruist.account.AccountSrv;
import com.altruist.core.IdDto;
import com.altruist.core.SingleErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/accounts/{accountId}/trades")
@Slf4j
public class TradeCtrl {

  @Autowired
  private TradeSrv tradeSrv;

  @Autowired
  private AccountSrv accountSrv;

  @PostMapping(consumes = APPLICATION_JSON_VALUE,
          produces = APPLICATION_JSON_VALUE,
          headers = "Accept-Version=1.0.0")
  public @ResponseBody ResponseEntity create(
      @RequestBody @Valid TradeRequestDto tradeRequestDto,
      @PathVariable("accountId") UUID accountId,
      HttpServletRequest httpServletRequest
  ) throws URISyntaxException {
    log.info("Received Trade creation request [{}]. for account id {}",
            tradeRequestDto, accountId);

    TradeDto tradeDto = new TradeDto();
    tradeDto.symbol = tradeRequestDto.symbol;
    tradeDto.quantity = tradeRequestDto.quantity;
    tradeDto.price = tradeRequestDto.price;
    tradeDto.side = tradeRequestDto.side;
    tradeDto.status = tradeRequestDto.status;

    tradeDto.account = accountSrv.findAccount(accountId);
    if (tradeDto.account == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new SingleErrorResponseDto("AccountId not found"));
    }

    UUID tradeId = tradeSrv.createTrade(tradeDto);
    return ResponseEntity.created(
            new URI(httpServletRequest.getRequestURL() + "/" + tradeId.toString()))
            .body(new IdDto(tradeId));
  }

  @GetMapping(value = "/{tradeId}",
          produces = APPLICATION_JSON_VALUE,
          headers = "Accept-Version=1.0.0")
  public @ResponseBody ResponseEntity get(
          @PathVariable("accountId") UUID accountId,
          @PathVariable("tradeId") UUID tradeId,
          HttpServletRequest httpServletRequest
  ) throws URISyntaxException {
    log.info("Received trade get request for trade id {}", tradeId);

    Account account = accountSrv.findAccount(accountId);
    if (account == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new SingleErrorResponseDto("AccountId not found"));
    }

    TradeResponseDto tradeResponseDto = tradeSrv.getTradeResponseDto(tradeId);
    if (tradeResponseDto == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new SingleErrorResponseDto("tradeId not found"));
    }
    return ResponseEntity.ok(tradeResponseDto);
  }

  @PatchMapping(value = "/{tradeId}",
          consumes = APPLICATION_JSON_VALUE,
          produces = APPLICATION_JSON_VALUE,
          headers = "Accept-Version=1.0.0")
  public @ResponseBody ResponseEntity patch(
          @RequestBody @Valid StatusRequestDto statusRequestDto,
          @PathVariable("accountId") UUID accountId,
          @PathVariable("tradeId") UUID tradeId,
          HttpServletRequest httpServletRequest
  ) throws URISyntaxException {
    log.info("Received Trade status update request [{}]. for trade id {}",
            statusRequestDto, tradeId);

    Account account = accountSrv.findAccount(accountId);
    if (account == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new SingleErrorResponseDto("AccountId not found"));
    }

    Trade trade = tradeSrv.findTrade(tradeId);
    if (trade == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new SingleErrorResponseDto("tradeId not found"));
    }

    if (trade.status != TradeStatus.SUBMITTED) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
              .body(new SingleErrorResponseDto("Cannot change trade status"));
    }

    if (statusRequestDto.status != TradeStatus.CANCELLED) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
              .body(new SingleErrorResponseDto("Trade can only be moved to Cancelled status"));
    }

    boolean success = tradeSrv.changeStatusToCancelled(trade);
    if (!success) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
              .body(new SingleErrorResponseDto("Could not update trade status"));
    }
    return ResponseEntity.noContent().build();
  }
}
