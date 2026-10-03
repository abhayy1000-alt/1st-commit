package com.campusswap.model;

/** Serialized lower-case ("info", "success", "error") to match the frontend icon map. */
public enum NotificationType {
    INFO,
    SUCCESS,
    ERROR;

    public String toJson() {
        return name().toLowerCase();
    }
}
