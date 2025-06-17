package com.hostel.management.repository;

import com.hostel.management.entity.AbsenceRequest;
import com.hostel.management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbsenceRequestRepository extends JpaRepository<AbsenceRequest, Long> {
    List<AbsenceRequest> findByStatusOrderByCreatedAtDesc(AbsenceRequest.RequestStatus status);
    
    Optional<AbsenceRequest> findByStudentAndAbsenceDate(Student student, LocalDate absenceDate);
    
    List<AbsenceRequest> findByStudentOrderByCreatedAtDesc(Student student);
    
    @Query("SELECT ar FROM AbsenceRequest ar WHERE ar.status = 'PENDING' AND ar.isLateRequest = :isLateRequest ORDER BY ar.createdAt DESC")
    List<AbsenceRequest> findPendingRequestsByTimeCategory(@Param("isLateRequest") Boolean isLateRequest);
}
