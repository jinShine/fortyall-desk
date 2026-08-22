package com.buzz.fortyall_desk.pass.repository;

import com.buzz.fortyall_desk.pass.entity.LessonPass;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonPassRepository extends JpaRepository<LessonPass, Long> {
    List<LessonPass> findAllByMembershipIdOrderByIdAsc(Long membershipId);

    @Query("select p from LessonPass p where p.membership.id = :membershipId and p.status = 'ACTIVE' order by p.validUntil asc")
    List<LessonPass> findActiveByMembershipId(@Param("membershipId") Long membershipId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from LessonPass p where p.id = :id")
    Optional<LessonPass> findByIdForUpdate(@Param("id") Long id);
}
