package com.finance.dto.request;

import com.finance.entity.User;
import lombok.Data;

/** Both fields optional — send only what you want to change. */
@Data
public class UserUpdateRequest {
    private User.Role role;
    private User.UserStatus status;
}
