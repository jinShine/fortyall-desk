package com.buzz.fortyall_desk.schedule.repository;

import com.buzz.fortyall_desk.schedule.entity.StandingSchedule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StandingScheduleRepository extends JpaRepository<StandingSchedule, Long> {
    @Query("""
           select s from StandingSchedule s
             join fetch s.product
             join fetch s.coach
           where s.center.id = :centerId and s.status = 'ACTIVE'
           """)
    List<StandingSchedule> findActiveByCenterId(@Param("centerId") Long centerId);

    Optional<StandingSchedule> findByIdAndCenterId(Long id, Long centerId);
}
