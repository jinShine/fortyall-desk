package com.buzz.fortyall_desk.lesson.repository;

import com.buzz.fortyall_desk.lesson.entity.LessonParticipant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonParticipantRepository extends JpaRepository<LessonParticipant, Long> {
    @Query("""
           select count(p) from LessonParticipant p
           where p.lessonPass.id = :passId and p.reserved = true
           """)
    int countReservedByPassId(@Param("passId") Long passId);

    @Query("""
           select p from LessonParticipant p
             join fetch p.lesson l
           where p.membership.id = :membershipId and l.lessonDate between :from and :to
           order by l.lessonDate asc, l.startTime asc
           """)
    List<LessonParticipant> findMyLessons(@Param("membershipId") Long membershipId,
                                          @Param("from") LocalDate from,
                                          @Param("to") LocalDate to);

    Optional<LessonParticipant> findByLessonIdAndMembershipId(Long lessonId, Long membershipId);
}
