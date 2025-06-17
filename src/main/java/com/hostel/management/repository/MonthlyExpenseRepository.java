package com.hostel.management.repository;

import com.hostel.management.entity.MonthlyExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonthlyExpenseRepository extends JpaRepository<MonthlyExpense, Long> {
    Optional<MonthlyExpense> findByMonthYear(String monthYear);
    boolean existsByMonthYear(String monthYear);
}
