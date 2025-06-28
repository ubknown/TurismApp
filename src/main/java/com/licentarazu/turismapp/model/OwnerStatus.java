package com.licentarazu.turismapp.model;

public enum OwnerStatus {
    NONE,        // No application submitted
    PENDING,     // Application submitted and pending review
    APPROVED,    // Application approved (user can be upgraded to OWNER)
    REJECTED     // Application rejected
}
