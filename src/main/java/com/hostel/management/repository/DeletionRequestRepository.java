package com.hostel.management.repository;

import com.hostel.management.entity.DeletionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeletionRequestRepository extends JpaRepository<DeletionRequest, Long> {
    List<DeletionRequest> findByStatusOrderByCreatedAtDesc(DeletionRequest.RequestStatus status);
}
