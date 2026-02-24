package com.example.Blogging_platform2.controller;

import com.example.Blogging_platform2.dao.UserDao;
import com.example.Blogging_platform2.dao.RoleDao;
import com.example.Blogging_platform2.dto.JwtResponse;
import com.example.Blogging_platform2.dto.LoginRequestDto7;
import com.example.Blogging_platform2.dto.UserDto;
import com.example.Blogging_platform2.model.Role;
import com.example.Blogging_platform2.model.User;
import com.example.Blogging_platform2.securityconfig7.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDao userDao;
    private final RoleDao roleDao;   //  Inject RoleDao
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserDao userDao,
                          RoleDao roleDao,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDao = userDao;
        this.roleDao = roleDao;   //  assign RoleDao
        this.passwordEncoder = passwordEncoder;
    }

    // -------------------- LOGIN --------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto7 loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            String token = jwtUtil.generateToken(loginRequest.getUsername(), roles);

            return ResponseEntity.ok(new JwtResponse(token, "Bearer"));

        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
    }

    // -------------------- REGISTER --------------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto userDto) {
        if (userDao.findByUsername(userDto.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        //  Assign role: default to ROLE_READER if none provided
        String roleName = (userDto.getRole() != null) ? userDto.getRole() : "ROLE_READER";
        Role role = roleDao.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.assignDefaultRole(role);


        userDao.save(user);

        return ResponseEntity.ok("User registered successfully with role " + roleName);
    }
}
