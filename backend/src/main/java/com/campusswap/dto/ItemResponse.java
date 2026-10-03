package com.campusswap.dto;

import com.campusswap.model.Item;

import java.time.Instant;

/** Field names match the objects the frontend previously kept in localStorage ("cs_items"). */
public record ItemResponse(
        Long id,
        String name,
        String category,
        String description,
        String condition,
        String listingType,
        String location,
        boolean available,
        String ownerName,
        String ownerEmail,
        Instant createdAt
) {
    public static ItemResponse from(Item i) {
        return new ItemResponse(i.getId(), i.getName(), i.getCategory(), i.getDescription(),
                i.getCondition(), i.getListingType(), i.getLocation(), i.isAvailable(),
                i.getOwner().getName(), i.getOwner().getEmail(), i.getCreatedAt());
    }
}
