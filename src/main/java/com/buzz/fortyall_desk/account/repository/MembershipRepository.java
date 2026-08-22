package com.buzz.fortyall_desk.account.repository;

import com.buzz.fortyall_desk.account.entity.Membership;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    @Query("select m from Membership m join fetch m.center where m.account.id = :accountId")
    List<Membership> findAllByAccountId(@Param("accountId") Long accountId);

    Optional<Membership> findByContactPhoneAndStatus(String contactPhone, Membership.MembershipStatus status);

    List<Membership> findAllByCenterIdOrderByNameAsc(Long centerId);

    Optional<Membership> findByIdAndCenterId(Long id, Long centerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Membership m where m.id = :id")
    Optional<Membership> findByIdForUpdate(@Param("id") Long id);
}
