package com.clipers.clipers.controller;

import com.clipers.clipers.dto.AuthRequest;
import com.clipers.clipers.dto.AuthResponse;
import com.clipers.clipers.dto.RegisterRequest;
import com.clipers.clipers.dto.UserDTO;
import com.clipers.clipers.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Credenciales inválidas", e);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            System.out.println("=== REGISTRO INICIADO ===");
            System.out.println("Email: " + request.getEmail());
            System.out.println("Role: " + request.getRole());
            System.out.println("FirstName: " + request.getFirstName());
            System.out.println("LastName: " + request.getLastName());
            
            AuthResponse response = authService.register(request);
            
            System.out.println("=== REGISTRO EXITOSO ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("=== ERROR EN REGISTRO ===");
            System.out.println("Error: " + e.getMessage());
            System.out.println("Causa: " + (e.getCause() != null ? e.getCause().getMessage() : "Sin causa"));
            e.printStackTrace();
            throw new RuntimeException("Error al registrar usuario: " + e.getMessage(), e);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.isEmpty()) {
                throw new RuntimeException("Refresh token es requerido");
            }
            
            AuthResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al refrescar token: " + e.getMessage(), e);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Extract token from header if present
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            UserDTO user = authService.getCurrentUser(token);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener usuario actual", e);
        }
    }

}
