package com.buzz.fortyall_desk.account.repository;

import com.buzz.fortyall_desk.account.entity.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByLoginPhone(String loginPhone);
}
