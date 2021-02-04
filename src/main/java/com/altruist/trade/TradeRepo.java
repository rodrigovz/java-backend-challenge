package com.altruist.trade;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TradeRepo extends PagingAndSortingRepository<Trade, UUID> {

}