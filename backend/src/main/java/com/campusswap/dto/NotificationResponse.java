package com.campusswap.dto;

import com.campusswap.model.Notification;

import java.time.Instant;

/** Field names match the objects the frontend previously kept in localStorage ("cs_notifications"). */
public record NotificationResponse(
        Long id,
        String recipientEmail,
        String title,
        String message,
        String type,
        Long relatedRequestId,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getRecipient().getEmail(), n.getTitle(),
                n.getMessage(), n.getType().toJson(), n.getRelatedRequestId(),
                n.isReadFlag(), n.getCreatedAt());
    }
}
