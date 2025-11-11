package com.clipers.clipers.dto;

import com.clipers.clipers.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    
    @Email(message = "Email debe tener un formato válido")
    @NotBlank(message = "Email es requerido")
    private String email;
    
    @NotBlank(message = "Password es requerido")
    @Size(min = 8, message = "Password debe tener al menos 8 caracteres")
    private String password;
    
    @NotNull(message = "Role es requerido")
    private User.Role role;
    
    private String firstName;
    private String lastName;
    private String companyName;

    // Constructors
    public RegisterRequest() {}

    public RegisterRequest(String email, String password, User.Role role, 
                          String firstName, String lastName, String companyName) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.companyName = companyName;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public User.Role getRole() { return role; }
    public void setRole(User.Role role) { this.role = role; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
}
