package com.campusswap.model;

import com.campusswap.exception.ApiException;

/** Labels match what the frontend displays and compares against ("Pending", "Accepted", "Rejected"). */
public enum RequestStatus {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected");

    private final String label;

    RequestStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static RequestStatus fromLabel(String value) {
        if (value != null) {
            String v = value.trim();
            for (RequestStatus s : values()) {
                if (s.label.equalsIgnoreCase(v) || s.name().equalsIgnoreCase(v)) {
                    return s;
                }
            }
        }
        throw ApiException.badRequest("Invalid status. Use 'Accepted' or 'Rejected'.");
    }
}
