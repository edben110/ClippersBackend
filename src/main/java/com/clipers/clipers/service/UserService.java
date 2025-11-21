package com.clipers.clipers.service;

import com.clipers.clipers.dto.RegisterRequest;
import com.clipers.clipers.dto.UserDTO;
import com.clipers.clipers.entity.Company;
import com.clipers.clipers.entity.User;
import com.clipers.clipers.repository.CompanyRepository;
import com.clipers.clipers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service that implements Template Method and Factory Method patterns implicitly
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Autowired
    public UserService(UserRepository userRepository, 
                      CompanyRepository companyRepository,
                      PasswordEncoder passwordEncoder,
                      NotificationService notificationService) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    /**
     * Template Method Pattern implemented implicitly
     * Defines the registration algorithm with specific steps
     */
    public UserDTO registerUser(RegisterRequest request) {
        // Step 1: Validate request
        validateRegistrationRequest(request);
        
        // Step 2: Create user using Factory Method (implicit in User entity)
        User user = createUserByRole(request);
        
        // Step 3: Save user
        User savedUser = userRepository.save(user);
        
        // Step 4: Create additional profile if needed
        createAdditionalProfile(savedUser, request);
        
        // Step 5: Send welcome notification (Observer pattern implicit)
        notificationService.sendWelcomeNotification(savedUser);
        
        // Step 6: Convert to DTO and return
        return convertToDTO(savedUser);
    }

    // Template method steps - can be overridden in subclasses if needed
    protected void validateRegistrationRequest(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email ya está registrado");
        }
        
        if (request.getRole() == null) {
            throw new IllegalArgumentException("Role es requerido");
        }
        
        validateRoleSpecificData(request);
    }

    private void validateRoleSpecificData(RegisterRequest request) {
        switch (request.getRole()) {
            case CANDIDATE:
                if (request.getFirstName() == null || request.getLastName() == null) {
                    throw new IllegalArgumentException("FirstName y LastName son requeridos para candidatos");
                }
                break;
            case COMPANY:
                if (request.getCompanyName() == null) {
                    throw new IllegalArgumentException("CompanyName es requerido para empresas");
                }
                break;
            case ADMIN:
                // Admin puede tener datos opcionales
                break;
        }
    }

    // Factory Method implicit - delegates to User factory methods
    private User createUserByRole(RegisterRequest request) {
        switch (request.getRole()) {
            case CANDIDATE:
                return User.createCandidate(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName(),
                    passwordEncoder
                );
            case COMPANY:
                return User.createCompany(
                    request.getEmail(),
                    request.getPassword(),
                    request.getCompanyName(),
                    passwordEncoder
                );
            case ADMIN:
                return User.createAdmin(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName(),
                    passwordEncoder
                );
            default:
                throw new IllegalArgumentException("Rol de usuario no válido: " + request.getRole());
        }
    }

    private void createAdditionalProfile(User user, RegisterRequest request) {
        if (user.getRole() == User.Role.COMPANY) {
            Company company = new Company();
            company.setName(request.getCompanyName());
            company.setUserId(user.getId());
            companyRepository.save(company);
        }
    }

    public Optional<UserDTO> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    public Optional<UserDTO> findById(String id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    public List<UserDTO> findByRole(User.Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> searchUsers(String query) {
        return userRepository.searchUsers(query)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO updateUser(String userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Template method for update
        updateBasicInfo(user, userDTO);
        validateUpdatedUser(user);
        
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    private void updateBasicInfo(User user, UserDTO userDTO) {
        if (userDTO.getFirstName() != null) {
            user.setFirstName(userDTO.getFirstName());
        }
        if (userDTO.getLastName() != null) {
            user.setLastName(userDTO.getLastName());
        }
        if (userDTO.getProfileImage() != null) {
            user.setProfileImage(userDTO.getProfileImage());
        }
    }

    private void validateUpdatedUser(User user) {
        if (!user.isValidForRole()) {
            throw new IllegalArgumentException("Datos de usuario inválidos para el rol especificado");
        }
    }

    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        userRepository.deleteById(userId);
    }

    // Adapter pattern implicit - converts between entity and DTO
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
}