package com.altruist.trade;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class TradeRequestDto {
  @NotBlank(message = "Symbol is mandatory")
  public String symbol;

  @NotNull(message = "Quantity is mandatory")
  @Positive(message = "Quantity must be greater than zero")
  public Integer quantity;

  @NotNull(message = "Price is mandatory")
  @Positive(message = "Price must be greater than zero")
  public BigDecimal price;

  @NotNull(message = "Side is mandatory")
  public TradeSide side;

  @NotNull(message = "Status is mandatory")
  public TradeStatus status;
}
