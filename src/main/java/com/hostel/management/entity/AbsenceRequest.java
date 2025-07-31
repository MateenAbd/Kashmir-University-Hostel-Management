package com.hostel.management.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AbsenceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"absenceRequests", "user", "otherRecursiveFields"}) // ignore fields causing recursion in Student entity
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties({"password", "absenceRequests", "otherRecursiveFields"}) // adjust accordingly
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

        if (cutoffTime == null) {
            // Default to 11:00 AM if cutoff time is null
            cutoffTime = LocalTime.of(11, 0);
        }

        // Check if request is after the configured cutoff time
        LocalTime submissionTime = submittedAt.toLocalTime();
        isLateRequest = submissionTime.isAfter(cutoffTime);
    }

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }
}
