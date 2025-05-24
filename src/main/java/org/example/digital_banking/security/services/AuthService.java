package org.example.digital_banking.security.services;

import org.example.digital_banking.dtos.security.AuthResponse;
import org.example.digital_banking.dtos.security.LoginRequest;
import org.example.digital_banking.dtos.security.RegisterRequest;
import org.example.digital_banking.entities.Customer;
import org.example.digital_banking.entities.security.Role;
import org.example.digital_banking.entities.security.User;
import org.example.digital_banking.repositories.CustomerRepo;
import org.example.digital_banking.repositories.security.RoleRepository;
import org.example.digital_banking.repositories.security.UserRepository;
import org.example.digital_banking.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CustomerRepo customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication);

        org.springframework.security.core.userdetails.User userDetails =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), roles);
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        // Create new user
        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .enabled(true)
                .roles(new ArrayList<>())
                .build();

        // Assign default role (USER)
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER", "Standard user role")));
        user.getRoles().add(userRole);

        User savedUser = userRepository.save(user);

        // Create customer profile linked to user
        Customer customer = Customer.builder()
                .name(registerRequest.getFirstName() + " " + registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword()) // Note: This is redundant but kept for backward compatibility
                .phone(registerRequest.getPhone())
                .address(registerRequest.getAddress())
                .city(registerRequest.getCity())
                .user(savedUser)
                .build();

        customerRepository.save(customer);

        // Generate token and return response
        List<String> roles = savedUser.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.toList());

        String jwt = jwtUtil.generateToken(savedUser.getUsername());

        return new AuthResponse(jwt, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), roles);
    }
}