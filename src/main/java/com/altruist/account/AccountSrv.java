package com.altruist.account;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AccountSrv {
  private final AccountRepo accountRepo;

  public AccountSrv(AccountRepo accountRepo) {
    this.accountRepo = accountRepo;
  }

  public UUID createAccount(AccountDto accountDto) {
    Objects.requireNonNull(accountDto.username);
    Objects.requireNonNull(accountDto.email);
    Account account = new Account();
    account.username = accountDto.username;
    account.email = accountDto.email;
    account.name = accountDto.name;
    account.street = accountDto.street;
    account.city = accountDto.city;
    account.state = accountDto.state;
    account.zipcode = Integer.parseInt(accountDto.zipcode);

    if (null != accountDto.name ||
        null != accountDto.street ||
        null != accountDto.city ||
        null != accountDto.state ||
        null != accountDto.zipcode
    ) {
      Objects.requireNonNull(accountDto.name);
      Objects.requireNonNull(accountDto.street);
      Objects.requireNonNull(accountDto.city);
      Objects.requireNonNull(accountDto.state);
      Objects.requireNonNull(accountDto.zipcode);
      account = accountRepo.saveAddress(account);
    }

    return accountRepo.save(account)
        .account_uuid;
  }
}
