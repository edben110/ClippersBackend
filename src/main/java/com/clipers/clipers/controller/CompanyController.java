package com.clipers.clipers.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clipers.clipers.entity.Company;
import com.clipers.clipers.entity.User;
import com.clipers.clipers.repository.CompanyRepository;
import com.clipers.clipers.repository.UserRepository;

/**
 * Controlador para gestión de empresas
 */
@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = "*")
public class CompanyController {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Autowired
    public CompanyController(CompanyRepository companyRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Company> getMyCompany() {
        try {
            String userId = getCurrentUserId();
            Optional<Company> company = companyRepository.findByUserId(userId);

            if (company.isPresent()) {
                return ResponseEntity.ok(company.get());
            } else {
                // Create default company if it doesn't exist
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                Company newCompany = new Company();
                newCompany.setName(user.getFirstName() != null ? user.getFirstName() : "Empresa");
                newCompany.setUserId(user.getId());
                Company savedCompany = companyRepository.save(newCompany);

                return ResponseEntity.ok(savedCompany);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener empresa: " + e.getMessage(), e);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Company> getCompanyByUserId(@PathVariable String userId) {
        Optional<Company> company = companyRepository.findByUserId(userId);
        return company.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Company> updateCompany(@PathVariable String id, @RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            Optional<Company> existingCompany = companyRepository.findById(id);

            if (existingCompany.isEmpty()) {
                throw new RuntimeException("Empresa no encontrada");
            }

            Company company = existingCompany.get();

            // Verify ownership
            if (!company.getUserId().equals(userId)) {
                throw new RuntimeException("No tienes permisos para editar esta empresa");
            }

            // Update fields
            if (request.containsKey("name")) {
                company.setName((String) request.get("name"));
            }
            if (request.containsKey("description")) {
                company.setDescription((String) request.get("description"));
            }
            if (request.containsKey("location")) {
                company.setLocation((String) request.get("location"));
            }
            if (request.containsKey("industry")) {
                company.setIndustry((String) request.get("industry"));
            }
            if (request.containsKey("website")) {
                company.setWebsite((String) request.get("website"));
            }
            if (request.containsKey("size")) {
                company.setSize((String) request.get("size"));
            }
            if (request.containsKey("foundedYear")) {
                company.setFoundedYear((Integer) request.get("foundedYear"));
            }
            if (request.containsKey("mission")) {
                company.setMission((String) request.get("mission"));
            }
            if (request.containsKey("vision")) {
                company.setVision((String) request.get("vision"));
            }
            if (request.containsKey("culture")) {
                company.setCulture((String) request.get("culture"));
            }
            if (request.containsKey("employeeCount")) {
                company.setEmployeeCount((Integer) request.get("employeeCount"));
            }
            if (request.containsKey("benefits")) {
                @SuppressWarnings("unchecked")
                java.util.List<String> benefits = (java.util.List<String>) request.get("benefits");
                company.setBenefits(benefits);
            }
            if (request.containsKey("values")) {
                @SuppressWarnings("unchecked")
                java.util.List<String> values = (java.util.List<String>) request.get("values");
                company.setValues(values);
            }
            if (request.containsKey("socialMedia")) {
                @SuppressWarnings("unchecked")
                java.util.List<String> socialMedia = (java.util.List<String>) request.get("socialMedia");
                company.setSocialMedia(socialMedia);
            }

            Company savedCompany = companyRepository.save(company);
            return ResponseEntity.ok(savedCompany);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar empresa: " + e.getMessage(), e);
        }
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }
}