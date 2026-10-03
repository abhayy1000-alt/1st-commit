package com.campusswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ItemCreateRequest(
        @NotBlank @Size(max = 100) String name,

        @NotBlank
        @Pattern(regexp = "Books|Calculators|Lab Equipment|Stationery|Electronics|Other",
                message = "is not a valid category")
        String category,

        @Size(max = 500) String description,

        @NotBlank
        @Pattern(regexp = "New|Like New|Good|Fair|Used", message = "is not a valid condition")
        String condition,

        @NotBlank @Size(max = 100) String location,

        @NotBlank
        @Pattern(regexp = "Sell|Borrow|Donate|Exchange", message = "must be Sell, Borrow, Donate or Exchange")
        String listingType
) {
}
