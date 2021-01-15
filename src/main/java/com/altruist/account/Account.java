package com.altruist.account;

import java.util.UUID;
import lombok.Data;

@Data
public class Account {

  public UUID account_uuid;
  public UUID address_uuid;
  public String username;
  public String email;
  public String name;
  public String street;
  public String city;
  public String state;
  public Integer zipcode;
}
