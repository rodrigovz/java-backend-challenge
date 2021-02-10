package com.altruist.trade.dto;

import com.altruist.trade.dto.TradeResponseDto;
import lombok.Data;

import java.util.List;

@Data
public class TradeListResponseDto {
  public List<TradeResponseDto> result;
}
