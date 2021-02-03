package com.altruist.account;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@Table(name = "address", schema = "trade")
public class Address {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public UUID addressUuid;

  public String name;
  public String street;
  public String city;

  @Enumerated(EnumType.STRING)
  public State state;

  public Integer zipcode;
}
