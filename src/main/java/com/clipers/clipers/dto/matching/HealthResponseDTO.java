package com.clipers.clipers.dto.matching;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponseDTO {
    private String status;
    private String message;
    private String service;
    private String version;
}
