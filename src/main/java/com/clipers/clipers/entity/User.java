package com.clipers.clipers.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad User que implementa Factory Method implícitamente
 * para la creación de diferentes tipos de usuarios
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.CANDIDATE;

    private String profileImage;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relationships
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cliper> clipers;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Company company;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ATSProfile atsProfile;

    // Constructors
    public User() {}

    private User(String email, String password, String firstName, String lastName, Role role) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // Factory Methods implementados implícitamente
    public static User createCandidate(String email, String rawPassword, String firstName, 
                                     String lastName, PasswordEncoder passwordEncoder) {
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentException("FirstName y LastName son requeridos para candidatos");
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);
        return new User(email, encodedPassword, firstName, lastName, Role.CANDIDATE);
    }

    public static User createCompany(String email, String rawPassword, String companyName, 
                                   PasswordEncoder passwordEncoder) {
        if (companyName == null) {
            throw new IllegalArgumentException("CompanyName es requerido para empresas");
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);
        return new User(email, encodedPassword, companyName, "Company", Role.COMPANY);
    }

    public static User createAdmin(String email, String rawPassword, String firstName, 
                                 String lastName, PasswordEncoder passwordEncoder) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        String fName = firstName != null ? firstName : "Admin";
        String lName = lastName != null ? lastName : "User";
        return new User(email, encodedPassword, fName, lName, Role.ADMIN);
    }

    // Template Method para validación de usuario
    public boolean isValidForRole() {
        if (!isBasicInfoValid()) {
            return false;
        }
        return isRoleSpecificInfoValid();
    }

    private boolean isBasicInfoValid() {
        return email != null && !email.isEmpty() && 
               password != null && !password.isEmpty() &&
               firstName != null && !firstName.isEmpty();
    }

    private boolean isRoleSpecificInfoValid() {
        switch (role) {
            case CANDIDATE:
                return lastName != null && !lastName.isEmpty();
            case COMPANY:
                return "Company".equals(lastName);
            case ADMIN:
                return true;
            default:
                return false;
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Cliper> getClipers() { return clipers; }
    public void setClipers(List<Cliper> clipers) { this.clipers = clipers; }

    public List<Post> getPosts() { return posts; }
    public void setPosts(List<Post> posts) { this.posts = posts; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public ATSProfile getAtsProfile() { return atsProfile; }
    public void setAtsProfile(ATSProfile atsProfile) { this.atsProfile = atsProfile; }

    public enum Role {
        CANDIDATE, COMPANY, ADMIN
    }
}