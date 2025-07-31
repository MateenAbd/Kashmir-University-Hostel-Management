package com.hostel.management.repository;

import com.hostel.management.entity.Attendance;
import com.hostel.management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByStudentAndDate(Student student, LocalDate date);
    
    @Query("SELECT a FROM Attendance a WHERE a.student = :student AND a.date BETWEEN :startDate AND :endDate ORDER BY a.date DESC")
    List<Attendance> fiudentAndDateBetween(@Param("student") Student student,
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student = :student AND a.status = 'PRESENT' AND FUNCTION('YEAR', a.date) = :year AND FUNCTION('MONTH', a.date) = :month")
    Integer countPresentDaysByStudentAndMonth(@Param("student") Student student, 
                                            @Param("year") int year, 
                                            @Param("month") int month);
    
    @Query("SELECT SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) FROM Attendance a WHERE FUNCTION('YEAR', a.date) = :year AND FUNCTION('MONTH', a.date) = :month")
    Integer countTotalPresentDaysByMonth(@Param("year") int year, @Param("month") int month);
}
