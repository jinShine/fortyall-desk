package com.buzz.fortyall_desk.lesson.repository;

import com.buzz.fortyall_desk.lesson.entity.Lesson;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    boolean existsByStandingScheduleIdAndLessonDate(Long scheduleId, LocalDate lessonDate);

    List<Lesson> findAllByCoachIdAndLessonDate(Long coachMembershipId, LocalDate lessonDate);

    @Query("""
           select distinct l from Lesson l
             join fetch l.participants p
             join fetch p.membership
           where l.coach.id = :coachId and l.lessonDate between :from and :to
           order by l.lessonDate asc, l.startTime asc
           """)
    List<Lesson> findCoachLessons(@Param("coachId") Long coachId,
                                  @Param("from") LocalDate from,
                                  @Param("to") LocalDate to);

    Optional<Lesson> findByIdAndCenterId(Long id, Long centerId);
}
