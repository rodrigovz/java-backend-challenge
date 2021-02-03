package com.altruist.account;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountRepo extends PagingAndSortingRepository<Account, UUID> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}