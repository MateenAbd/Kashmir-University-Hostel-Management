package com.hostel.management.repository;

import com.hostel.management.entity.FormNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormNumberRepository extends JpaRepository<FormNumber, Long> {
    Optional<FormNumber> findByFormNumberAndIsUsedFalse(String formNumber);
    boolean existsByFormNumber(String formNumber);
}
