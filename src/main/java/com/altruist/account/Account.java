package com.altruist.account;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@Table(name = "account", schema = "trade")
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public UUID accountUuid;

  public String username;
  public String email;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "addressUuid", referencedColumnName = "addressUuid")
  public Address address;
}
