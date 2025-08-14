package com.finanzas.app_back.dto.User;

import lombok.Data;

@Data
public class UserLogin {
    private String email;
    private String password;
}