package org.example.digital_banking.dtos.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;

    // Customer related fields
    private String phone;
    private String address;
    private String city;

    // Role related field
    private boolean isCustomerAdmin = false;
}
