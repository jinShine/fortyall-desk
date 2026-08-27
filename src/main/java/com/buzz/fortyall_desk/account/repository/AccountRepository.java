package com.buzz.fortyall_desk.account.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.buzz.fortyall_desk.account.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

	Optional<Account> findByEmail(String email);

	Optional<Account> findByPhoneNumber(String phoneNumber);
}
