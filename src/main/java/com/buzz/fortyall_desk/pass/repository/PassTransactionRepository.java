package com.buzz.fortyall_desk.pass.repository;

import com.buzz.fortyall_desk.pass.entity.PassTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PassTransactionRepository extends JpaRepository<PassTransaction, Long> {
    @Query("select coalesce(sum(t.delta), 0) from PassTransaction t where t.lessonPass.id = :passId")
    int sumDeltaByPassId(@Param("passId") Long passId);

    List<PassTransaction> findAllByLessonPassIdOrderByIdAsc(Long passId);
}
