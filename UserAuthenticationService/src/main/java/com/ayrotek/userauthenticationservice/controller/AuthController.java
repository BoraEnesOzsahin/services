package com.ayrotek.userauthenticationservice.controller;

import com.ayrotek.userauthenticationservice.entity.User;
import com.ayrotek.userauthenticationservice.dto.UserRegistrationRequest;
import com.ayrotek.userauthenticationservice.repository.UserRepository;
import com.ayrotek.userauthenticationservice.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.ayrotek.userauthenticationservice.dto.LoginRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtils;

    @Autowired
    public AuthController(

            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtils
    ){

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;

    }


@PostMapping(value = "/signin", consumes = "application/json", produces = "application/json")
public Map<String, String> authenticateUser(@Valid @RequestBody LoginRequest loginRequest){
    Authentication authentication = authenticationManager.authenticate(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    loginRequest.username(), loginRequest.password()));
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    return Map.of("token", jwtUtils.generateToken(userDetails.getUsername()));
}



    @PostMapping("/signup")
    public Map<String, String> registerUser(@Valid @RequestBody UserRegistrationRequest request){

       if (userRepository.existsByUsername(request.username())){
           return Map.of("message", "User already exists");
       }

       final User newUser = new User(null, request.username(), passwordEncoder.encode(request.password())
       );
       userRepository.save(newUser);
       return Map.of("message", "User saved successfully");
    }





    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        // 1. Header kontrolü
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token formatı geçersiz.");
        }

        String token = authHeader.substring(7);

        // 2. Senin mevcut metodunla imza ve süre kontrolü
        if (jwtUtils.validateJwtToken(token)) {

            // 3. Gateway'e paslamak için token içinden verileri çıkarıyoruz
            // (Not: Jwts.parser()... kullanarak username ve rolleri çeken metotların olduğunu varsayıyorum)
            String username = jwtUtils.getUserFromToken(token);
            //List<String> roles = jwtUtils.getUserFromToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("username", username);
            //response.put("roles", roles);

            return ResponseEntity.ok(response);
        }

        // Metodun false dönerse veya catch'e düşerse direkt 401 fırlatıyoruz
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is invalid or expired.");
    }



    @GetMapping("/usertest/merhaba")
    public ResponseEntity<String> testEndpoint(@RequestHeader("X-User-Username") String username) {
        return ResponseEntity.ok("Gateway'den başarıyla geçtin! Kullanıcı adın: " + username);
    }



}
