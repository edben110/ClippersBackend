package com.clipers.clipers.service;

import com.clipers.clipers.dto.AuthRequest;
import com.clipers.clipers.dto.AuthResponse;
import com.clipers.clipers.dto.RegisterRequest;
import com.clipers.clipers.dto.UserDTO;
import com.clipers.clipers.entity.User;
import com.clipers.clipers.repository.UserRepository;
import com.clipers.clipers.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación que implementa Template Method implícitamente
 * para los flujos de login y registro
 */
@Service
@Transactional
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthService(UserService userService,
                      UserRepository userRepository,
                      AuthenticationManager authenticationManager,
                      JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Template Method para login - define los pasos del proceso de autenticación
     */
    public AuthResponse login(AuthRequest request) {
        // Step 1: Validate credentials
        Authentication authentication = authenticateUser(request);
        
        // Step 2: Set security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Step 3: Get user details
        User user = getUserByEmail(request.getEmail());

        // Step 4: Generate tokens
        TokenPair tokens = generateTokens(user);

        // Step 5: Convert and return response
        UserDTO userDTO = convertToDTO(user);
        return new AuthResponse(tokens.accessToken, tokens.refreshToken, userDTO);
    }

    private Authentication authenticateUser(AuthRequest request) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException("Credenciales inválidas", e);
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private TokenPair generateTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        return new TokenPair(accessToken, refreshToken);
    }

    /**
     * Template Method para registro - define los pasos del proceso de registro
     */
    public AuthResponse register(RegisterRequest request) {
        // Step 1: Register user using UserService (que tiene su propio Template Method)
        UserDTO userDTO = userService.registerUser(request);

        // Step 2: Get the saved user for token generation
        User user = getUserById(userDTO.getId());

        // Step 3: Generate tokens
        TokenPair tokens = generateTokens(user);

        // Step 4: Return response
        return new AuthResponse(tokens.accessToken, tokens.refreshToken, userDTO);
    }

    private User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error al crear usuario"));
    }

    /**
     * Template Method para refresh token
     */
    public AuthResponse refreshToken(String refreshToken) {
        // Step 1: Validate refresh token
        validateRefreshToken(refreshToken);

        // Step 2: Extract user from token
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = getUserByEmail(email);

        // Step 3: Generate new tokens
        TokenPair tokens = generateTokens(user);

        // Step 4: Return response
        UserDTO userDTO = convertToDTO(user);
        return new AuthResponse(tokens.accessToken, tokens.refreshToken, userDTO);
    }

    private void validateRefreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Token de refresh inválido");
        }
    }

    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails)) {
            throw new RuntimeException("Usuario no autenticado");
        }

        org.springframework.security.core.userdetails.UserDetails userDetails =
            (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        User user = getUserByEmail(email);
        return convertToDTO(user);
    }

    public UserDTO getCurrentUser(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token no proporcionado");
        }

        String userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getProfileImage(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    // Value Object para encapsular el par de tokens
    private static class TokenPair {
        final String accessToken;
        final String refreshToken;

        TokenPair(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}
