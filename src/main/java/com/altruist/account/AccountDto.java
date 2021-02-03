package com.altruist.account;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class AccountDto {
  @NotBlank(message = "Username is mandatory")
  public String username;

  @Email(message= "Must be a valid email")
  @NotBlank(message = "Email is mandatory")
  public String email;

  public String name;
  public String street;
  public String city;
  public String state;
  public String zipcode;
}
