package com.hostel.management.repository;

import com.hostel.management.entity.RegistrationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {
    List<RegistrationRequest> findByStatusOrderByCreatedAtDesc(RegistrationRequest.RequestStatus status);
    boolean existsByEnrollmentNo(String enrollmentNo);
    boolean existsByEmail(String email);
}
