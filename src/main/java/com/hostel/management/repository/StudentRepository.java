package com.hostel.management.repository;

import com.hostel.management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEnrollmentNo(String enrollmentNo);
    
    @Query("SELECT s FROM Student s WHERE s.user.email = :email")
    Optional<Student> findByUserEmail(@Param("email") String email);
    
    @Query("SELECT s FROM Student s WHERE s.user.userId = :userId")
    Optional<Student> findByUserId(@Param("userId") Long userId);
    
    boolean existsByEnrollmentNo(String enrollmentNo);
    
    Optional<Student> findByIsMonitorTrue();
    
    @Modifying
    @Query("UPDATE Student s SET s.isMonitor = false WHERE s.isMonitor = true")
    void clearAllMonitors();
}
