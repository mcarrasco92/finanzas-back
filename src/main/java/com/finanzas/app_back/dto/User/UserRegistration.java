package com.finanzas.app_back.dto.User;

import lombok.Data;

@Data
public class UserRegistration {
    String email;
    String password;
    String name;
}
