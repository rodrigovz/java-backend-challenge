package com.altruist.account;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountDto {
  public String username;
  public String email;
  public String name;
  public String street;
  public String city;
  public String state;
  public String zipcode;
}
