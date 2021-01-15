package com.altruist.account;

import java.sql.Types;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class AccountRepo {

  private final NamedParameterJdbcOperations jdbcOperations;

  public AccountRepo(NamedParameterJdbcOperations jdbcOperations) {
    this.jdbcOperations = jdbcOperations;
  }

  public Account save(Account account) {
    BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(account);
    KeyHolder keyHolder = new GeneratedKeyHolder();
    log.info("Saving account [{}].", account);
    String sql = "INSERT INTO trade.account (username,email,address_uuid) VALUES (:username, :email, :address_uuid)";
    jdbcOperations.update(sql, params, keyHolder);
    UUID id;
    Map<String, Object> keys = keyHolder.getKeys();
    if (null != keys) {
      id = (UUID) keys.get("account_uuid");
      log.info("Inserted account record {}.", id);
      account.account_uuid = id;
    } else {
      log.warn("Insert of account record failed. {}", account);
      throw new RuntimeException("Insert failed for account");
    }
    return account;
  }

  public Account saveAddress(Account account) {
    BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(account);
    params.registerSqlType("state", Types.VARCHAR);
    KeyHolder keyHolder = new GeneratedKeyHolder();
    log.info("Saving address [{}].", account);
    String sql = "INSERT INTO trade.address (name, street, city, state, zipcode) VALUES (:name, :street, :city, :state::trade.state, :zipcode)";
    jdbcOperations.update(sql, params, keyHolder);
    UUID id;
    Map<String, Object> keys = keyHolder.getKeys();
    if (null != keys) {
      id = (UUID) keys.get("address_uuid");
      log.info("Inserted address record {}.", id);
      account.address_uuid = id;
    } else {
      log.warn("Insert of address record failed. {}", account);
      throw new RuntimeException("Insert failed for address");
    }
    return account;
  }
}
