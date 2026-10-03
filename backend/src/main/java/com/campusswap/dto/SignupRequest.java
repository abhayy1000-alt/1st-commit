package com.campusswap.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 50) String collegeId,
        @NotBlank @Size(max = 20) String phone,
        @NotBlank @Size(max = 100) String branch,
        @NotBlank @Pattern(regexp = "1st Year|2nd Year|3rd Year|4th Year",
                message = "must be 1st Year, 2nd Year, 3rd Year or 4th Year") String year,
        @NotBlank @Size(min = 8, max = 100, message = "must be at least 8 characters") String password
) {
}
