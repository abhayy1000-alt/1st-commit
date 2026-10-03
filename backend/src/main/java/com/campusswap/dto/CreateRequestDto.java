package com.campusswap.dto;

import jakarta.validation.constraints.NotNull;

public record CreateRequestDto(@NotNull Long itemId) {
}
