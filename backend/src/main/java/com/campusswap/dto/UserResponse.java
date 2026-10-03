package com.campusswap.dto;

import com.campusswap.model.User;

/** Public profile of a user. The password hash is never exposed. */
public record UserResponse(
        Long id,
        String name,
        String email,
        String collegeId,
        String branch,
        String year,
        String phone
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getCollegeId(),
                u.getBranch(), u.getYear(), u.getPhone());
    }
}
