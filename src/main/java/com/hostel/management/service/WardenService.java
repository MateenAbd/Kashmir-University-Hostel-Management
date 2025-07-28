package com.hostel.management.service;

import com.hostel.management.entity.AbsenceRequest;
import com.hostel.management.entity.DeletionRequest;
import com.hostel.management.entity.MonthlyExpense;

import java.util.List;

/**
 * Service interface for warden-related operations
 */
public interface WardenService {
    /**
     * Gets pending deletion requests
     *
     * @return List of pending deletion requests
     */
    List<DeletionRequest> getPendingDeletionRequests();

    /**
     * Approves a deletion request
     *
     * @param requestId ID of the request to approve
     * @param wardenEmail Email of the warden approving the request
     */
    void approveDeletionRequest(Long requestId, String wardenEmail);

    /**
     * Rejects a deletion request
     *
     * @param requestId ID of the request to reject
     * @param reason Rejection reason
     * @param wardenEmail Email of the warden rejecting the request
     */
    void rejectDeletionRequest(Long requestId, String reason, String wardenEmail);

    /**
     * Gets all monthly expenses
     *
     * @return List of all monthly expenses
     */
    List<MonthlyExpense> getAllMonthlyExpenses();

    /**
     * Gets a specific monthly expense
     *
     * @param monthYear Month and year in YYYY-MM format
     * @return Monthly expense for the specified month
     */
    MonthlyExpense getMonthlyExpense(String monthYear);

    /**
     * Gets late absence requests (after cutoff time)
     *
     * @return List of late absence requests
     */
    List<AbsenceRequest> getLateAbsenceRequests();

    /**
     * Approves an absence request
     *
     * @param requestId ID of the request to approve
     * @param comments Approval comments
     * @param wardenEmail Email of the warden approving the request
     */
    void approveAbsenceRequest(Long requestId, String comments, String wardenEmail);

    /**
     * Rejects an absence request
     *
     * @param requestId ID of the request to reject
     * @param reason Rejection reason
     * @param wardenEmail Email of the warden rejecting the request
     */
    void rejectAbsenceRequest(Long requestId, String reason, String wardenEmail);
}
