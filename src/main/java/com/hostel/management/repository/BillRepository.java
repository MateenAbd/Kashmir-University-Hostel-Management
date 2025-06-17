package com.hostel.management.repository;

import com.hostel.management.entity.Bill;
import com.hostel.management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByStudentAndMonthYear(Student student, String monthYear);
    
    List<Bill> findByStudentOrderByMonthYearDesc(Student student);
    
    List<Bill> findByStatusOrderByMonthYearDesc(Bill.BillStatus status);
    
    // New method to find bills that are not fully paid, ordered by oldest first
    List<Bill> findByStudentAndStatusNotOrderByMonthYearAsc(Student student, Bill.BillStatus status);
}
