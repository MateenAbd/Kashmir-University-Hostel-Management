package com.hostel.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "month_year"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 7) // Format: YYYY-MM
    private String monthYear;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amountDue;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer presentDays;

    @Column(nullable = false)
    private Integer totalDays;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BillStatus status = BillStatus.PENDING;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum BillStatus {
        PENDING, PARTIALLY_PAID, FULLY_PAID
    }
}
