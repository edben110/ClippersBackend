package com.clipers.clipers.dto.matching;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDTO {
    private String id;
    private String name;
    private List<String> skills;
    private Double experienceYears;
    private List<ExperienceDTO> experience;
    private List<EducationDTO> education;
    private List<String> languages;
    private String summary;
    private String location;
}
