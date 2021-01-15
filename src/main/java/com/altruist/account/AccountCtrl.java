package com.altruist.account;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.altruist.IdDto;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
@Slf4j
public class AccountCtrl {

  private final AccountSrv accountSrv;

  public AccountCtrl(AccountSrv accountSrv) {
    this.accountSrv = accountSrv;
  }

  @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<IdDto> create(
      @RequestBody @Valid AccountDto accountDto,
      HttpServletRequest httpServletRequest
  ) throws URISyntaxException {
    log.info("Received Account creation request [{}].", accountDto);
    UUID accountId = accountSrv.createAccount(accountDto);
    return ResponseEntity.created(new URI(httpServletRequest.getRequestURL() + "/" + accountId.toString()))
        .body(new IdDto(accountId));
  }
}
