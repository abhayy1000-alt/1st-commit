package com.campusswap.dto;

import com.campusswap.model.Item;
import com.campusswap.model.Request;
import com.campusswap.model.RequestStatus;
import com.campusswap.model.User;

import java.time.Instant;

/**
 * Field names match the objects the frontend previously kept in localStorage ("cs_requests").
 * Phone numbers are only revealed once the owner has accepted the request.
 */
public record RequestResponse(
        Long id,
        Long itemId,
        String itemName,
        String itemCategory,
        String requesterName,
        String requesterEmail,
        String requesterPhone,
        String ownerName,
        String ownerEmail,
        String ownerPhone,
        String listingType,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static RequestResponse from(Request r) {
        Item item = r.getItem();
        User owner = item.getOwner();
        User requester = r.getRequester();
        boolean accepted = r.getStatus() == RequestStatus.ACCEPTED;

        return new RequestResponse(
                r.getId(),
                item.getId(),
                item.getName(),
                item.getCategory(),
                requester.getName(),
                requester.getEmail(),
                accepted ? requester.getPhone() : null,
                owner.getName(),
                owner.getEmail(),
                accepted ? owner.getPhone() : null,
                item.getListingType(),
                r.getStatus().getLabel(),
                r.getCreatedAt(),
                r.getUpdatedAt());
    }
}
