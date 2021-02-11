package com.altruist.account;

import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
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

    Address address = new Address();
    address.name = accountDto.name;
    address.street = accountDto.street;
    address.city = accountDto.city;
    address.state = State.valueOf(accountDto.state.toUpperCase());
    address.zipcode = Integer.parseInt(accountDto.zipcode);

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
      account.address = address;
    }

    return accountRepo.save(account)
        .accountUuid;
  }

  public boolean existsUsername(String username) {
    return accountRepo.existsByUsername(username);
  }

  public boolean existsEmail(String email) {
    return accountRepo.existsByEmail(email);
  }

  public Account findAccount(UUID accountUuid) {
    return accountRepo.findByAccountUuid(accountUuid);
  }
}
