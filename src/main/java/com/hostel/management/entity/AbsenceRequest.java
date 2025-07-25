package com.hostel.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "absence_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AbsenceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Column(nullable = false)
    private LocalDate absenceDate;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private String comments;

    private LocalDateTime submittedAt;

    private LocalDateTime approvedAt;

    @Column(name = "is_late_request")
    private Boolean isLateRequest;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void setLateRequestStatus(LocalTime cutoffTime) {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
        // Check if request is after the configured cutoff time
        LocalTime submissionTime = submittedAt.toLocalTime();
        isLateRequest = submissionTime.isAfter(cutoffTime);
    }

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }
}
