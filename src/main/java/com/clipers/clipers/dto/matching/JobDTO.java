package com.clipers.clipers.dto.matching;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO {
    private String id;
    private String title;
    private String description;
    private List<String> skills;
    private List<String> requirements;
    private String location;
    private String type; // FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP
    private Integer salaryMin;
    private Integer salaryMax;
    private Double minExperienceYears;
}
