package com.altruist.trade.dto;

import com.altruist.trade.TradeStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class StatusRequestDto {
  @NotNull(message = "Status is mandatory")
  public TradeStatus status;
}
