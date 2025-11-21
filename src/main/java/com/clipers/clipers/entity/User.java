package com.clipers.clipers.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User entity that implements Factory Method pattern implicitly
 * for creating different types of users
 */
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Email
    @NotBlank
    @Indexed(unique = true)
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private Role role = Role.CANDIDATE;

    private String profileImage;

    private String phone;

    private String address;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Relationships - MongoDB usa referencias por ID en lugar de objetos completos
    @JsonIgnore
    private List<String> cliperIds;

    @JsonIgnore
    private List<String> postIds;

    @JsonIgnore
    private String companyId;

    @JsonIgnore
    private String atsProfileId;

    // Constructors
    public User() {}

    private User(String email, String password, String firstName, String lastName, Role role) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // Factory Methods implemented implicitly
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

    // Template Method for user validation
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

    public List<String> getCliperIds() { return cliperIds; }
    public void setCliperIds(List<String> cliperIds) { this.cliperIds = cliperIds; }

    public List<String> getPostIds() { return postIds; }
    public void setPostIds(List<String> postIds) { this.postIds = postIds; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getAtsProfileId() { return atsProfileId; }
    public void setAtsProfileId(String atsProfileId) { this.atsProfileId = atsProfileId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public enum Role {
        CANDIDATE, COMPANY, ADMIN
    }
}