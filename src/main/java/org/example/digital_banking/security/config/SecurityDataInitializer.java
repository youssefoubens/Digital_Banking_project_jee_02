package org.example.digital_banking.security.config;

import org.example.digital_banking.entities.Customer;
import org.example.digital_banking.entities.security.Role;
import org.example.digital_banking.entities.security.User;
import org.example.digital_banking.repositories.CustomerRepo;
import org.example.digital_banking.repositories.security.RoleRepository;
import org.example.digital_banking.repositories.security.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;

@Component
public class SecurityDataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepo customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Create roles if they don't exist
        if (roleRepository.count() == 0) {
            Role adminRole = new Role("ADMIN", "Administrator role with full access");
            Role userRole = new Role("USER", "Standard user role with limited access");
            roleRepository.saveAll(Arrays.asList(adminRole, userRole));
            System.out.println("Default roles created");
        }

        // Create admin user if it doesn't exist
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@digitalbanking.com")
                    .firstName("Admin")
                    .lastName("User")
                    .enabled(true)
                    .roles(new ArrayList<>())
                    .build();
            adminUser.getRoles().add(adminRole);
            
            User savedUser = userRepository.save(adminUser);
            
            // Create customer profile for admin
            Customer adminCustomer = Customer.builder()
                    .name("Admin User")
                    .email("admin@digitalbanking.com")
                    .password("admin123") // Note: This is redundant but kept for backward compatibility
                    .phone("1234567890")
                    .address("Admin Address")
                    .city("Admin City")
                    .user(savedUser)
                    .build();
            
            customerRepository.save(adminCustomer);
            
            System.out.println("Admin user created");
        }
    }
}