package com.altruist.trade;

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
import java.math.BigDecimal;
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
      @PathVariable("accountId") String accountId,
      HttpServletRequest httpServletRequest
  ) throws URISyntaxException {
    log.info("Received Trade creation request [{}].", tradeRequestDto);

    UUID accountUUID;
    try {
      accountUUID = UUID.fromString(accountId);
    } catch(IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new SingleErrorResponseDto("Wrong accountId format"));
    }

    TradeDto tradeDto = new TradeDto();
    tradeDto.symbol = tradeRequestDto.symbol;
    tradeDto.quantity = tradeRequestDto.quantity;
    tradeDto.price = tradeRequestDto.price;

    tradeDto.account = accountSrv.findAccount(accountUUID);
    if (tradeDto.account == null) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
              .body(new SingleErrorResponseDto("AccountId not found"));
    }

    try {
      tradeDto.status = TradeStatus.valueOf(tradeRequestDto.status.toUpperCase());
    } catch(IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new SingleErrorResponseDto("Invalid Status value"));
    }

    try {
      tradeDto.side = TradeSide.valueOf(tradeRequestDto.side.toUpperCase());
    } catch(IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new SingleErrorResponseDto("Invalid side value"));
    }

    UUID tradeId = tradeSrv.createTrade(tradeDto);
    return ResponseEntity.created(
            new URI(httpServletRequest.getRequestURL() + "/" + tradeId.toString()))
            .body(new IdDto(tradeId));
  }
}
