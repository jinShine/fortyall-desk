package com.buzz.fortyall_desk.schedule.repository;

import com.buzz.fortyall_desk.schedule.entity.ScheduleParticipant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleParticipantRepository extends JpaRepository<ScheduleParticipant, Long> {
    @Query("""
           select sp from ScheduleParticipant sp
             join fetch sp.regularEnrollment re
             join fetch re.membership
           where sp.standingSchedule.id = :scheduleId and sp.active = true
           """)
    List<ScheduleParticipant> findActiveByScheduleId(@Param("scheduleId") Long scheduleId);

    long countByStandingScheduleIdAndActiveTrue(Long scheduleId);
}
