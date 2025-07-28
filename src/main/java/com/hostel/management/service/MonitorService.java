package com.hostel.management.service;

import com.hostel.management.entity.AbsenceRequest;

import java.util.List;

/**
 * Service interface for monitor-related operations
 */
public interface MonitorService {
    /**
     * Gets early absence requests (before cutoff time)
     *
     * @return List of early absence requests
     */
    List<AbsenceRequest> getEarlyAbsenceRequests();

    /**
     * Approves an absence request
     *
     * @param requestId ID of the request to approve
     * @param comments Approval comments
     * @param monitorEmail Email of the monitor approving the request
     */
    void approveAbsenceRequest(Long requestId, String comments, String monitorEmail);

    /**
     * Rejects an absence request
     *
     * @param requestId ID of the request to reject
     * @param reason Rejection reason
     * @param monitorEmail Email of the monitor rejecting the request
     */
    void rejectAbsenceRequest(Long requestId, String reason, String monitorEmail);
}
