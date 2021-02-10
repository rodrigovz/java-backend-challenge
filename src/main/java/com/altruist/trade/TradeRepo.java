package com.altruist.trade;

import com.altruist.account.Account;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Repository
public interface TradeRepo extends PagingAndSortingRepository<Trade, UUID> {
    Trade findByTradeUuid(UUID tradeUuid);

    List<Trade> findAllByAccount(Account account, Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Trade t set t.status = ?2 where t.tradeUuid = ?1")
    int changeStatus(UUID tradeUuid, TradeStatus status);
}