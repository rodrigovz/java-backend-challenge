package com.altruist.account;

import com.altruist.core.IdDto;
import com.altruist.core.SingleErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/accounts")
@Slf4j
public class AccountCtrl {

  private final AccountSrv accountSrv;

  public AccountCtrl(AccountSrv accountSrv) {
    this.accountSrv = accountSrv;
  }

  @PostMapping(consumes = APPLICATION_JSON_VALUE,
          produces = APPLICATION_JSON_VALUE,
          headers = "Accept-Version=1.0.0")
  public @ResponseBody ResponseEntity create(
      @RequestBody @Valid AccountDto accountDto,
      HttpServletRequest httpServletRequest
  ) throws URISyntaxException {
    log.info("Received Account creation request [{}].", accountDto);

    if (accountSrv.existsEmail(accountDto.email)) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
              .body(new SingleErrorResponseDto("Email already exists"));
    }

    if (accountSrv.existsUsername(accountDto.username)) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
              .body(new SingleErrorResponseDto("Username already exists"));
    }

    UUID accountId = accountSrv.createAccount(accountDto);
    return ResponseEntity.created(new URI(httpServletRequest.getRequestURL() + "/" + accountId.toString()))
        .body(new IdDto(accountId));
  }
}
