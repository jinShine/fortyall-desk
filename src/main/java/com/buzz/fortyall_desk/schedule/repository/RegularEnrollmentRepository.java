package com.buzz.fortyall_desk.schedule.repository;

import com.buzz.fortyall_desk.schedule.entity.RegularEnrollment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegularEnrollmentRepository extends JpaRepository<RegularEnrollment, Long> {
    Optional<RegularEnrollment> findByMembershipId(Long membershipId);
}
