package com.clipers.clipers.dto.matching;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceDTO {
    private String company;
    private String position;
    private String description;
    private String startDate;
    private String endDate;
    private Double years;
}
