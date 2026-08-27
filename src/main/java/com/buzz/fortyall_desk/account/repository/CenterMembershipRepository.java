package com.buzz.fortyall_desk.account.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.buzz.fortyall_desk.account.entity.CenterMembership;

public interface CenterMembershipRepository extends JpaRepository<CenterMembership, Long> {

	@Query("select m from CenterMembership m join fetch m.center where m.account.id = :accountId")
	List<CenterMembership> findAllByAccountId(@Param("accountId") Long accountId);

	Optional<CenterMembership> findByIdAndCenterId(Long id, Long centerId);

	List<CenterMembership> findAllByCenterIdOrderByDisplayNameAsc(Long centerId);
}
