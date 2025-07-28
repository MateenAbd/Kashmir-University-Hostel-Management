package com.hostel.management.service;

import com.hostel.management.dto.request.PaymentRequest;
import com.hostel.management.dto.response.RegistrationRequestResponse;
import com.hostel.management.entity.RegistrationRequest;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Service interface for admin-related operations
 */
public interface AdminService {
    /**
     * Adds form numbers for student registration
     *
     * @param formNumbers List of form numbers to add
     * @param adminEmail Email of the admin adding the form numbers
     */
    void addFormNumbers(List<String> formNumbers, String adminEmail);

    /**
     * Gets pending registration requests
     *
     * @return List of pending registration requests
     */
    List<RegistrationRequest> getPendingRegistrationRequests();

    /**
     * Gets pending registration requests as DTOs
     *
     * @return List of registration request response DTOs
     */
    List<RegistrationRequestResponse> getPendingRegistrationRequestResponses();

    /**
     * Approves a registration request
     *
     * @param requestId ID of the request to approve
     * @param adminEmail Email of the admin approving the request
     */
    void approveRegistrationRequest(Long requestId, String adminEmail);

    /**
     * Rejects a registration request
     *
     * @param requestId ID of the request to reject
     * @param comments Rejection comments
     * @param adminEmail Email of the admin rejecting the request
     */
    void rejectRegistrationRequest(Long requestId, String comments, String adminEmail);

    /**
     * Assigns monitor role to a student
     *
     * @param studentId ID of the student to assign monitor role
     */
    void assignMonitorRole(Long studentId);

    /**
     * Enters monthly expense and generates bills
     *
     * @param monthYear Month and year for the expense
     * @param totalAmount Total expense amount
     * @param adminEmail Email of the admin entering the expense
     */
    void enterMonthlyExpense(YearMonth monthYear, BigDecimal totalAmount, String adminEmail);

    /**
     * Records a payment for a student
     *
     * @param paymentRequest Payment details
     * @param adminEmail Email of the admin recording the payment
     */
    void recordPayment(PaymentRequest paymentRequest, String adminEmail);

    /**
     * Requests student deletion
     *
     * @param studentId ID of the student to delete
     * @param reason Reason for deletion
     * @param adminEmail Email of the admin requesting deletion
     */
    void requestStudentDeletion(Long studentId, String reason, String adminEmail);
}
